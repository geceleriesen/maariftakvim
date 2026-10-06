package com.geceleriesen.maariftakvim.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.geceleriesen.maariftakvim.data.AgendaRepository
import com.geceleriesen.maariftakvim.data.CalendarRepository
import com.geceleriesen.maariftakvim.data.CityResolver
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.network.CityData
import com.geceleriesen.maariftakvim.network.WeatherPrayerService
import com.geceleriesen.maariftakvim.ui.page.CalendarPageView
import com.geceleriesen.maariftakvim.ui.page.PageData
import com.geceleriesen.maariftakvim.ui.wallpaper.LiveWallpaperLauncher
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private lateinit var page: CalendarPageView
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val tr: Locale = Locale.forLanguageTag("tr-TR")

    private var job: Job? = null
    private var lastNetworkAt = 0L
    private val lock = Any()

    // Her 30 sn'de bir bakar: gun degistiyse hemen, degilse 10 dk'da bir veriyi yeniler.
    private val ticker = object : Runnable {
        override fun run() {
            val d = page.data
            val now = System.currentTimeMillis()
            val dateChanged = d != null && d.date != LocalDate.now()
            if (d == null || dateChanged || now - lastNetworkAt >= REFRESH_MS) refresh()
            page.postDelayed(this, 30_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 28) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }

        page = CalendarPageView(this)
        // Dokun: yapragi cevir (on yuz / arka yuz). Uzun bas: ayarlar.
        page.setOnClickListener { flip() }
        page.setOnLongClickListener {
            openSettings()
            true
        }
        setContentView(page)
        hideSystemBars()

        page.post { maybeShowFirstRun() }
    }

    override fun onResume() {
        super.onResume()
        applyKeepScreenOn()
        page.removeCallbacks(ticker)
        lastNetworkAt = 0L // geri donunce (ayarlar degismis olabilir) hemen yenile
        ticker.run()
    }

    override fun onPause() {
        page.removeCallbacks(ticker)
        super.onPause()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun applyKeepScreenOn() {
        if (Settings(this).keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun flip() {
        page.animate().scaleX(0f).setDuration(150).withEndAction {
            val d = page.data
            if (d != null) page.data = d.copy(showBack = !d.showBack)
            page.animate().scaleX(1f).setDuration(150).start()
        }.start()
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun maybeShowFirstRun() {
        val settings = Settings(this)
        if (settings.firstRunDone) return
        settings.firstRunDone = true

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
            .setTitle("Hoş geldin")
            .setMessage(
                "Takvim yaprağını kilit ekranına koyabilirsin; analog saatler orada da gerçekten çalışır.\n\n" +
                    "“Kilit ekranına koy”a basınca açılan ekranda önizlemenin altındaki “Uygula” düğmesine bas " +
                    "ve kilit ekranını seç.\n\n" +
                    "Yaprağı çevirmek için ekrana dokun, ayarlar için uzun bas."
            )
            .setPositiveButton("Kilit ekranına koy") { _, _ ->
                if (!LiveWallpaperLauncher.open(this)) {
                    Toast.makeText(this, "Telefon canlı duvar kağıdı ekranını açamadı.", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Şehir seç") { _, _ -> openSettings() }
            .setNegativeButton("Şimdi değil", null)
            .show()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    private fun blankCity(name: String) = CityData(
        cityName = name.uppercase(tr), temp = "--",
        imsak = "--:--", gunes = "--:--", ogle = "--:--",
        ikindi = "--:--", aksam = "--:--", yatsi = "--:--"
    )

    private fun refresh() {
        val today = LocalDate.now()
        val baseDay = CalendarRepository(this).getDay(today)

        val settings = Settings(this)
        val saved1 = settings.city1
        val c2 = settings.city2
        val showAgenda = settings.showAgenda
        val autoLoc = settings.autoLocation

        // Ilk acilista bos gorunum; sonraki yenilemelerde eldeki veri EKRANDA KALIR (yanip sonme yok)
        if (page.data == null) {
            val firstName = if (autoLoc) "Konumum" else saved1.name
            page.data = PageData(baseDay, today, blankCity(firstName), blankCity(c2.name), saved1.zoneId(), c2.zoneId(), false)
        }

        // Onceki yenileme hala suruyorsa iptal et, eski sonuc yenisinin ustune yazmasin
        job?.cancel()
        val dir = filesDir
        job = scope.launch {
            val c1 = CityResolver.city1(this@MainActivity)
            val z1 = c1.zoneId()
            val z2 = c2.zoneId()
            val agenda = if (showAgenda) AgendaRepository(this@MainActivity).nextEventText() else ""

            // 1) Diskteki veriyle HEMEN ciz (ag beklemeden)
            var first = keepWeather(page.data?.left, WeatherPrayerService.cachedCityData(dir, c1.name, c1.lat, c1.lon))
            var second = keepWeather(page.data?.right, WeatherPrayerService.cachedCityData(dir, c2.name, c2.lat, c2.lon))
            fun push() {
                val (f, s2) = synchronized(lock) { Pair(first, second) }
                val day = baseDay.copy(dayLengthInfo = f.dayLengthInfo, agenda = agenda)
                runOnUiThread {
                    if (isDestroyed || isFinishing) return@runOnUiThread
                    val back = page.data?.showBack ?: false
                    page.data = PageData(day, today, f, s2, z1, z2, back)
                }
            }
            push()

            // 2) Aga git; hangi sehir once biterse o hemen ekrana yansir
            val a = launch {
                val r = WeatherPrayerService.fetchCityData(dir, c1.name, c1.lat, c1.lon, zone = z1)
                synchronized(lock) { first = keepWeather(first, r) }
                push()
            }
            val b = launch {
                val r = WeatherPrayerService.fetchCityData(dir, c2.name, c2.lat, c2.lon, zone = z2)
                synchronized(lock) { second = keepWeather(second, r) }
                push()
            }
            a.join()
            b.join()
            // Eksik veri kaldiysa (ag yok) 10 dk beklemeden 2 dk sonra tekrar dene
            val missing = first.temp == "--" || first.imsak == "--:--" || second.temp == "--" || second.imsak == "--:--"
            lastNetworkAt = System.currentTimeMillis() - if (missing) REFRESH_MS - 2 * 60_000L else 0L
        }
    }

    // Yeni veride sicaklik okunamadiysa (ag yok) ayni sehrin eldeki sicakligini koru
    private fun keepWeather(old: CityData?, new: CityData): CityData {
        if (old != null && new.temp == "--" && old.temp != "--" && old.cityName == new.cityName) {
            return new.copy(temp = old.temp, weatherCode = old.weatherCode)
        }
        return new
    }

    companion object {
        private const val REFRESH_MS = 10 * 60 * 1000L
    }
}
