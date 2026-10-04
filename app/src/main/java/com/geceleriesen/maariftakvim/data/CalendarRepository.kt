package com.geceleriesen.maariftakvim.data

import android.content.Context
import org.json.JSONArray

class CalendarRepository(private val context: Context) {

    fun getTodayData(): CalendarDay {
        return try {
            val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val obj = jsonArray.getJSONObject(0)

            CalendarDay(
                id = obj.optInt("id", 1),
                gregorianDate = obj.optString("gregorianDate", "22 HAZİRAN"),
                hijriDate = obj.optString("hijriDate", "16 ZİLHİCCE 1447"),
                rumiDate = obj.optString("rumiDate", "9 HAZİRAN 1442"),
                dayNumber = obj.optString("dayNumber", "22"),
                dayName = obj.optString("dayName", "PAZAR"),
                dayLengtheningInfo = obj.optString("dayLengtheningInfo", "GÜN: 15 Sa. 12 Dk."),
                quote = obj.optString("quote", "Bilmeyen ve bilmediğini bilen çocuktur, ona öğretin."),
                quoteAuthor = obj.optString("quoteAuthor", "Koyunbaba"),
                folkCalendar = obj.optString("folkCalendar", "Yaz Başlangıcı"),
                historyEvent = obj.optString("historyEvent", "1919: Amasya Genelgesi yayımlandı."),
                recipe = obj.optString("recipe", "Taze Fasulye"),
                dayOfYear = obj.optString("dayOfYear", "173. Gün"),
                backSide = BackSideContent(
                    title = "Günün Yemek Tarifi",
                    text = obj.optString("recipe", "Taze Fasulye")
                )
            )
        } catch (e: Exception) {
            CalendarDay()
        }
    }
}