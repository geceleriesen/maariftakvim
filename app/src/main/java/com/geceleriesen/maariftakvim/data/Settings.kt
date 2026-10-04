package com.geceleriesen.maariftakvim.data

import android.content.Context
import java.time.ZoneId

data class CityPref(val name: String, val lat: Double, val lon: Double, val zone: String) {
    fun zoneId(): ZoneId = try {
        ZoneId.of(zone)
    } catch (e: Exception) {
        ZoneId.systemDefault()
    }
}

class Settings(context: Context) {

    private val sp = context.getSharedPreferences("maarif_settings", Context.MODE_PRIVATE)

    var city1: CityPref
        get() = read("c1", DEFAULT1)
        set(value) = write("c1", value)

    var city2: CityPref
        get() = read("c2", DEFAULT2)
        set(value) = write("c2", value)

    var firstRunDone: Boolean
        get() = sp.getBoolean("first_run_done", false)
        set(value) {
            sp.edit().putBoolean("first_run_done", value).apply()
        }

    private fun read(key: String, def: CityPref): CityPref {
        val name = sp.getString("${key}_name", null) ?: return def
        val lat = sp.getString("${key}_lat", null)?.toDoubleOrNull() ?: return def
        val lon = sp.getString("${key}_lon", null)?.toDoubleOrNull() ?: return def
        val zone = sp.getString("${key}_zone", null) ?: return def
        return CityPref(name, lat, lon, zone)
    }

    private fun write(key: String, c: CityPref) {
        sp.edit()
            .putString("${key}_name", c.name)
            .putString("${key}_lat", c.lat.toString())
            .putString("${key}_lon", c.lon.toString())
            .putString("${key}_zone", c.zone)
            .apply()
    }

    companion object {
        val DEFAULT1 = CityPref("Söke", 37.75, 27.40, "Europe/Istanbul")
        val DEFAULT2 = CityPref("Ankara", 39.93, 32.85, "Europe/Istanbul")
    }
}