package com.geceleriesen.maariftakvim.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import android.graphics.Color
import com.geceleriesen.maariftakvim.data.CalendarRepository

class MaarifWidget : GlanceAppWidget() {
    override async fun provideGlance(context: Context, id: GlanceId) {
        val repo = CalendarRepository(context)
        val today = repo.getTodayData()

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = today.gregorianDate,
                    style = TextStyle(color = ColorProvider(Color.BLACK))
                )
                Text(
                    text = today.dayNumber,
                    style = TextStyle(color = ColorProvider(Color.BLACK))
                )
                Text(
                    text = today.dayName,
                    style = TextStyle(color = ColorProvider(Color.BLACK))
                )
                Text(
                    text = today.quote,
                    style = TextStyle(color = ColorProvider(Color.DKGRAY))
                )
            }
        }
    }
}

class MaarifWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MaarifWidget()
}
