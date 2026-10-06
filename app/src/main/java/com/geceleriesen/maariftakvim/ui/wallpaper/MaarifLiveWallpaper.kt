package com.geceleriesen.maariftakvim.ui.wallpaper

import android.app.Activity
import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.geceleriesen.maariftakvim.data.CalendarRepository
import com.geceleriesen.maariftakvim.data.CityResolver
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.network.WeatherPrayerService
import com.geceleriesen.maariftakvim.ui.page.CalendarPageRenderer
import com.geceleriesen.maariftakvim.ui.page.FontLoader
import com.geceleriesen.maariftakvim.ui.page.PageData
import java.time.LocalDate
import java.util.concurrent.Executors
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

/**
 * Canli duvar kagidi: takvim yapragi kilit ekraninda gercekten calisan analog saatle gorunur.
 *
 * Yaprak (kollar haric) veri degisince bir kez bitmap'e cizilir; ekran gorunurken her saniye
 * bitmap'in ustune yalniz akrep/yelkovan/saniye kolu cizilir. Ekran kapaliyken hicbir sey cizilmez.
 */
class MaarifLiveWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine = ClockEngine()

    private inner class ClockEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private val worker = Executors.newSingleThreadExecutor()
        private val renderer = CalendarPageRenderer(FontLoader.numberFace(applicationContext))

        private var data: PageData? = null
        private var base: Bitmap? = null
        private var w = 0
        private var h = 0
        private var visible = false
        private var lastNetAt = 0L
        private var lastContentVer = -1L

        @Volatile
        private var loading = false

        // Ana ekran resmi: telefon kilitli DEGILKEN bu resim, kilitliyken takvim gorunur
        private val keyguard = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        private var homeBase: Bitmap? = null
        private var homeVer = 0L
        private var showingHome = false

        private val lockStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (visible) {
                    handler.removeCallbacks(tick)
                    handler.post(tick)
                }
            }
        }

        private val tick = object : Runnable {
            override fun run() {
                if (!visible) return
                val now = System.currentTimeMillis()
                val d = data
                if (d == null || d.date != LocalDate.now() || now - lastNetAt >= REFRESH_MS) loadData()
                drawFrame()
                handler.postDelayed(this, 1000L - (now % 1000L))
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val f = IntentFilter(Intent.ACTION_USER_PRESENT)
            f.addAction(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(lockStateReceiver, f, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(lockStateReceiver, f)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            w = width
            h = height
            dropBase()
            dropHome()
            if (visible) drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            showingHome = false // gorunur olunca ilk kareyi mutlaka yeniden ciz
            handler.removeCallbacks(tick)
            if (visible) handler.post(tick)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(tick)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            visible = false
            handler.removeCallbacks(tick)
            worker.shutdownNow()
            try {
                unregisterReceiver(lockStateReceiver)
            } catch (e: Exception) {
                // kayitli degilse sorun degil
            }
            dropBase()
            dropHome()
            super.onDestroy()
        }

        private fun dropBase() {
            base?.recycle()
            base = null
        }

        private fun dropHome() {
            homeBase?.recycle()
            homeBase = null
            showingHome = false
        }

        private fun inset(): Int = (h * WallpaperJob.TOP_INSET_RATIO).toInt()

        private fun drawFrame() {
            if (w <= 0 || h <= 0) return

            // Kullanici ana ekran resmi sectiyse ve telefon kilitli degilse o resmi goster
            val ver = Settings(applicationContext).homeImageVersion
            if (ver != homeVer) {
                dropHome()
                homeVer = ver
            }
            var homeBmp: Bitmap? = null
            if (ver != 0L && !keyguard.isKeyguardLocked) {
                if (showingHome) return // degisen bir sey yok, bos yere cizme
                homeBmp = homeBase ?: HomeImage.load(applicationContext, w, h)
                homeBase = homeBmp
            }

            val holder = surfaceHolder
            var c: Canvas? = null
            try {
                c = holder.lockCanvas()
                if (c == null) return
                if (homeBmp != null) {
                    c.drawBitmap(homeBmp, 0f, 0f, null)
                    showingHome = true
                    return
                }
                showingHome = false
                val d = data
                if (d == null) {
                    c.drawColor(PAPER)
                    return
                }
                var b = base
                if (b == null) {
                    b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    renderer.draw(Canvas(b), w, h, d, inset(), withHands = false)
                    base = b
                }
                c.drawBitmap(b, 0f, 0f, null)
                renderer.drawHandsOnly(c, w, h, d, inset())
            } catch (e: Exception) {
                // bir kare cizilemezse sonraki saniyede tekrar denenir
            } finally {
                if (c != null) {
                    try {
                        holder.unlockCanvasAndPost(c)
                    } catch (e: Exception) {
                        // yuzey kapanmis olabilir
                    }
                }
            }
        }

        /** Once diskteki veriyle hemen, sonra aga giderek gunceller. Arka plan is parcaciginda calisir. */
        private fun loadData() {
            if (loading) return
            loading = true
            lastNetAt = System.currentTimeMillis()
            val app = applicationContext
            try {
                worker.execute {
                    try {
                        val settings = Settings(app)
                        val today = LocalDate.now()
                        val c1 = CityResolver.city1(app)
                        val c2 = settings.city2
                        val z1 = c1.zoneId()
                        val z2 = c2.zoneId()
                        val dir = app.filesDir
                        val repo = CalendarRepository(app)

                        fun publish(first: com.geceleriesen.maariftakvim.network.CityData, second: com.geceleriesen.maariftakvim.network.CityData) {
                            // Gizlilik: ajanda kilit ekranina asla eklenmez
                            val day = repo.getDay(today).copy(dayLengthInfo = first.dayLengthInfo, agenda = "")
                            val pd = PageData(day, today, first, second, z1, z2, false)
                            handler.post {
                                data = pd
                                dropBase()
                                if (visible) drawFrame()
                            }
                        }

                        publish(
                            WeatherPrayerService.cachedCityData(dir, c1.name, c1.lat, c1.lon),
                            WeatherPrayerService.cachedCityData(dir, c2.name, c2.lat, c2.lon)
                        )

                        val (first, second) = runBlocking {
                            coroutineScope {
                                val a = async { WeatherPrayerService.fetchCityData(dir, c1.name, c1.lat, c1.lon, zone = z1) }
                                val b = async { WeatherPrayerService.fetchCityData(dir, c2.name, c2.lat, c2.lon, zone = z2) }
                                Pair(a.await(), b.await())
                            }
                        }
                        publish(first, second)

                        // Eksik veri kaldiysa (ag yok) 2 dk sonra tekrar dene
                        val missing = first.temp == "--" || first.imsak == "--:--" || second.temp == "--" || second.imsak == "--:--"
                        if (missing) lastNetAt = System.currentTimeMillis() - REFRESH_MS + 2 * 60_000L
                    } catch (e: Exception) {
                        // bir sonraki turda tekrar denenir
                    } finally {
                        loading = false
                    }
                }
            } catch (e: Exception) {
                loading = false
            }
        }
    }

    companion object {
        private const val REFRESH_MS = 10 * 60 * 1000L
        private const val PAPER = 0xFFE7D7A3.toInt()
    }
}

/** Sistemin canli duvar kagidi onizleme ekranini acar (ayarlar ve ilk acilis ortak kullanir). */
object LiveWallpaperLauncher {

    /** Ekran acilabildiyse true doner ve canli modu isaretler. */
    fun open(activity: Activity): Boolean {
        val settings = Settings(activity)
        return try {
            val i = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            i.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(activity, MaarifLiveWallpaper::class.java)
            )
            activity.startActivity(i)
            settings.liveLock = true
            true
        } catch (e: Exception) {
            try {
                activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
                settings.liveLock = true
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}
