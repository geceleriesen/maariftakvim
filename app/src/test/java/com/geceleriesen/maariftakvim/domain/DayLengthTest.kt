package com.geceleriesen.maariftakvim.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DayLengthTest {

    @Test
    fun gunKisaliyor() {
        assertEquals("GÜN: 11 Sa. 40 Dk. (-3 Dk.)", DayLength.describe("06:10", "17:50", "06:09", "17:52"))
    }

    @Test
    fun gunUzuyor() {
        assertEquals("GÜN: 14 Sa. 20 Dk. (+3 Dk.)", DayLength.describe("05:00", "19:20", "05:01", "19:18"))
    }

    @Test
    fun farkYok() {
        assertEquals("GÜN: 12 Sa. 0 Dk.", DayLength.describe("06:00", "18:00", "06:00", "18:00"))
    }

    @Test
    fun dunVerisiYok() {
        assertEquals("GÜN: 12 Sa. 0 Dk.", DayLength.describe("06:00", "18:00", null, null))
    }

    @Test
    fun eksikVeri() {
        assertEquals("", DayLength.describe("--:--", "18:00", null, null))
    }
}