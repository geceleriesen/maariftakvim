package com.geceleriesen.maariftakvim.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.ui.MainActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
        val dir = context.filesDir
        val settings = Settings(context)
        val c1 = settings.city1
        val c2 = settings.city2

        val (first, second) = coroutineScope {
            val a = async { WeatherPrayerService.fetchCityData(dir, c1.name, c1.lat, c1.lon) }
            val b = async { WeatherPrayerService.fetchCityData(dir, c2.name, c2.lat, c2.lon) }
            Pair(a.await(), b.await())
        }

        val today = CalendarRepository(context).getDay().copy(dayLengthInfo = first.dayLengthInfo)
        val openApp = Intent(context, MainActivity::class.java)

        provideContent {
            MaarifWidgetContent(today = today, soke = first, ankara = second, openApp = openApp)
        }
    }
}

@Composable
fun MaarifWidgetContent(today: CalendarDay, soke: CityData, ankara: CityData, openApp: Intent) {
    val paperColor = ColorProvider(Color(0xFFF5F0E1))
    val textColorPrimary = ColorProvider(Color(0xFF1A1A1A))
    val textColorSecondary = ColorProvider(Color(0xFF4A4A4A))

    val centerInfo = if (today.dayLengthInfo.isNotEmpty()) {
        today.dayLengthInfo
    } else {
        "${today.dayOfYear}. gün · ${today.daysLeftInYear} gün kaldı"
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(paperColor)
            .clickable(actionStartActivity(openApp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ust bilgi satiri: Hicri | gun bilgisi | Rumi
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = today.hijriDate, style = TextStyle(color = textColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(text = centerInfo, style = TextStyle(color = textColorSecondary, fontSize = 9.sp))
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(text = today.rumiDate, style = TextStyle(color = textColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Miladi tarih + gun adi
        Text(text = today.gregorianText, style = TextStyle(color = textColorPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold))

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Iki sehir + dev gun numarasi
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CityColumn(
                city = soke,
                primary = textColorPrimary,
                secondary = textColorSecondary,
                modifier = GlanceModifier.defaultWeight()
            )

            Text(
                text = today.dayNumber,
                style = TextStyle(color = textColorPrimary, fontSize = 60.sp, fontWeight = FontWeight.Bold)
            )

            CityColumn(
                city = ankara,
                primary = textColorPrimary,
                secondary = textColorSecondary,
                modifier = GlanceModifier.defaultWeight()
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(text = today.dayName, style = TextStyle(color = textColorPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold))

        Spacer(modifier = GlanceModifier.height(4.dp))

        if (today.folkCalendar.isNotEmpty()) {
            Text(text = "(${today.folkCalendar})", style = TextStyle(color = textColorSecondary, fontSize = 10.sp))
        }
        if (today.historyEvent.isNotEmpty()) {
            Text(text = today.historyEvent, style = TextStyle(color = textColorSecondary, fontSize = 10.sp))
        }

        if (today.quote.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            val author = if (today.quoteAuthor.isNotEmpty()) " — ${today.quoteAuthor}" else ""
            Text(
                text = "\"${today.quote}\"$author",
                style = TextStyle(color = textColorSecondary, fontSize = 9.sp)
            )
        }
    }
}

@Composable
private fun CityColumn(
    city: CityData,
    primary: ColorProvider,
    secondary: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "${city.cityName} ${city.temp}", style = TextStyle(color = primary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = GlanceModifier.height(2.dp))
        PrayerLine("İmsak", city.imsak, secondary)
        PrayerLine("Güneş", city.gunes, secondary)
        PrayerLine("Öğle", city.ogle, secondary)
        PrayerLine("İkindi", city.ikindi, secondary)
        PrayerLine("Akşam", city.aksam, secondary)
        PrayerLine("Yatsı", city.yatsi, secondary)
    }
}

@Composable
private fun PrayerLine(label: String, time: String, color: ColorProvider) {
    Text(text = "$label: $time", style = TextStyle(color = color, fontSize = 9.sp))
}

class MaarifWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MaarifWidget()
}