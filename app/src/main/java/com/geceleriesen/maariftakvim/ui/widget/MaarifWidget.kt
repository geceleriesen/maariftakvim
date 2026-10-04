package com.geceleriesen.maariftakvim.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.geceleriesen.maariftakvim.data.CalendarDay
import com.geceleriesen.maariftakvim.data.CalendarRepository
import com.geceleriesen.maariftakvim.network.CityData
import com.geceleriesen.maariftakvim.network.WeatherPrayerService

class MaarifWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = CalendarRepository(context)
        val today = repo.getTodayData()

        // Söke ve Ankara için canlı hava durumu verisi çekiliyor
        val sokeData = WeatherPrayerService.fetchCityData("Söke", 37.75, 27.40)
        val ankaraData = WeatherPrayerService.fetchCityData("Ankara", 39.93, 32.85)

        provideContent {
            MaarifWidgetContent(today = today, soke = sokeData, ankara = ankaraData)
        }
    }
}

@Composable
fun MaarifWidgetContent(today: CalendarDay, soke: CityData, ankara: CityData) {
    val paperColor = ColorProvider(Color(0xFFF5F0E1))
    val textColorPrimary = ColorProvider(Color(0xFF1A1A1A))
    val textColorSecondary = ColorProvider(Color(0xFF4A4A4A))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(paperColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Bilgi Satırı
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = today.hijriDate, style = TextStyle(color = textColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(text = today.dayLengtheningInfo, style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(text = today.rumiDate, style = TextStyle(color = textColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Miladi Tarih
        Text(text = today.gregorianDate, style = TextStyle(color = textColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold))

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Çift Sütun Vakitler & Hava Durumu + Büyük Gün Numarası
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Sütun: Söke
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "${soke.cityName} ${soke.temp}", style = TextStyle(color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(text = "Güneş: ${soke.gunes}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Öğle: ${soke.ogle}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "İkindi: ${soke.ikindi}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Akşam: ${soke.aksam}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Yatsı: ${soke.yatsı}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
            }

            // Orta: Dev Gün Numarası
            Text(
                text = today.dayNumber,
                style = TextStyle(color = textColorPrimary, fontSize = 60.sp, fontWeight = FontWeight.Bold)
            )

            // Sağ Sütun: Ankara
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "${ankara.cityName} ${ankara.temp}", style = TextStyle(color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(text = "Güneş: ${ankara.gunes}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Öğle: ${ankara.ogle}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "İkindi: ${ankara.ikindi}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Akşam: ${ankara.aksam}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
                Text(text = "Yatsı: ${ankara.yatsı}", style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Gün İsmi
        Text(text = today.dayName, style = TextStyle(color = textColorPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold))

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Halk Takvimi ve Olaylar
        if (today.folkCalendar.isNotEmpty()) {
            Text(text = "(${today.folkCalendar})", style = TextStyle(color = textColorSecondary, fontSize = 10.sp))
        }
        if (today.historyEvent.isNotEmpty()) {
            Text(text = today.historyEvent, style = TextStyle(color = textColorSecondary, fontSize = 10.sp))
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Günün Sözü
        Text(
            text = "\"${today.quote}\" — ${today.quoteAuthor}",
            style = TextStyle(color = textColorSecondary, fontSize = 9.sp)
        )
    }
}

class MaarifWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MaarifWidget()
}