package com.geceleriesen.maariftakvim.network

import com.geceleriesen.maariftakvim.domain.DayLength
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class DayTimes(
    val imsak: String,
    val gunes: String,
    val ogle: String,
    val ikindi: String,
    val aksam: String,
    val yatsi: String,
    val sunset: String
)

data class CityData(
    val cityName: String,
    val temp: String,
    val imsak: String,
    val gunes: String,
    val ogle: String,
    val ikindi: String,
    val aksam: String,
    val yatsi: String,
    val dayLengthInfo: String = "",
    val weatherCode: Int = -1
)

object WeatherPrayerService {

    private val TR: Locale = Locale.forLanguageTag("tr-TR")
    private val TIME_REGEX = Regex("""\d{1,2}:\d{2}""")
    private const val NO_TIME = "--:--"

    // Aladhan yontem 13 = Diyanet (deneysel, hesaplanmis). Resmi cetvelden birkac dakika sapabilir.
    private const val METHOD = 13

    // Dakika ayari gerekirse virgulle ayrilmis 9 sayi:
    // imsak,fajr,sunrise,dhuhr,asr,maghrib,sunset,isha,midnight  (ornek: "0,0,0,1,0,0,0,0,0")
    // TUNE degistirirsen CACHE_VERSION'u da artir ki eski onbellek kullanilmasin.
    private const val TUNE = ""
    private const val CACHE_VERSION = 1

    suspend fun fetchCityData(
        dir: File,
        cityName: String,
        lat: Double,
        lon: Double,
        date: LocalDate = LocalDate.now()
    ): CityData = withContext(Dispatchers.IO) {
        val (temp, weatherCode) = try {
            fetchWeather(lat, lon)
        } catch (e: Exception) {
            Pair("--", -1)
        }
        val today = timesFor(dir, lat, lon, date)
        val yesterday = timesFor(dir, lat, lon, date.minusDays(1))
        val dayInfo = if (today != null) {
            DayLength.describe(today.gunes, today.sunset, yesterday?.gunes, yesterday?.sunset)
        } else {
            ""
        }

        CityData(
            cityName = cityName.uppercase(TR),
            temp = temp,
            imsak = today?.imsak ?: NO_TIME,
            gunes = today?.gunes ?: NO_TIME,
            ogle = today?.ogle ?: NO_TIME,
            ikindi = today?.ikindi ?: NO_TIME,
            aksam = today?.aksam ?: NO_TIME,
            yatsi = today?.yatsi ?: NO_TIME,
            dayLengthInfo = dayInfo,
            weatherCode = weatherCode
        )
    }

    // Sicaklik metni ve WMO hava kodu (ikon icin); kod okunamazsa -1
    private fun fetchWeather(lat: Double, lon: Double): Pair<String, Int> {
        val q = String.format(Locale.ROOT, "latitude=%.4f&longitude=%.4f&current_weather=true", lat, lon)
        val body = httpGet("https://api.open-meteo.com/v1/forecast?$q")
        val cw = JSONObject(body).getJSONObject("current_weather")
        val t = cw.getDouble("temperature")
        val code = cw.optInt("weathercode", cw.optInt("weather_code", -1))
        return Pair("${Math.round(t)}°C", code)
    }

    private fun timesFor(dir: File, lat: Double, lon: Double, date: LocalDate): DayTimes? {
        val month = loadMonth(dir, lat, lon, date.year, date.monthValue) ?: return null
        return month[date.dayOfMonth]
    }

    // Once diskteki aylik onbellege bakar; yoksa indirir ve kaydeder. Internet yoksa null doner.
    private fun loadMonth(dir: File, lat: Double, lon: Double, year: Int, month: Int): Map<Int, DayTimes>? {
        val file = File(dir, cacheName(lat, lon, year, month))
        if (file.exists()) {
            val cached = try {
                fromCompact(file.readText())
            } catch (e: Exception) {
                null
            }
            if (cached != null && cached.isNotEmpty()) return cached
        }

        val fresh = try {
            downloadMonth(lat, lon, year, month)
        } catch (e: Exception) {
            null
        }
        if (fresh == null) return null

        try {
            file.writeText(toCompact(fresh))
        } catch (e: Exception) {
            // Yazilamazsa sorun degil, sadece onbellek olmaz
        }
        return fresh
    }

    private fun cacheName(lat: Double, lon: Double, year: Int, month: Int): String =
        String.format(Locale.ROOT, "prayer_v%d_m%d_%.2f_%.2f_%04d-%02d.json", CACHE_VERSION, METHOD, lat, lon, year, month)

    private fun downloadMonth(lat: Double, lon: Double, year: Int, month: Int): Map<Int, DayTimes> {
        var q = String.format(Locale.ROOT, "latitude=%.4f&longitude=%.4f&method=%d", lat, lon, METHOD)
        if (TUNE.isNotEmpty()) q += "&tune=$TUNE"
        val body = httpGet("https://api.aladhan.com/v1/calendar/$year/$month?$q")

        val data = JSONObject(body).getJSONArray("data")
        val result = HashMap<Int, DayTimes>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val day = item.getJSONObject("date").getJSONObject("gregorian").getString("day").toInt()
            val t = item.getJSONObject("timings")
            result[day] = DayTimes(
                imsak = clean(t.getString("Imsak")),
                gunes = clean(t.getString("Sunrise")),
                ogle = clean(t.getString("Dhuhr")),
                ikindi = clean(t.getString("Asr")),
                aksam = clean(t.getString("Maghrib")),
                yatsi = clean(t.getString("Isha")),
                sunset = clean(t.getString("Sunset"))
            )
        }
        if (result.isEmpty()) throw IllegalStateException("bos takvim")
        return result
    }

    // API "06:03 (+03)" gibi donebilir; sadece SS:DD kismini al
    private fun clean(raw: String): String =
        TIME_REGEX.find(raw)?.value ?: throw IllegalArgumentException("saat okunamadi: $raw")

    private fun toCompact(month: Map<Int, DayTimes>): String {
        val obj = JSONObject()
        for ((day, t) in month) {
            val arr = JSONArray()
            arr.put(t.imsak)
            arr.put(t.gunes)
            arr.put(t.ogle)
            arr.put(t.ikindi)
            arr.put(t.aksam)
            arr.put(t.yatsi)
            arr.put(t.sunset)
            obj.put(day.toString(), arr)
        }
        return obj.toString()
    }

    private fun fromCompact(text: String): Map<Int, DayTimes> {
        val obj = JSONObject(text)
        val result = HashMap<Int, DayTimes>()
        for (key in obj.keys()) {
            val a = obj.getJSONArray(key)
            result[key.toInt()] = DayTimes(
                a.getString(0), a.getString(1), a.getString(2),
                a.getString(3), a.getString(4), a.getString(5), a.getString(6)
            )
        }
        return result
    }

    private fun httpGet(address: String): String {
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