package com.geceleriesen.maariftakvim.network

import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class CityData(
    val cityName: String,
    val temp: String,
    val imsak: String,
    val gunes: String,
    val ogle: String,
    val ikindi: String,
    val aksam: String,
    val yatsi: String
)

object WeatherPrayerService {

    private val TR: Locale = Locale.forLanguageTag("tr-TR")
    private const val NO_TIME = "--:--"

    // Namaz vakitleri Adim 3'te gercek kaynaktan gelecek; o zamana kadar bos gosterilir.
    suspend fun fetchCityData(cityName: String, lat: Double, lon: Double): CityData =
        withContext(Dispatchers.IO) {
            val temp = try {
                fetchTemperature(lat, lon)
            } catch (e: Exception) {
                "--"
            }
            CityData(
                cityName = cityName.uppercase(TR),
                temp = temp,
                imsak = NO_TIME,
                gunes = NO_TIME,
                ogle = NO_TIME,
                ikindi = NO_TIME,
                aksam = NO_TIME,
                yatsi = NO_TIME
            )
        }

    private fun fetchTemperature(lat: Double, lon: Double): String {
        val url = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val t = JSONObject(response).getJSONObject("current_weather").getDouble("temperature")
            return "${Math.round(t)}°C"
        } finally {
            conn.disconnect()
        }
    }
}