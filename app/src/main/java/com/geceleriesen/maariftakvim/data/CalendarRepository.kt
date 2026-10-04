package com.geceleriesen.maariftakvim.data

import android.content.Context
import org.json.JSONArray
import java.util.Calendar

class CalendarRepository(private val context: Context) {

    fun getTodayData(): CalendarDay {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.optInt("day_of_year") == dayOfYear) {
                val backObj = obj.getJSONObject("back_side")
                val namesObj = backObj.getJSONObject("names")
                
                return CalendarDay(
                    dayOfYear = obj.getInt("day_of_year"),
                    gregorianDate = obj.optString("gregorian_date", "4 EKİM 2026 MİLADİ"),
                    hijriDate = obj.optString("hijri_date", "1448 HİCRİ REBİÜLEVVEL 22"),
                    rumiDate = obj.optString("rumi_date", "1442 RUMİ EYLÜL 21"),
                    dayNumber = obj.optString("day_number", "22"),
                    dayName = obj.optString("day_name", "PAZAR"),
                    dayLengtheningInfo = obj.optString("day_lengthening", "Günün uzaması 3 dk"),
                    folkCalendar = obj.optString("folk_calendar", ""),
                    historyEvent = obj.optString("history_event", ""),
                    quote = obj.getString("quote"),
                    quoteAuthor = obj.getString("quote_author"),
                    backSide = BackSideContent(
                        girlName = namesObj.optString("girl", "-"),
                        boyName = namesObj.optString("boy", "-"),
                        recipe = backObj.optString("recipe", "-"),
                        riddle = backObj.optString("riddle", "-"),
                        riddleAnswer = backObj.optString("riddle_answer", "-"),
                        joke = backObj.optString("joke", "-")
                    )
                )
            }
        }
        
        return CalendarDay(
            dayOfYear = dayOfYear,
            gregorianDate = "4 EKİM 2026 MİLADİ",
            hijriDate = "1448 HİCRİ 22",
            rumiDate = "1442 RUMİ 21",
            dayNumber = "22",
            dayName = "PAZAR",
            dayLengtheningInfo = "",
            folkCalendar = "Fırtına",
            historyEvent = "",
            quote = "Kişinin aklı, sorduğu sorulardan anlaşılır.",
            quoteAuthor = "Hz. Ömer (r.a.)",
            backSide = BackSideContent("Defne", "Rüzgar", "Karnıyarık", "Çarşıdan aldım bir tane...", "Nar", "Fıkra")
        )
    }
}
