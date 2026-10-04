package com.geceleriesen.maariftakvim.data

data class BackSideContent(
    val title: String = "",
    val text: String = ""
)

data class CalendarDay(
    val id: Int = 1,
    val gregorianDate: String = "22 HAZİRAN",
    val hijriDate: String = "16 ZİLHİCCE 1447",
    val rumiDate: String = "9 HAZİRAN 1442",
    val dayNumber: String = "22",
    val dayName: String = "PAZAR",
    val dayLengtheningInfo: String = "GÜN: 15 Sa. 12 Dk.",
    val quote: String = "",
    val quoteAuthor: String = "",
    val folkCalendar: String = "",
    val historyEvent: String = "",
    val recipe: String = "",
    val dayOfYear: String = "",
    val backSide: BackSideContent = BackSideContent()
)