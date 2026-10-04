package com.geceleriesen.maariftakvim.data

data class CalendarDay(
    val dayOfYear: Int,
    val gregorianDate: String,
    val hijriDate: String,
    val rumiDate: String,
    val dayNumber: String,
    val dayName: String,
    val dayLengtheningInfo: String,
    val folkCalendar: String,
    val historyEvent: String,
    val quote: String,
    val quoteAuthor: String,
    val backSide: BackSideContent
)

data class BackSideContent(
    val girlName: String,
    val boyName: String,
    val recipe: String,
    val riddle: String,
    val riddleAnswer: String,
    val joke: String
)
