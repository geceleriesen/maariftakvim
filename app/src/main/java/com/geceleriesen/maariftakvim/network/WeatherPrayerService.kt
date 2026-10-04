package com.geceleriesen.maariftakvim.network

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CityData(
    val cityName: String,
    val temp: String,
    val imsak: String,
    val gunes: String,
    val ogle: String,
    val ikindi: String,
    val aksam: String,
    val yatsı: String
)

object WeatherPrayerService {

    suspend fun fetchCityData(cityName: String, lat: Double, lon: Double): CityData = withContext(Dispatchers.IO) {
        try {
            // Open-Meteo Ücretsiz Hava Durumu API
            val weatherUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true")
            val conn = weatherUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val response = reader.readText()
            reader.close()

            val jsonObj = JSONObject(response)
            val currentWeather = jsonObj.getJSONObject("current_weather")
            val temp = "${currentWeather.getDouble("temperature").toInt()}°C"

            // Varsayılan/Örnek Vakitler (Arayüz Uyumu İçin)
            return@withContext CityData(
                cityName = cityName.uppercase(),
                temp = temp,
                imsak = "04:12",
                gunes = "05:48",
                ogle = "13:05",
                ikindi = "16:52",
                aksam = "20:21",
                yatsı = "21:50"
            )
        } catch (e: Exception) {
            // İnternet olmaması durumunda fallback veriler
            return@withContext CityData(
                cityName = cityName.uppercase(),
                temp = "22°C",
                imsak = "04:12",
                gunes = "05:48",
                ogle = "13:05",
                ikindi = "16:52",
                aksam = "20:21",
                yatsı = "21:50"
            )
        }
    }
}