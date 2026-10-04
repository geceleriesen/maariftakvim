package com.geceleriesen.maariftakvim.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.geceleriesen.maariftakvim.data.CalendarRepository
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.network.CityData
import com.geceleriesen.maariftakvim.network.WeatherPrayerService
import com.geceleriesen.maariftakvim.ui.page.CalendarPageView
import com.geceleriesen.maariftakvim.ui.page.PageData
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private lateinit var page: CalendarPageView
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val tr: Locale = Locale.forLanguageTag("tr-TR")

    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            page.postDelayed(this, 15 * 60 * 1000L)
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        page = CalendarPageView(this)
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
        page.removeCallbacks(ticker)
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
                "Takvimde iki şehrin vakitleri, saati ve havası görünür. " +
                    "Şehirlerini şimdi seçmek ister misin?\n\n" +
                    "Bunu sonra da yapabilirsin: takvim ekranına uzun bas."
            )
            .setPositiveButton("Seç") { _, _ -> openSettings() }
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
        val day = CalendarRepository(this).getDay(today)

        val settings = Settings(this)
        val c1 = settings.city1
        val c2 = settings.city2
        val z1 = c1.zoneId()
        val z2 = c2.zoneId()

        // Once cevrimdisi gorunumu hemen goster
        page.data = PageData(day, today, blankCity(c1.name), blankCity(c2.name), z1, z2)

        val dir = filesDir
        scope.launch {
            val a = async { WeatherPrayerService.fetchCityData(dir, c1.name, c1.lat, c1.lon) }
            val b = async { WeatherPrayerService.fetchCityData(dir, c2.name, c2.lat, c2.lon) }
            val first = a.await()
            val second = b.await()
            val fresh = PageData(day.copy(dayLengthInfo = first.dayLengthInfo), today, first, second, z1, z2)
            runOnUiThread { page.data = fresh }
        }
    }
}