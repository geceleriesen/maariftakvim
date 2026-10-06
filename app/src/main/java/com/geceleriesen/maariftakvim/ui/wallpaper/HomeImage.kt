package com.geceleriesen.maariftakvim.ui.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.media.ExifInterface
import com.geceleriesen.maariftakvim.data.Settings
import java.io.File
import kotlin.math.max

/**
 * Canli modda telefon kilitli degilken (ana ekranda) gosterilecek kullanici resmi.
 * Resim uygulamanin kendi klasorune kopyalanir; sistem duvar kagidina DOKUNULMAZ.
 */
object HomeImage {

    private const val FILE = "home_bg.jpg"
    private const val MAX_SIDE = 2400

    fun save(context: Context, uri: Uri): Boolean {
        try {
            val cr = context.contentResolver

            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2
            val opts = BitmapFactory.Options()
            opts.inSampleSize = sample
            val raw = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return false

            val degrees = try {
                cr.openInputStream(uri)?.use { exifDegrees(it) } ?: 0
            } catch (e: Exception) {
                0
            }
            val bmp = if (degrees != 0) {
                val m = Matrix()
                m.postRotate(degrees.toFloat())
                Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
            } else {
                raw
            }

            File(context.filesDir, FILE).outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            Settings(context).homeImageVersion = System.currentTimeMillis()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun clear(context: Context) {
        try {
            File(context.filesDir, FILE).delete()
        } catch (e: Exception) {
            // yok sayilir
        }
        Settings(context).homeImageVersion = 0L
    }

    /** Resmi ekran boyutuna "kirparak doldur" (center-crop) olcekler. Yoksa null. */
    fun load(context: Context, w: Int, h: Int): Bitmap? {
        return try {
            val f = File(context.filesDir, FILE)
            if (!f.exists()) return null
            val src = BitmapFactory.decodeFile(f.path) ?: return null
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val s = max(w / src.width.toFloat(), h / src.height.toFloat())
            val dw = src.width * s
            val dh = src.height * s
            val p = Paint(Paint.FILTER_BITMAP_FLAG)
            Canvas(out).drawBitmap(src, null, RectF((w - dw) / 2f, (h - dh) / 2f, (w + dw) / 2f, (h + dh) / 2f), p)
            src.recycle()
            out
        } catch (e: Exception) {
            null
        }
    }

    private fun exifDegrees(stream: java.io.InputStream): Int {
        val o = ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (o) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
