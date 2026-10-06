package com.geceleriesen.maariftakvim.data

import android.content.Context
import java.time.ZoneId
import java.util.Locale

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

    /** 1. sehir yerine telefonun son bilinen konumunu kullan */
    var autoLocation: Boolean
        get() = sp.getBoolean("auto_location", false)
        set(value) {
            sp.edit().putBoolean("auto_location", value).apply()
        }

    /** Tam ekranda takvimdeki siradaki etkinligi goster */
    var showAgenda: Boolean
        get() = sp.getBoolean("show_agenda", false)
        set(value) {
            sp.edit().putBoolean("show_agenda", value).apply()
        }

    /** Takvim ekrani acikken telefonun ekrani kapanmasin */
    var keepScreenOn: Boolean
        get() = sp.getBoolean("keep_screen_on", true)
        set(value) {
            sp.edit().putBoolean("keep_screen_on", value).apply()
        }

    /** Kilit ekrani resmini her gece otomatik yenile */
    var lockDaily: Boolean
        get() = sp.getBoolean("lock_daily", false)
        set(value) {
            sp.edit().putBoolean("lock_daily", value).apply()
        }

    /** Ayni yapragi ana ekran duvar kagidina da bas. Varsayilan kapali. */
    var homeDaily: Boolean
        get() = sp.getBoolean("home_daily", false)
        set(value) {
            sp.edit().putBoolean("home_daily", value).apply()
        }

    // Konumdan bulunan yer adlari (Geocoder her seferinde sorulmasin)
    fun cachedPlace(lat: Double, lon: Double): String? = sp.getString(placeKey(lat, lon), null)

    fun cachePlace(lat: Double, lon: Double, name: String) {
        sp.edit().putString(placeKey(lat, lon), name).apply()
    }

    private fun placeKey(lat: Double, lon: Double): String =
        String.format(Locale.ROOT, "place_%.2f_%.2f", lat, lon)

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
