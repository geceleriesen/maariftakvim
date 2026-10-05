package com.geceleriesen.maariftakvim.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** Telefonun takviminden bugunun siradaki etkinligini okur. Veri telefondan cikmaz. */
class AgendaRepository(private val context: Context) {

    fun hasPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    /** Ornek: "17:30 - Haftalik Proje Incelemesi". Izin yoksa ya da etkinlik yoksa bos doner. */
    fun nextEventText(now: Long = System.currentTimeMillis()): String {
        if (!hasPermission()) return ""

        val zone = ZoneId.systemDefault()
        val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, endOfDay)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.ALL_DAY
        )

        try {
            val cursor = context.contentResolver.query(
                builder.build(), projection, null, null,
                CalendarContract.Instances.BEGIN + " ASC"
            )
            if (cursor != null) {
                cursor.use { c ->
                    while (c.moveToNext()) {
                        val title = c.getString(0) ?: ""
                        if (title.isBlank()) continue
                        val begin = c.getLong(1)
                        val allDay = c.getInt(2) == 1
                        val time = if (allDay) {
                            "Gün boyu"
                        } else {
                            val t = Instant.ofEpochMilli(begin).atZone(zone).toLocalTime()
                            String.format(Locale.ROOT, "%02d:%02d", t.hour, t.minute)
                        }
                        return "$time - ${title.trim()}"
                    }
                }
            }
        } catch (e: Exception) {
            // Okunamazsa ajanda satiri gosterilmez
        }
        return ""
    }
}
