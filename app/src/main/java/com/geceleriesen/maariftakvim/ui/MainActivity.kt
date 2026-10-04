package com.geceleriesen.maariftakvim.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.geceleriesen.maariftakvim.data.CalendarRepository
import com.geceleriesen.maariftakvim.network.CityData
import com.geceleriesen.maariftakvim.network.WeatherPrayerService
import com.geceleriesen.maariftakvim.ui.page.CalendarPageView
import com.geceleriesen.maariftakvim.ui.page.PageData
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private lateinit var page: CalendarPageView
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
        setContentView(page)
        hideSystemBars()
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
        cityName = name, temp = "--",
        imsak = "--:--", gunes = "--:--", ogle = "--:--",
        ikindi = "--:--", aksam = "--:--", yatsi = "--:--"
    )

    private fun refresh() {
        val today = LocalDate.now()
        val day = CalendarRepository(this).getDay(today)

        // Once cevrimdisi gorunumu hemen goster
        page.data = PageData(day, today, blankCity("SÖKE"), blankCity("ANKARA"))

        // Sehirler 4b'de kullanicidan/GPS'ten gelecek
        val dir = filesDir
        scope.launch {
            val a = async { WeatherPrayerService.fetchCityData(dir, "Söke", 37.75, 27.40) }
            val b = async { WeatherPrayerService.fetchCityData(dir, "Ankara", 39.93, 32.85) }
            val soke = a.await()
            val ankara = b.await()
            val fresh = PageData(day.copy(dayLengthInfo = soke.dayLengthInfo), today, soke, ankara)
            runOnUiThread { page.data = fresh }
        }
    }
}