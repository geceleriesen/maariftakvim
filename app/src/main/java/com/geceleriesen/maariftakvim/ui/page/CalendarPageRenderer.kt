package com.geceleriesen.maariftakvim.ui.page

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.geceleriesen.maariftakvim.data.CalendarDay
import com.geceleriesen.maariftakvim.network.CityData
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Random
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

data class PageData(
    val day: CalendarDay,
    val date: LocalDate,
    val left: CityData,
    val right: CityData,
    val leftZone: ZoneId = ZoneId.systemDefault(),
    val rightZone: ZoneId = ZoneId.systemDefault()
)

/**
 * Takvim yapragini herhangi bir Canvas'a cizer (tam ekran, duvar kagidi, widget resmi).
 * Dikey eksende 1000 birim genislikli sanal bir koordinat sistemi kullanir.
 */
class CalendarPageRenderer {

    companion object {
        private const val VW = 1000f
        private const val BASE_H = 1600f
        private const val BOX_H = 396f
    }

    private val ink = 0xFF2B2118.toInt()
    private val inkSoft = 0xFF5A4A38.toInt()

    private val serif = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    private val serifItalic = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    private val condensedBold = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    private val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    fun draw(canvas: Canvas, width: Int, height: Int, data: PageData, now: LocalTime = LocalTime.now()) {
        val w = width.toFloat()
        val h = height.toFloat()

        drawPaper(canvas, w, h)

        val s = w / VW
        val vh = h / s
        val k = max(1f, vh / BASE_H)

        canvas.save()
        canvas.scale(s, s)

        drawFrame(canvas, vh)
        drawHeader(canvas, data)
        drawTitle(canvas, data)

        val clockY = 480f * k
        val boxTop = 640f * k
        drawClock(canvas, 167f, clockY, 78f, LocalTime.now(data.leftZone))
        drawClock(canvas, 833f, clockY, 78f, LocalTime.now(data.rightZone))
        txt(canvas, data.left.temp, 167f, clockY + 78f + 52f, 38f, ink, serifBold, 200f)
        txt(canvas, data.right.temp, 833f, clockY + 78f + 52f, 38f, ink, serifBold, 200f)

        drawPrayerBox(canvas, 52f, boxTop, 230f, data.left)
        drawPrayerBox(canvas, 718f, boxTop, 230f, data.right)

        // Dev gun numarasi
        txt(canvas, data.day.dayNumber, 500f, boxTop + 372f, 480f, ink, condensedBold, 410f)

        drawLowerPart(canvas, data, boxTop, vh)

        canvas.restore()
    }

    // ---------- Zemin ----------

    private fun drawPaper(canvas: Canvas, w: Float, h: Float) {
        fillPaint.shader = RadialGradient(
            w / 2f, h / 2f, max(w, h) * 0.75f,
            intArrayOf(0xFFF1E6BE.toInt(), 0xFFE7D7A3.toInt(), 0xFFCDB77C.toInt()),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null

        val unit = w / VW
        val rnd = Random(42)

        // Eskime lekeleri
        repeat(14) {
            fillPaint.color = Color.argb(6 + rnd.nextInt(8), 150, 110, 50)
            canvas.drawCircle(
                rnd.nextFloat() * w, rnd.nextFloat() * h,
                (40f + rnd.nextFloat() * 100f) * unit, fillPaint
            )
        }
        // Kagit benekleri
        repeat(900) {
            fillPaint.color = Color.argb(8 + rnd.nextInt(24), 110, 80, 30)
            canvas.drawCircle(
                rnd.nextFloat() * w, rnd.nextFloat() * h,
                (0.5f + rnd.nextFloat() * 2.4f) * unit, fillPaint
            )
        }
    }

    private fun drawFrame(canvas: Canvas, vh: Float) {
        strokePaint.color = ink
        strokePaint.strokeWidth = 5f
        canvas.drawRect(24f, 24f, VW - 24f, vh - 24f, strokePaint)
        strokePaint.strokeWidth = 2f
        canvas.drawRect(38f, 38f, VW - 38f, vh - 38f, strokePaint)

        fillPaint.color = ink
        var x = 45f
        while (x < VW - 40f) {
            canvas.drawCircle(x, 31f, 2.5f, fillPaint)
            canvas.drawCircle(x, vh - 31f, 2.5f, fillPaint)
            x += 26f
        }
        var y = 45f
        while (y < vh - 40f) {
            canvas.drawCircle(31f, y, 2.5f, fillPaint)
            canvas.drawCircle(VW - 31f, y, 2.5f, fillPaint)
            y += 26f
        }
    }

    // ---------- Ust bolum ----------

    private fun threeLines(dateText: String, label: String): List<String> {
        val parts = dateText.trim().split(" ")
        return if (parts.size == 3) {
            listOf("${parts[2]} $label", parts[1], parts[0])
        } else {
            listOf(dateText, "", "")
        }
    }

    private fun drawHeader(canvas: Canvas, d: PageData) {
        val day = d.day

        val hijri = threeLines(day.hijriDate, "HİCRİ")
        val rumi = threeLines(day.rumiDate, "RUMİ")

        val info = if (day.dayLengthInfo.isNotEmpty()) day.dayLengthInfo else "${day.dayOfYear}. GÜN"
        val idx = info.indexOf(" (")
        val center = if (idx > 0) {
            listOf(info.substring(0, idx), info.substring(idx + 1), "")
        } else {
            listOf(info, "", "")
        }

        for (i in 0 until 3) {
            val y = 130f + i * 36f
            val size = if (i == 0) 26f else 30f
            val face = if (i == 0) serif else serifBold
            txt(canvas, hijri[i], 185f, y, size, ink, face, 260f)
            txt(canvas, center[i], 500f, y, 26f, inkSoft, serif, 330f)
            txt(canvas, rumi[i], 815f, y, size, ink, face, 260f)
        }

        strokePaint.color = ink
        strokePaint.strokeWidth = 2f
        canvas.drawLine(60f, 222f, VW - 60f, 222f, strokePaint)
        val strip = "YIL: ${d.date.year}    AY: ${d.date.monthValue}    GÜN: ${day.dayOfYear}    KALAN: ${day.daysLeftInYear}"
        txt(canvas, strip, 500f, 256f, 30f, ink, serifBold, 880f)
        canvas.drawLine(60f, 274f, VW - 60f, 274f, strokePaint)
    }

    private fun drawTitle(canvas: Canvas, d: PageData) {
        txt(canvas, d.day.gregorianText, 500f, 362f, 74f, ink, serifBold, 900f)
    }

    // ---------- Saat ve vakit kutulari ----------

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float, now: LocalTime) {
        fillPaint.color = 0x33FFFFFF
        canvas.drawCircle(cx, cy, r, fillPaint)

        strokePaint.color = ink
        strokePaint.strokeWidth = 5f
        canvas.drawCircle(cx, cy, r, strokePaint)

        strokePaint.strokeWidth = 3f
        for (i in 0 until 12) {
            val a = Math.toRadians(i * 30.0)
            val inner = if (i % 3 == 0) r * 0.78f else r * 0.86f
            val sx = sin(a).toFloat()
            val cs = cos(a).toFloat()
            canvas.drawLine(cx + sx * inner, cy - cs * inner, cx + sx * r * 0.94f, cy - cs * r * 0.94f, strokePaint)
        }

        val minute = now.minute + now.second / 60f
        val hour = (now.hour % 12) + minute / 60f
        hand(canvas, cx, cy, hour * 30f, r * 0.5f, 7f)
        hand(canvas, cx, cy, minute * 6f, r * 0.76f, 4.5f)

        fillPaint.color = ink
        canvas.drawCircle(cx, cy, 5f, fillPaint)
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, degrees: Float, length: Float, width: Float) {
        val a = Math.toRadians(degrees.toDouble())
        strokePaint.color = ink
        strokePaint.strokeWidth = width
        strokePaint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(cx, cy, cx + sin(a).toFloat() * length, cy - cos(a).toFloat() * length, strokePaint)
        strokePaint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawPrayerBox(canvas: Canvas, left: Float, top: Float, width: Float, c: CityData) {
        strokePaint.color = ink
        strokePaint.strokeWidth = 3f
        canvas.drawRect(left, top, left + width, top + BOX_H, strokePaint)

        txt(canvas, c.cityName, left + width / 2f, top + 46f, 36f, ink, serifBold, width - 24f)
        strokePaint.strokeWidth = 2f
        canvas.drawLine(left + 14f, top + 62f, left + width - 14f, top + 62f, strokePaint)

        val rows = listOf(
            "İmsak" to c.imsak,
            "Güneş" to c.gunes,
            "Öğle" to c.ogle,
            "İkindi" to c.ikindi,
            "Akşam" to c.aksam,
            "Yatsı" to c.yatsi
        )
        var y = top + 62f + 48f
        for ((label, time) in rows) {
            txt(canvas, label, left + 16f, y, 26f, inkSoft, serif, 110f, Paint.Align.LEFT)
            txt(canvas, time, left + width - 16f, y, 28f, ink, serifBold, 90f, Paint.Align.RIGHT)
            y += 52f
        }
    }

    // ---------- Alt bolum ----------

    private fun drawLowerPart(canvas: Canvas, d: PageData, boxTop: Float, vh: Float) {
        val day = d.day
        var y = boxTop + BOX_H + 120f

        txt(canvas, day.dayName, 500f, y, 100f, ink, serifBold, 800f)
        y += 56f

        if (day.folkCalendar.isNotEmpty()) {
            txt(canvas, "(${day.folkCalendar})", 500f, y, 30f, inkSoft, serif, 880f)
            y += 14f
        }
        if (day.historyEvent.isNotEmpty()) {
            val l = layoutOf(day.historyEvent, 30f, inkSoft, serif, 860f)
            drawLayout(canvas, l, 500f, y, 860f)
            y += l.height + 10f
        }

        if (day.quote.isNotEmpty()) {
            val author = if (day.quoteAuthor.isNotEmpty()) " — ${day.quoteAuthor}" else ""
            val l = layoutOf("“${day.quote}”$author", 28f, inkSoft, serifItalic, 840f)
            val space = (vh - 120f) - y
            val top = y + max(10f, (space - l.height) / 2f)
            drawLayout(canvas, l, 500f, top, 840f)
        }

        strokePaint.color = ink
        strokePaint.strokeWidth = 2f
        canvas.drawLine(60f, vh - 100f, VW - 60f, vh - 100f, strokePaint)
        txt(canvas, "Büyük Saatli Maarif Takvimi", 500f, vh - 62f, 26f, inkSoft, serif, 800f)
    }

    // ---------- Yazi yardimcilari ----------

    private fun txt(
        canvas: Canvas,
        s: String,
        x: Float,
        baseline: Float,
        size: Float,
        color: Int,
        face: Typeface,
        maxWidth: Float,
        align: Paint.Align = Paint.Align.CENTER
    ) {
        tp.typeface = face
        tp.color = color
        tp.textAlign = align
        tp.textSize = size
        val w = tp.measureText(s)
        if (w > maxWidth && w > 0f) tp.textSize = size * maxWidth / w
        canvas.drawText(s, x, baseline, tp)
    }

    private fun layoutOf(s: String, size: Float, color: Int, face: Typeface, maxWidth: Float): StaticLayout {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG)
        p.typeface = face
        p.color = color
        p.textSize = size
        return StaticLayout.Builder.obtain(s, 0, s.length, p, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()
    }

    private fun drawLayout(canvas: Canvas, l: StaticLayout, centerX: Float, top: Float, maxWidth: Float) {
        canvas.save()
        canvas.translate(centerX - maxWidth / 2f, top)
        l.draw(canvas)
        canvas.restore()
    }
}