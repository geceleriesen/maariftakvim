package com.geceleriesen.maariftakvim.data

import android.content.Context
import com.geceleriesen.maariftakvim.domain.TurkishCalendar
import java.time.LocalDate
import java.util.Locale
import org.json.JSONObject

class CalendarRepository(private val context: Context) {

    fun getDay(date: LocalDate = LocalDate.now()): CalendarDay {
        val info = TurkishCalendar.info(date)
        val extras = loadExtras(date)

        return CalendarDay(
            gregorianText = info.gregorianText,
            dayNumber = info.gregorianDay.toString(),
            dayName = info.dayName,
            hijriDate = info.hijriText,
            rumiDate = info.rumiText,
            dayOfYear = info.dayOfYear,
            daysLeftInYear = info.daysLeftInYear,
            quote = extras?.optString("quote", "").orEmpty(),
            quoteAuthor = extras?.optString("quoteAuthor", "").orEmpty(),
            folkCalendar = extras?.optString("folkCalendar", "").orEmpty(),
            historyEvent = extras?.optString("historyEvent", "").orEmpty(),
            recipe = extras?.optString("recipe", "").orEmpty()
        )
    }

    private fun loadExtras(date: LocalDate): JSONObject? {
        return try {
            val text = context.assets.open("data.json").bufferedReader().use { it.readText() }
            val key = String.format(Locale.ROOT, "%02d-%02d", date.monthValue, date.dayOfMonth)
            JSONObject(text).optJSONObject(key)
        } catch (e: Exception) {
            null
        }
    }
}