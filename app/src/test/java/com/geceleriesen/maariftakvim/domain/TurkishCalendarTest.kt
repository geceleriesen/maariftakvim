package com.geceleriesen.maariftakvim.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TurkishCalendarTest {

    @Test
    fun ekim4_2026() {
        val i = TurkishCalendar.info(LocalDate.of(2026, 10, 4))
        assertEquals(4, i.gregorianDay)
        assertEquals("4 EKİM 2026 PAZAR", i.gregorianText)
        assertEquals("PAZAR", i.dayName)
        assertEquals("21 EYLÜL 1442", i.rumiText)
        assertEquals(277, i.dayOfYear)
        assertTrue(i.hijriText.contains("REBİÜLAHİR"))
        assertTrue(i.hijriText.endsWith("1448"))
    }

    @Test
    fun haziran22_2026_pazartesi() {
        val i = TurkishCalendar.info(LocalDate.of(2026, 6, 22))
        assertEquals("22 HAZİRAN 2026 PAZARTESİ", i.gregorianText)
        assertEquals("9 HAZİRAN 1442", i.rumiText)
    }

    @Test
    fun rumiYilBasi() {
        assertEquals("28 ŞUBAT 1441", TurkishCalendar.info(LocalDate.of(2026, 3, 13)).rumiText)
        assertEquals("1 MART 1442", TurkishCalendar.info(LocalDate.of(2026, 3, 14)).rumiText)
    }
}