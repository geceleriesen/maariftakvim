package com.geceleriesen.maariftakvim.domain

object DayLength {

    fun toMinutes(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    private fun length(sunrise: String?, sunset: String?): Int? {
        if (sunrise == null || sunset == null) return null
        val a = toMinutes(sunrise) ?: return null
        val b = toMinutes(sunset) ?: return null
        val d = b - a
        return if (d > 0) d else null
    }

    /** Ornek: "GÜN: 11 Sa. 40 Dk. (-3 Dk.)". Veri eksikse bos string doner. */
    fun describe(sunrise: String, sunset: String, prevSunrise: String?, prevSunset: String?): String {
        val today = length(sunrise, sunset) ?: return ""
        val base = "GÜN: ${today / 60} Sa. ${today % 60} Dk."
        val yesterday = length(prevSunrise, prevSunset) ?: return base
        val delta = today - yesterday
        if (delta == 0) return base
        val sign = if (delta > 0) "+" else "-"
        return "$base ($sign${kotlin.math.abs(delta)} Dk.)"
    }
}