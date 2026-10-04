package com.geceleriesen.maariftakvim.ui.widget

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.geceleriesen.maariftakvim.data.CalendarRepository

class MaarifWidget : GlanceAppWidget() {
    override async fun provideGlance(context: Context, id: GlanceId) {
        val repo = CalendarRepository(context)
        val today = repo.getTodayData()

        provideContent {
            MaarifWidgetContent(today = today)
        }
    }
}

@Composable
fun MaarifWidgetContent(today: com.geceleriesen.maariftakvim.data.CalendarDay) {
    // Nostaljik Krem / Sarımtırak Kağıt Doku Arka Planı
    val paperColor = ColorProvider(Color.parseColor("#F5F0E1"))
    val textColorPrimary = ColorProvider(Color.parseColor("#1A1A1A"))
    val textColorSecondary = ColorProvider(Color.parseColor("#4A4A4A"))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(paperColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ÜST BÖLÜM: 3 FARKLI TAKVİM VE GÜNÜN UZAMASI ---
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = today.hijriDate,
                    style = TextStyle(color = textColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = today.dayLengtheningInfo,
                    style = TextStyle(color = textColorSecondary, fontSize = 9.sp)
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = today.rumiDate,
                    style = TextStyle(color = textColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // --- MİLADİ TARİH BAŞLIĞI ---
        Text(
            text = today.gregorianDate,
            style = TextStyle(color = textColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        // --- ORTA BÖLÜM: ÇİFT ŞEHİRLİ SÜTUNLAR VE DEV GÜN NUMARASI ---
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL SÜTUN (Söke / GPS Otomatik Konum)
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "SÖKE 20°C", style = TextStyle(color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(text = "Güneş: 07:03", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Öğle: 12:55", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "İkindi: 16:09", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Akşam: 18:37", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Yatsı: 19:53", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "İmsak: 05:35", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
            }

            // MERKEZ (Dev Gün Numarası)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = today.dayNumber,
                    style = TextStyle(color = textColorPrimary, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                )
            }

            // SAĞ SÜTUN (Ankara / Seçili 2. Şehir)
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "ANKARA 18°C", style = TextStyle(color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(text = "Güneş: 06:48", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Öğle: 12:40", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "İkindi: 15:54", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Akşam: 18:22", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Yatsı: 19:38", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "İmsak: 05:19", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // --- BÜYÜK GÜN İSMİ ---
        Text(
            text = today.dayName,
            style = TextStyle(color = textColorPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        // --- ALT BÖLÜM: HALK TAKVİMİ & TARİHTE BUGÜN ---
        if (today.folkCalendar.isNotEmpty()) {
            Text(
                text = "(${today.folkCalendar})",
                style = TextStyle(color = textColorSecondary, fontSize = 11.sp)
            )
        }

        if (today.historyEvent.isNotEmpty()) {
            Text(
                text = "(${today.historyEvent})",
                style = TextStyle(color = textColorSecondary, fontSize = 10.sp)
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // --- AJANDA SATIRI ---
        Text(
            text = "[AJANDA]: 17:30 - Haftalık Proje İncelemesi",
            style = TextStyle(color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        // --- GÜNÜN SÖZÜ / VECİZE ---
        Text(
            text = "\"${today.quote}\" — ${today.quoteAuthor}",
            style = TextStyle(color = textColorSecondary, fontSize = 10.sp)
        )
    }
}

class MaarifWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MaarifWidget()
}
