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
    val rightZone: ZoneId = ZoneId.systemDefault(),
    val showBack: Boolean = false
)

/**
 * Takvim yapragini herhangi bir Canvas'a cizer (tam ekran, kilit ekrani resmi).
 * Genisligi 1000 birim olan sanal bir koordinat sistemi kullanir.
 */
class CalendarPageRenderer(numberFace: Typeface? = null) {

    companion object {
        private const val VW = 1000f
        private const val BASE_H = 1600f
        private const val BOX_H = 396f
        private const val CLOCK_Y = 330f
        private const val SECOND_RED = 0xFFC62828.toInt()
    }

    private class Block(val title: String, val body: String, val note: String = "")

    private class Row(val height: Float, val draw: (Float) -> Unit)

    private val ink = 0xFF2B2118.toInt()
    private val inkSoft = 0xFF5A4A38.toInt()

    private val serif = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    private val serifItalic = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    private val condensedBold = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    // Buyuk gun numarasi: ozel font (Anton) yuklenebildiyse o, yoksa sistem fontu
    private val bigNumber = numberFace ?: condensedBold
    private val bigNumberSize = if (numberFace != null) 416f else 480f

    private val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    /**
     * topInset: sayfanin ustunde birakilacak piksel (kilit ekraninda saat/bildirim icin).
     * Kagit zemin tum ekrani kaplar, sayfa icerigi bu boslugun altina cizilir.
     */
    fun draw(canvas: Canvas, width: Int, height: Int, data: PageData, topInset: Int = 0, withHands: Boolean = true) {
        val w = width.toFloat()
        val h = height.toFloat()

        drawPaper(canvas, w, h)

        val inset = topInset.toFloat()
        // Sayfa genislige gore olceklenir; ekran kisa/genis ise (tablet, katlanir telefon)
        // yukseklige gore kucultulup ortalanir, boylece icerik asla tasmaz.
        val availH = (h - inset).coerceAtLeast(1f)
        val s = kotlin.math.min(w / VW, availH / BASE_H)
        val leafH = BASE_H * s
        val offsetX = (w - VW * s) / 2f
        val offsetY = inset + (availH - leafH) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(s, s)

        drawFrame(canvas, BASE_H)
        if (data.showBack) {
            drawBack(canvas, data, BASE_H)
        } else {
            drawFront(canvas, data, BASE_H, 1f, withHands)
        }

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

        // Iki cizgi arasinda yildiz motifi
        strokePaint.color = ink
        strokePaint.strokeWidth = 1.8f
        var x = 45f
        while (x < VW - 40f) {
            star(canvas, x, 31f)
            star(canvas, x, vh - 31f)
            x += 26f
        }
        var y = 45f
        while (y < vh - 40f) {
            star(canvas, 31f, y)
            star(canvas, VW - 31f, y)
            y += 26f
        }
    }

    private fun star(canvas: Canvas, cx: Float, cy: Float) {
        val r = 4.5f
        val d = 3.2f
        canvas.drawLine(cx - r, cy, cx + r, cy, strokePaint)
        canvas.drawLine(cx, cy - r, cx, cy + r, strokePaint)
        canvas.drawLine(cx - d, cy - d, cx + d, cy + d, strokePaint)
        canvas.drawLine(cx - d, cy + d, cx + d, cy - d, strokePaint)
    }

    // ---------- Ön yüz ----------

    private fun drawFront(canvas: Canvas, d: PageData, vh: Float, k: Float, withHands: Boolean) {
        drawHeader(canvas, d)

        val clockY = CLOCK_Y
        val boxTop = 455f
        val tempY = clockY + 96f
        val stripTop = 980f

        drawClock(canvas, 167f, clockY, 58f, LocalTime.now(d.leftZone), withHands)
        drawClock(canvas, 833f, clockY, 58f, LocalTime.now(d.rightZone), withHands)

        drawWeatherIcon(canvas, 104f, tempY - 10f, 36f, d.left.weatherCode)
        txt(canvas, d.left.temp, 184f, tempY, 28f, ink, serifBold, 100f)
        drawWeatherIcon(canvas, 770f, tempY - 10f, 36f, d.right.weatherCode)
        txt(canvas, d.right.temp, 850f, tempY, 28f, ink, serifBold, 100f)

        drawPrayerBox(canvas, 52f, boxTop, 220f, d.left)
        drawPrayerBox(canvas, 728f, boxTop, 220f, d.right)

        val parts = d.day.gregorianText.trim().split(" ")
        val monthYear = if (parts.size >= 3) parts[1] + " " + parts[2] else d.day.gregorianText
        txt(canvas, d.day.dayNumber, 500f, boxTop + 188f, 340f, ink, bigNumber, 430f)
        txt(canvas, monthYear, 500f, boxTop + 268f, 42f, ink, serifBold, 430f)
        txt(canvas, d.day.dayName, 500f, boxTop + 328f, 52f, ink, serifBold, 430f)

        drawBackStrip(canvas, d, stripTop, vh - 78f)
        drawFooter(canvas, vh, "Büyük Saatli Maarif Takvimi")
    }

    private fun drawBackStrip(canvas: Canvas, d: PageData, top: Float, bottom: Float) {
        val day = d.day
        strokePaint.color = ink
        strokePaint.strokeWidth = 2f
        canvas.drawLine(60f, top, VW - 60f, top, strokePaint)
        txt(canvas, "ARKA YAPRAK", 500f, top + 28f, 22f, ink, serifBold, 400f)

        val lines = ArrayList<String>()
        if (day.menu.isNotEmpty()) lines.add("Menü: " + day.menu)
        if (day.girlNames.isNotEmpty() || day.boyNames.isNotEmpty()) {
            lines.add("Doğanlar: " + day.girlNames + " / " + day.boyNames)
        }
        if (day.riddle.isNotEmpty()) lines.add("Bilmece: " + day.riddle)
        if (day.riddleAnswer.isNotEmpty()) lines.add("Cevap: " + day.riddleAnswer)
        if (day.joke.isNotEmpty()) lines.add("Fıkra: " + day.joke)
        if (day.historyEvent.isNotEmpty()) lines.add("Tarih: " + day.historyEvent)

        var y = top + 52f
        for (line in lines) {
            if (y + 28f > bottom) break
            val l = layoutOf(line, 22f, inkSoft, serif, 860f)
            drawLayout(canvas, l, 500f, y, 860f)
            y += l.height + 8f
        }
    }

    private fun lowerRows(canvas: Canvas, d: PageData): List<Row> {
        val day = d.day
        val rows = ArrayList<Row>()

        if (day.folkCalendar.isNotEmpty()) {
            rows.add(Row(46f) { top -> txt(canvas, "(${day.folkCalendar})", 500f, top + 32f, 30f, inkSoft, serif, 880f) })
        }
        if (day.historyEvent.isNotEmpty()) {
            val l = layoutOf(day.historyEvent, 30f, inkSoft, serif, 860f)
            rows.add(Row(l.height + 12f) { top -> drawLayout(canvas, l, 500f, top, 860f) })
        }
        if (day.agenda.isNotEmpty()) {
            val agenda = if (day.agenda.length > 46) day.agenda.take(45).trimEnd() + "…" else day.agenda
            rows.add(Row(52f) { top -> txt(canvas, "[AJANDA]: $agenda", 500f, top + 38f, 30f, ink, serifBold, 880f) })
        }
        if (day.quote.isNotEmpty()) {
            val author = if (day.quoteAuthor.isNotEmpty()) " — ${day.quoteAuthor}" else ""
            val l = layoutOf("“${day.quote}”$author", 28f, inkSoft, serifItalic, 840f)
            rows.add(Row(l.height + 40f) { top -> drawLayout(canvas, l, 500f, top + 28f, 840f) })
        }
        return rows
    }

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

    // ---------- Arka yaprak ----------

    private fun drawBack(canvas: Canvas, d: PageData, vh: Float) {
        val day = d.day
        txt(canvas, day.gregorianText, 500f, 130f, 44f, ink, serifBold, 880f)

        val blocks = ArrayList<Block>()
        if (day.menu.isNotEmpty()) blocks.add(Block("GÜNÜN MENÜSÜ", day.menu))
        if (day.girlNames.isNotEmpty() || day.boyNames.isNotEmpty()) {
            blocks.add(Block("BU GÜN DOĞANLARA", "Kız: ${day.girlNames}\nErkek: ${day.boyNames}"))
        }
        if (day.riddle.isNotEmpty()) {
            blocks.add(Block("GÜNÜN BİLMECESİ", day.riddle, "Cevap: ${day.riddleAnswer}"))
        }
        if (day.joke.isNotEmpty()) blocks.add(Block("GÜNÜN FIKRASI", day.joke))
        if (day.historyEvent.isNotEmpty()) blocks.add(Block("TARİHTE BUGÜN", day.historyEvent))

        val top = 190f
        val avail = (vh - 150f) - top

        // Metin sigmazsa yazi boyutunu kucult
        var bodies: List<StaticLayout> = emptyList()
        var notes: List<StaticLayout?> = emptyList()
        for (size in listOf(46f, 42f, 38f, 34f, 30f, 27f, 24f)) {
            bodies = blocks.map { layoutOf(it.body, size, ink, serif, 820f) }
            notes = blocks.map { b ->
                if (b.note.isEmpty()) null else layoutOf(b.note, size - 4f, inkSoft, serifItalic, 820f)
            }
            var total = 0f
            for (i in blocks.indices) total += blockHeight(bodies[i], notes[i])
            if (total <= avail * 0.85f) break
        }

        var total = 0f
        for (i in blocks.indices) total += blockHeight(bodies[i], notes[i])
        val gap = max(24f, (avail - total) / (blocks.size + 1))

        var y = top + gap
        for (i in blocks.indices) {
            txt(canvas, blocks[i].title, 500f, y + 42f, 42f, ink, serifBold, 800f)
            strokePaint.color = ink
            strokePaint.strokeWidth = 2f
            canvas.drawLine(430f, y + 62f, 570f, y + 62f, strokePaint)

            drawLayout(canvas, bodies[i], 500f, y + 80f, 820f)
            var h = 80f + bodies[i].height
            val n = notes[i]
            if (n != null) {
                drawLayout(canvas, n, 500f, y + h + 10f, 820f)
                h += 10f + n.height
            }
            y += h + gap
            if (i < blocks.size - 1) {
                canvas.drawLine(380f, y - gap / 2f, 620f, y - gap / 2f, strokePaint)
            }
        }

        drawFooter(canvas, vh, "Yaprağı çevirmek için dokun")
    }

    private fun blockHeight(body: StaticLayout, note: StaticLayout?): Float =
        80f + body.height + (if (note != null) 10f + note.height else 0f)

    private fun drawFooter(canvas: Canvas, vh: Float, text: String) {
        strokePaint.color = ink
        strokePaint.strokeWidth = 2f
        canvas.drawLine(60f, vh - 100f, VW - 60f, vh - 100f, strokePaint)
        txt(canvas, text, 500f, vh - 62f, 26f, inkSoft, serif, 800f)
    }

    // ---------- Saat, hava ikonu, vakit kutulari ----------

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float, now: LocalTime, withHands: Boolean = true) {
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

        if (withHands) drawHands(canvas, cx, cy, r, now)
    }

    private fun drawHands(canvas: Canvas, cx: Float, cy: Float, r: Float, now: LocalTime) {
        val minute = now.minute + now.second / 60f
        val hour = (now.hour % 12) + minute / 60f
        hand(canvas, cx, cy, hour * 30f, r * 0.5f, 7f)
        hand(canvas, cx, cy, minute * 6f, r * 0.76f, 4.5f)
        // Saniye kolu kirmizi, kisa bir kuyrukla (duvar saatlerindeki gibi)
        hand(canvas, cx, cy, now.second * 6f, r * 0.84f, 2.5f, SECOND_RED)
        hand(canvas, cx, cy, now.second * 6f + 180f, r * 0.22f, 3.5f, SECOND_RED)

        fillPaint.color = ink
        canvas.drawCircle(cx, cy, 5f, fillPaint)
        fillPaint.color = SECOND_RED
        canvas.drawCircle(cx, cy, 2.5f, fillPaint)
    }

    /**
     * Canli duvar kagidi icin: yaprak (kollar haric) bir kez bitmap'e cizilir, her saniye
     * bitmap'in ustune yalniz akrep/yelkovan/saniye kolu cizilir. draw(..., withHands=false)
     * ile AYNI donusumu kullanir, boylece kollar kadranin ustune tam oturur.
     */
    fun drawHandsOnly(canvas: Canvas, width: Int, height: Int, data: PageData, topInset: Int = 0) {
        val w = width.toFloat()
        val h = height.toFloat()
        val inset = topInset.toFloat()
        val s = kotlin.math.min(w / VW, (h - inset) / BASE_H)
        val offsetX = (w / s - VW) / 2f

        canvas.save()
        canvas.translate(0f, inset)
        canvas.scale(s, s)
        canvas.translate(offsetX, 0f)
        drawHands(canvas, 167f, CLOCK_Y, 58f, LocalTime.now(data.leftZone))
        drawHands(canvas, 833f, CLOCK_Y, 58f, LocalTime.now(data.rightZone))
        canvas.restore()
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, degrees: Float, length: Float, width: Float, color: Int = ink) {
        val a = Math.toRadians(degrees.toDouble())
        strokePaint.color = color
        strokePaint.strokeWidth = width
        strokePaint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(cx, cy, cx + sin(a).toFloat() * length, cy - cos(a).toFloat() * length, strokePaint)
        strokePaint.strokeCap = Paint.Cap.BUTT
    }

    /** WMO hava kodundan basit cizgi ikonu: gunes, bulut, yagmur, kar, sis, firtina. */
    private fun drawWeatherIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, code: Int) {
        if (code < 0) return
        when {
            code == 0 -> sun(canvas, cx, cy, size * 0.3f)
            code == 1 || code == 2 -> {
                sun(canvas, cx + size * 0.14f, cy - size * 0.12f, size * 0.2f)
                cloud(canvas, cx - size * 0.04f, cy + size * 0.1f, size * 0.8f)
            }
            code in 45..48 -> {
                cloud(canvas, cx, cy - size * 0.1f, size)
                strokePaint.color = inkSoft
                strokePaint.strokeWidth = 3f
                canvas.drawLine(cx - size * 0.4f, cy + size * 0.28f, cx + size * 0.4f, cy + size * 0.28f, strokePaint)
                canvas.drawLine(cx - size * 0.3f, cy + size * 0.42f, cx + size * 0.3f, cy + size * 0.42f, strokePaint)
            }
            code in 51..67 || code in 80..82 -> {
                cloud(canvas, cx, cy - size * 0.1f, size)
                strokePaint.color = ink
                strokePaint.strokeWidth = 3.5f
                for (i in -1..1) {
                    val x = cx + i * size * 0.26f
                    canvas.drawLine(x, cy + size * 0.28f, x - size * 0.06f, cy + size * 0.46f, strokePaint)
                }
            }
            code in 71..77 || code in 85..86 -> {
                cloud(canvas, cx, cy - size * 0.1f, size)
                fillPaint.color = ink
                for (i in -1..1) {
                    canvas.drawCircle(cx + i * size * 0.26f, cy + size * 0.38f, size * 0.06f, fillPaint)
                }
            }
            code in 95..99 -> {
                cloud(canvas, cx, cy - size * 0.1f, size)
                strokePaint.color = ink
                strokePaint.strokeWidth = 4f
                canvas.drawLine(cx + size * 0.06f, cy + size * 0.24f, cx - size * 0.1f, cy + size * 0.38f, strokePaint)
                canvas.drawLine(cx - size * 0.1f, cy + size * 0.38f, cx + size * 0.08f, cy + size * 0.38f, strokePaint)
                canvas.drawLine(cx + size * 0.08f, cy + size * 0.38f, cx - size * 0.06f, cy + size * 0.52f, strokePaint)
            }
            else -> cloud(canvas, cx, cy, size)
        }
    }

    private fun sun(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        fillPaint.color = ink
        canvas.drawCircle(cx, cy, r, fillPaint)
        strokePaint.color = ink
        strokePaint.strokeWidth = 3f
        for (i in 0 until 8) {
            val a = Math.toRadians(i * 45.0)
            val sx = sin(a).toFloat()
            val cs = cos(a).toFloat()
            canvas.drawLine(cx + sx * r * 1.4f, cy - cs * r * 1.4f, cx + sx * r * 1.9f, cy - cs * r * 1.9f, strokePaint)
        }
    }

    private fun cloud(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        fillPaint.color = inkSoft
        canvas.drawCircle(cx - 0.25f * s, cy, 0.22f * s, fillPaint)
        canvas.drawCircle(cx, cy - 0.12f * s, 0.28f * s, fillPaint)
        canvas.drawCircle(cx + 0.27f * s, cy + 0.02f * s, 0.2f * s, fillPaint)
        canvas.drawRect(cx - 0.25f * s, cy, cx + 0.27f * s, cy + 0.22f * s, fillPaint)
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
