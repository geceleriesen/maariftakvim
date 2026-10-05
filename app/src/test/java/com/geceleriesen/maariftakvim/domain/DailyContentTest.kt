package com.geceleriesen.maariftakvim.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyContentTest {

    @Test
    fun ayniGunAyniIcerik() {
        val d = LocalDate.of(2026, 10, 4)
        assertEquals(DailyContent.forDate(d), DailyContent.forDate(d))
    }

    @Test
    fun mevsimler() {
        assertEquals(DailyContent.Season.KIS, DailyContent.seasonOf(LocalDate.of(2026, 1, 15)))
        assertEquals(DailyContent.Season.KIS, DailyContent.seasonOf(LocalDate.of(2026, 12, 31)))
        assertEquals(DailyContent.Season.ILKBAHAR, DailyContent.seasonOf(LocalDate.of(2026, 4, 1)))
        assertEquals(DailyContent.Season.YAZ, DailyContent.seasonOf(LocalDate.of(2026, 7, 1)))
        assertEquals(DailyContent.Season.SONBAHAR, DailyContent.seasonOf(LocalDate.of(2026, 10, 4)))
    }

    @Test
    fun yilBoyuHerGunDoluVeTutarli() {
        var date = LocalDate.of(2028, 1, 1) // artik yil
        repeat(366) {
            val i = DailyContent.forDate(date)
            assertTrue("menu bos: $date", i.menu.isNotEmpty())
            assertEquals("kiz isimleri: $date", 3, i.girlNames.split(", ").distinct().size)
            assertEquals("erkek isimleri: $date", 3, i.boyNames.split(", ").distinct().size)
            val hasRiddle = i.riddle.isNotEmpty()
            val hasJoke = i.joke.isNotEmpty()
            assertTrue("bilmece ya da fikra olmali: $date", hasRiddle != hasJoke)
            if (hasRiddle) assertTrue("cevap yok: $date", i.riddleAnswer.isNotEmpty())
            date = date.plusDays(1)
        }
    }

    @Test
    fun ardisikGunlerFarkliIsimler() {
        val a = DailyContent.forDate(LocalDate.of(2026, 10, 4))
        val b = DailyContent.forDate(LocalDate.of(2026, 10, 5))
        assertNotEquals(a.girlNames, b.girlNames)
        assertNotEquals(a.boyNames, b.boyNames)
    }
}
