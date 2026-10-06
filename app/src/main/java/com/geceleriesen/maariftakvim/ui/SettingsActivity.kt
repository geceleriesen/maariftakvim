package com.geceleriesen.maariftakvim.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.geceleriesen.maariftakvim.data.AgendaRepository
import com.geceleriesen.maariftakvim.data.CityPref
import com.geceleriesen.maariftakvim.data.LocationHelper
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.network.CityResult
import com.geceleriesen.maariftakvim.network.CitySearch
import com.geceleriesen.maariftakvim.ui.wallpaper.MaarifLiveWallpaper
import com.geceleriesen.maariftakvim.ui.wallpaper.WallpaperJob
import com.geceleriesen.maariftakvim.ui.wallpaper.WallpaperScheduler

class SettingsActivity : Activity() {

    companion object {
        private const val REQ_LOCATION = 11
        private const val REQ_CALENDAR = 12
    }

    private val ink = 0xFF2B2118.toInt()
    private lateinit var settings: Settings

    private lateinit var city1Text: TextView
    private lateinit var city2Text: TextView
    private lateinit var locationBtn: Button
    private lateinit var agendaBtn: Button
    private lateinit var screenBtn: Button
    private lateinit var lockDailyBtn: Button
    private lateinit var homeDailyBtn: Button
    private lateinit var liveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)

        val dp = resources.displayMetrics.density
        val pad = (20 * dp).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(pad, pad * 2, pad, pad)

        root.addView(textView("Ayarlar", 28f, true))
        root.addView(textView("Takvimde iki şehrin vakitleri, saati ve havası görünür.", 15f, false, pad / 2))

        // --- Şehirler ---
        root.addView(textView("1. şehir (sol sütun)", 14f, false, pad))
        city1Text = textView("", 24f, true)
        root.addView(city1Text)
        root.addView(button("Şehri değiştir") { pickCity(1) })
        locationBtn = button("") { toggleLocation() }
        root.addView(locationBtn)

        root.addView(textView("2. şehir (sağ sütun)", 14f, false, pad))
        city2Text = textView("", 24f, true)
        root.addView(city2Text)
        root.addView(button("Şehri değiştir") { pickCity(2) })

        // --- Ajanda ---
        root.addView(textView("Ajanda", 14f, false, pad * 2))
        root.addView(textView("Telefon takvimindeki bugünün sıradaki etkinliği takvim sayfasında görünür. Bilgi telefondan çıkmaz.", 13f, false, 4))
        agendaBtn = button("") { toggleAgenda() }
        root.addView(agendaBtn)

        // --- Ekran ---
        root.addView(textView("Ekran", 14f, false, pad * 2))
        screenBtn = button("") { toggleScreen() }
        root.addView(screenBtn)

        // --- Kilit ekranı ---
        root.addView(textView("Kilit ekranı", 14f, false, pad * 2))
        root.addView(
            textView(
                "Bugünün takvim yaprağı kilit ekranı resmi olarak ayarlanır. " +
                    "Gizlilik için ajanda bu resme eklenmez. Telefonun markasına göre kilit ekranı resmi " +
                    "başka bir tema uygulamasıyla değişebilir.",
                13f, false, 4
            )
        )
        root.addView(
            textView(
                "Canlı kilit ekranı: analog saatin akrep ve yelkovanı gerçekten çalışır. " +
                    "Açınca telefonun duvar kağıdı ekranı açılır; orada kilit ekranına uygula'yı seç. " +
                    "Bu mod açıkken gece yenilemesi (statik resim) devre dışı kalır.",
                13f, false, 4
            )
        )
        liveBtn = button("") { toggleLive() }
        root.addView(liveBtn)
        root.addView(button("Kilit ekranına şimdi uygula (statik)") { applyLockNow() })
        lockDailyBtn = button("") { toggleLockDaily() }
        root.addView(lockDailyBtn)
        homeDailyBtn = button("") { toggleHomeDaily() }
        root.addView(homeDailyBtn)

        // --- Gizlilik ---
        root.addView(
            textView(
                "Gizlilik: Şehir koordinatları hava durumu (Open-Meteo) ve namaz vakti (Aladhan) " +
                    "servislerine gönderilir. Otomatik konum açıksa telefonun son bilinen konumu kullanılır.",
                12f, false, pad * 2
            )
        )
        root.addView(textView("İpucu: Takvim ekranında dokununca yaprak döner, uzun basınca buraya gelinir.", 12f, false, pad))

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(0xFFF1E6BE.toInt())
        scroll.addView(root)
        setContentView(scroll)
        refreshRows()
    }

    private fun refreshRows() {
        city1Text.text = if (settings.autoLocation) "Otomatik (bulunduğum yer)" else settings.city1.name
        city2Text.text = settings.city2.name
        locationBtn.text = if (settings.autoLocation) "Otomatik konum: AÇIK (kapat)" else "Otomatik konum: KAPALI (aç)"
        agendaBtn.text = if (settings.showAgenda) "Ajanda: AÇIK (kapat)" else "Ajanda: KAPALI (aç)"
        screenBtn.text = if (settings.keepScreenOn) "Ekran açık kalsın: EVET (kapat)" else "Ekran açık kalsın: HAYIR (aç)"
        lockDailyBtn.text = if (settings.lockDaily) "Her gece otomatik yenile: AÇIK (kapat)" else "Her gece otomatik yenile: KAPALI (aç)"
        liveBtn.text = if (settings.liveLock) "Canlı kilit ekranı: AÇIK (kapat)" else "Canlı kilit ekranı: KAPALI (aç)"
        homeDailyBtn.text = if (settings.homeDaily) "Ana ekrana da bas: AÇIK (kapat)" else "Ana ekrana da bas: KAPALI (aç)"
    }

    // ---------- Düğmeler ----------

    private fun toggleLocation() {
        if (settings.autoLocation) {
            settings.autoLocation = false
            refreshRows()
        } else if (LocationHelper.hasPermission(this)) {
            settings.autoLocation = true
            refreshRows()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQ_LOCATION)
        }
    }

    private fun toggleAgenda() {
        if (settings.showAgenda) {
            settings.showAgenda = false
            refreshRows()
        } else if (AgendaRepository(this).hasPermission()) {
            settings.showAgenda = true
            refreshRows()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), REQ_CALENDAR)
        }
    }

    private fun toggleScreen() {
        settings.keepScreenOn = !settings.keepScreenOn
        refreshRows()
    }

    private fun applyLockNow() {
        if (settings.liveLock) {
            toast("Canlı kilit ekranı açık, statik resim onu ezmesin diye atlandı. Önce canlı modu kapat.")
            return
        }
        toast("Kilit ekranı resmi hazırlanıyor, birkaç saniye sürebilir…")
        Thread {
            val ok = WallpaperJob.applyNow(this)
            runOnUiThread {
                if (ok) {
                    val where = if (settings.homeDaily) "Kilit ve ana ekran güncellendi." else "Kilit ekranı güncellendi. Telefonu kilitleyip bak."
                    toast(where)
                } else {
                    toast("Kilit ekranı ayarlanamadı. Telefonun bunu engelliyor olabilir.")
                }
            }
        }.start()
    }

    private fun toggleLive() {
        if (settings.liveLock) {
            settings.liveLock = false
            toast("Canlı kilit ekranı kapandı. Telefonun duvar kağıdından başka bir resim seçebilir ya da statik yenilemeyi kullanabilirsin.")
            refreshRows()
            return
        }
        try {
            val i = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            i.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, MaarifLiveWallpaper::class.java)
            )
            startActivity(i)
            settings.liveLock = true
        } catch (e: Exception) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
                settings.liveLock = true
                toast("Listeden Büyük Saatli Maarif Takvimi'ni seç.")
            } catch (e2: Exception) {
                toast("Telefon canlı duvar kağıdı ekranını açamadı.")
            }
        }
        refreshRows()
    }

    private fun toggleLockDaily() {
        if (settings.lockDaily) {
            settings.lockDaily = false
            settings.homeDaily = false
            WallpaperScheduler.cancel(this)
        } else {
            settings.lockDaily = true
            WallpaperScheduler.schedule(this)
            applyLockNow()
        }
        refreshRows()
    }

    private fun toggleHomeDaily() {
        if (settings.homeDaily) {
            settings.homeDaily = false
        } else {
            settings.homeDaily = true
            if (!settings.lockDaily) {
                settings.lockDaily = true
                WallpaperScheduler.schedule(this)
            }
            applyLockNow()
        }
        refreshRows()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (requestCode == REQ_LOCATION) {
            if (granted) {
                settings.autoLocation = true
            } else {
                toast("Konum izni verilmedi, otomatik konum kapalı kaldı.")
            }
        } else if (requestCode == REQ_CALENDAR) {
            if (granted) {
                settings.showAgenda = true
            } else {
                toast("Takvim izni verilmedi, ajanda kapalı kaldı.")
            }
        }
        refreshRows()
    }

    // ---------- Şehir arama ----------

    private fun pickCity(slot: Int) {
        val input = EditText(this)
        input.hint = "Şehir adı (örn. İzmir, Berlin)"
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setSingleLine()

        AlertDialog.Builder(this)
            .setTitle("Şehir ara")
            .setView(input)
            .setPositiveButton("Ara") { _, _ -> runSearch(slot, input.text.toString()) }
            .setNegativeButton("Vazgeç", null)
            .show()
    }

    private fun runSearch(slot: Int, query: String) {
        if (query.trim().length < 2) {
            toast("En az 2 harf yaz.")
            return
        }
        Thread {
            val results = try {
                CitySearch.search(query)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                if (results == null) {
                    toast("Arama başarısız, internet bağlantısını kontrol et.")
                } else if (results.isEmpty()) {
                    toast("Sonuç bulunamadı.")
                } else {
                    showResults(slot, results)
                }
            }
        }.start()
    }

    private fun showResults(slot: Int, results: List<CityResult>) {
        val labels = results.map { it.label() }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Birini seç")
            .setItems(labels) { _, which -> saveCity(slot, results[which]) }
            .setNegativeButton("Vazgeç", null)
            .show()
    }

    private fun saveCity(slot: Int, r: CityResult) {
        val zone = if (r.zone.isNotEmpty()) r.zone else "Europe/Istanbul"
        val pref = CityPref(r.name, r.lat, r.lon, zone)
        if (slot == 1) {
            settings.city1 = pref
            settings.autoLocation = false
        } else {
            settings.city2 = pref
        }
        refreshRows()
    }

    // ---------- Yardımcılar ----------

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun textView(text: String, sizeSp: Float, bold: Boolean, top: Int = 0): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = sizeSp
        tv.setTextColor(ink)
        tv.typeface = if (bold) Typeface.create(Typeface.SERIF, Typeface.BOLD) else Typeface.SERIF
        tv.setPadding(0, top, 0, 0)
        return tv
    }

    private fun button(label: String, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.setOnClickListener { onClick() }
        return b
    }
}
