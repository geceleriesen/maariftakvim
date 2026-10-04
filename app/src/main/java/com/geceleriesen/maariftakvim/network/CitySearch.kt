package com.geceleriesen.maariftakvim.network

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

data class CityResult(
    val name: String,
    val admin: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val zone: String
) {
    fun label(): String =
        listOf(name, admin, country).filter { it.isNotEmpty() }.distinct().joinToString(", ")
}

object CitySearch {

    // En az 2 harf gerekir; sonuc yoksa bos liste doner.
    fun search(query: String): List<CityResult> {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val body = get("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=8&language=tr&format=json")
        val arr = JSONObject(body).optJSONArray("results") ?: return emptyList()

        val out = ArrayList<CityResult>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                CityResult(
                    name = o.optString("name", ""),
                    admin = o.optString("admin1", ""),
                    country = o.optString("country", ""),
                    lat = o.getDouble("latitude"),
                    lon = o.getDouble("longitude"),
                    zone = o.optString("timezone", "")
                )
            )
        }
        return out
    }

    private fun get(address: String): String {
        val conn = URL(address).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}