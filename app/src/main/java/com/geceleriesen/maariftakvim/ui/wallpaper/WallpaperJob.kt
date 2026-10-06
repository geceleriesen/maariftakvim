package com.geceleriesen.maariftakvim.ui.wallpaper

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.DisplayMetrics
import android.view.WindowManager
import com.geceleriesen.maariftakvim.data.CalendarRepository
import com.geceleriesen.maariftakvim.data.CityResolver
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.network.WeatherPrayerService
import com.geceleriesen.maariftakvim.ui.page.CalendarPageRenderer
import com.geceleriesen.maariftakvim.ui.page.FontLoader
import com.geceleriesen.maariftakvim.ui.page.PageData
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

object WallpaperJob {

    const val ACTION_REFRESH = "com.geceleriesen.maariftakvim.action.REFRESH_LOCK"

    // Kilit ekraninin ustundeki saat ve bildirimler icin sayfanin ustunde birakilan oran
    private const val TOP_INSET_RATIO = 0.32f

    /**
     * Bugunun takvim yapragini kilit ekrani resmi olarak ayarlar.
     * Ag ve disk kullanir: ana is parcaciginda CAGIRMA. Basarili olursa true doner.
     * Ajanda gizlilik icin kilit ekrani resmine ASLA eklenmez.
     */
    @Suppress("DEPRECATION")
    fun applyNow(context: Context): Boolean {
        return try {
            val app = context.applicationContext
            val settings = Settings(app)
            val today = LocalDate.now()
            val c1 = CityResolver.city1(app)
            val c2 = settings.city2
            val dir = app.filesDir

            val (first, second) = runBlocking {
                coroutineScope {
                    val a = async { WeatherPrayerService.fetchCityData(dir, c1.name, c1.lat, c1.lon, zone = c1.zoneId()) }
                    val b = async { WeatherPrayerService.fetchCityData(dir, c2.name, c2.lat, c2.lon, zone = c2.zoneId()) }
                    Pair(a.await(), b.await())
                }
            }

            val day = CalendarRepository(app).getDay(today).copy(dayLengthInfo = first.dayLengthInfo, agenda = "")
            val data = PageData(day, today, first, second, c1.zoneId(), c2.zoneId(), false)

            val metrics = DisplayMetrics()
            val wm = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            CalendarPageRenderer(FontLoader.numberFace(app)).draw(Canvas(bmp), width, height, data, (height * TOP_INSET_RATIO).toInt())

            val which = if (settings.homeDaily) {
                WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
            } else {
                WallpaperManager.FLAG_LOCK
            }
            WallpaperManager.getInstance(app).setBitmap(bmp, null, true, which)
            bmp.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }
}

object WallpaperScheduler {

    private const val REQUEST_CODE = 1001

    /**
     * Siradaki 00:05 icin tek seferlik alarm kurar (Doze'da da calisir). Alarm tetiklenince
     * receiver bir sonrakini kendisi kurar; setInexactRepeating Doze'da saatlerce kayabiliyordu.
     */
    fun schedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val zone = ZoneId.systemDefault()
        var next = LocalDate.now().atTime(0, 5).atZone(zone)
        if (!next.toInstant().isAfter(java.time.Instant.now())) next = next.plusDays(1)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), pendingIntent(context))
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WallpaperReceiver::class.java)
        intent.action = WallpaperJob.ACTION_REFRESH
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** Gece alarmi ve telefon yeniden acildiginda kilit ekrani resmini yeniler. */
class WallpaperReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val settings = Settings(context)
        if (!settings.lockDaily) return

        val action = intent.action
        if (action != WallpaperJob.ACTION_REFRESH && action != Intent.ACTION_BOOT_COMPLETED) return

        // Yeniden baslatmada alarmlar silinir; tek seferlik alarm oldugu icin her tetiklenmede de sonrakini kur
        WallpaperScheduler.schedule(context)

        val pending = goAsync()
        Thread {
            try {
                WallpaperJob.applyNow(context)
            } finally {
                pending.finish()
            }
        }.start()
    }
}
