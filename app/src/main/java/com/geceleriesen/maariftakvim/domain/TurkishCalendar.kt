package com.geceleriesen.maariftakvim.domain

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Locale

data class TurkishDateInfo(
    val gregorianDay: Int,
    val gregorianText: String,
    val dayName: String,
    val hijriText: String,
    val rumiText: String,
    val dayOfYear: Int,
    val daysLeftInYear: Int
)

object TurkishCalendar {

    private val TR: Locale = Locale.forLanguageTag("tr-TR")

    private val gregorianMonths = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    // DayOfWeek.value: 1 = Pazartesi ... 7 = Pazar
    private val dayNames = listOf(
        "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
    )

    private val hijriMonths = listOf(
        "Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir",
        "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce"
    )

    // Rumi yil Mart ile baslar
    private val rumiMonths = listOf(
        "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos",
        "Eylül", "Teşrinievvel", "Teşrinisani", "Kânunuevvel", "Kânunusani", "Şubat"
    )

    /**
     * Hicri tarih Umm al-Qura takviminden gelir; Diyanet ile bazen 1 gun fark olabilir.
     * Fark varsa hijriOffsetDays olarak -1 veya +1 verilir.
     */
    fun info(date: LocalDate, hijriOffsetDays: Long = 0): TurkishDateInfo {
        val dayName = dayNames[date.dayOfWeek.value - 1].uppercase(TR)
        val monthName = gregorianMonths[date.monthValue - 1].uppercase(TR)

        val hijri = HijrahDate.from(date.plusDays(hijriOffsetDays))
        val hijriDay = hijri.get(ChronoField.DAY_OF_MONTH)
        val hijriMonth = hijriMonths[hijri.get(ChronoField.MONTH_OF_YEAR) - 1].uppercase(TR)
        val hijriYear = hijri.get(ChronoField.YEAR)

        // Rumi takvim: Miladi - 13 gun (1900-03-14 ile 2100-02-28 arasinda gecerli)
        val julian = date.minusDays(13)
        val rumiYear = if (julian.monthValue >= 3) julian.year - 584 else julian.year - 585
        val rumiMonth = rumiMonths[(julian.monthValue + 9) % 12].uppercase(TR)

        return TurkishDateInfo(
            gregorianDay = date.dayOfMonth,
            gregorianText = "${date.dayOfMonth} $monthName ${date.year} $dayName",
            dayName = dayName,
            hijriText = "$hijriDay $hijriMonth $hijriYear",
            rumiText = "${julian.dayOfMonth} $rumiMonth $rumiYear",
            dayOfYear = date.dayOfYear,
            daysLeftInYear = date.lengthOfYear() - date.dayOfYear
        )
    }
}