package com.geceleriesen.maariftakvim.data

data class CalendarDay(
    val gregorianText: String = "",
    val dayNumber: String = "",
    val dayName: String = "",
    val hijriDate: String = "",
    val rumiDate: String = "",
    val dayOfYear: Int = 0,
    val daysLeftInYear: Int = 0,
    val dayLengthInfo: String = "",
    val quote: String = "",
    val quoteAuthor: String = "",
    val folkCalendar: String = "",
    val historyEvent: String = "",
    val recipe: String = ""
)