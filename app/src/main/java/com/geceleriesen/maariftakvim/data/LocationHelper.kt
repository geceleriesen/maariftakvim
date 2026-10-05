package com.geceleriesen.maariftakvim.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import java.time.ZoneId
import java.util.Locale

object LocationHelper {

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Telefonun bildigi en taze konum; izin ya da konum yoksa null. */
    fun lastKnown(context: Context): Location? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        var best: Location? = null
        val providers = try {
            lm.getProviders(true)
        } catch (e: Exception) {
            emptyList<String>()
        }
        for (p in providers) {
            val l = try {
                lm.getLastKnownLocation(p)
            } catch (e: SecurityException) {
                null
            }
            if (l == null) continue
            val current = best
            if (current == null || l.time > current.time) best = l
        }
        return best
    }

    /** Ag gerektirebilir: yalnizca arka plan is parcaciginda cagir. */
    fun placeName(context: Context, settings: Settings, lat: Double, lon: Double): String {
        val cached = settings.cachedPlace(lat, lon)
        if (cached != null) return cached

        val found: String? = try {
            if (Geocoder.isPresent()) {
                val list = Geocoder(context, Locale.forLanguageTag("tr-TR")).getFromLocation(lat, lon, 1)
                val a = if (list != null && list.isNotEmpty()) list[0] else null
                a?.locality ?: a?.subAdminArea ?: a?.adminArea
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        if (found == null) return "Konumum"
        settings.cachePlace(lat, lon, found)
        return found
    }
}

/** 1. sehri belirler: otomatik konum aciksa ve konum biliniyorsa o, degilse kayitli sehir. */
object CityResolver {

    /** Ag gerektirebilir: yalnizca arka plan is parcaciginda cagir. */
    fun city1(context: Context): CityPref {
        val settings = Settings(context)
        if (settings.autoLocation) {
            val loc = LocationHelper.lastKnown(context)
            if (loc != null) {
                val name = LocationHelper.placeName(context, settings, loc.latitude, loc.longitude)
                return CityPref(name, loc.latitude, loc.longitude, ZoneId.systemDefault().id)
            }
        }
        return settings.city1
    }
}
