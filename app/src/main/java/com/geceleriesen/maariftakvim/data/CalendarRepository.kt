package com.geceleriesen.maariftakvim.data

import android.content.Context
import com.geceleriesen.maariftakvim.domain.DailyContent
import com.geceleriesen.maariftakvim.domain.TurkishCalendar
import java.time.LocalDate
import java.util.Locale
import org.json.JSONObject

class CalendarRepository(private val context: Context) {

    fun getDay(date: LocalDate = LocalDate.now()): CalendarDay {
        val info = TurkishCalendar.info(date)
        val daily = DailyContent.forDate(date)
        val x = loadExtras(date)

        fun str(key: String): String = x?.optString(key, "").orEmpty()

        // data.json'da arka yaprak alanlari varsa hesaplanan icerigin ustune yazar
        val ownRiddle = str("riddle")
        val ownJoke = str("joke")
        val riddle: String
        val riddleAnswer: String
        val joke: String
        if (ownRiddle.isNotEmpty()) {
            riddle = ownRiddle
            riddleAnswer = str("riddleAnswer")
            joke = ""
        } else if (ownJoke.isNotEmpty()) {
            riddle = ""
            riddleAnswer = ""
            joke = ownJoke
        } else {
            riddle = daily.riddle
            riddleAnswer = daily.riddleAnswer
            joke = daily.joke
        }

        return CalendarDay(
            gregorianText = info.gregorianText,
            dayNumber = info.gregorianDay.toString(),
            dayName = info.dayName,
            hijriDate = info.hijriText,
            rumiDate = info.rumiText,
            dayOfYear = info.dayOfYear,
            daysLeftInYear = info.daysLeftInYear,
            quote = str("quote"),
            quoteAuthor = str("quoteAuthor"),
            folkCalendar = str("folkCalendar"),
            historyEvent = str("historyEvent"),
            menu = str("menu").ifEmpty { daily.menu },
            girlNames = str("girlNames").ifEmpty { daily.girlNames },
            boyNames = str("boyNames").ifEmpty { daily.boyNames },
            riddle = riddle,
            riddleAnswer = riddleAnswer,
            joke = joke
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
