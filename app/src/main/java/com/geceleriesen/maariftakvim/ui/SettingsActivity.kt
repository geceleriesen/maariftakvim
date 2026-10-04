package com.geceleriesen.maariftakvim.ui

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.geceleriesen.maariftakvim.data.CityPref
import com.geceleriesen.maariftakvim.data.Settings
import com.geceleriesen.maariftakvim.network.CityResult
import com.geceleriesen.maariftakvim.network.CitySearch

class SettingsActivity : Activity() {

    private val ink = 0xFF2B2118.toInt()
    private lateinit var settings: Settings
    private lateinit var city1Text: TextView
    private lateinit var city2Text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)

        val dp = resources.displayMetrics.density
        val pad = (20 * dp).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(pad, pad * 2, pad, pad)
        root.setBackgroundColor(0xFFF1E6BE.toInt())

        root.addView(textView("Ayarlar", 28f, true))
        root.addView(textView("Takvimde iki şehrin vakitleri, saati ve havası görünür.", 15f, false, pad / 2))

        root.addView(textView("1. şehir (sol sütun)", 14f, false, pad))
        city1Text = textView("", 24f, true)
        root.addView(city1Text)
        root.addView(button("Şehri değiştir") { pickCity(1) })

        root.addView(textView("2. şehir (sağ sütun)", 14f, false, pad))
        city2Text = textView("", 24f, true)
        root.addView(city2Text)
        root.addView(button("Şehri değiştir") { pickCity(2) })

        root.addView(textView("İpucu: Takvim ekranındayken ekrana uzun basarak buraya dönebilirsin.", 13f, false, pad * 2))

        setContentView(root)
        refreshRows()
    }

    private fun refreshRows() {
        city1Text.text = settings.city1.name
        city2Text.text = settings.city2.name
    }

    private fun textView(text: String, sizeSp: Float, bold: Boolean, top: Int = 0): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = sizeSp
        tv.setTextColor(ink)
        tv.typeface = if (bold) Typeface.create(Typeface.SERIF, Typeface.BOLD) else Typeface.SERIF
        tv.setPadding(0, top, 0, 0)
        return tv
    }

    private fun button(label: String, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.setOnClickListener { onClick() }
        return b
    }

    private fun pickCity(slot: Int) {
        val input = EditText(this)
        input.hint = "Şehir adı (örn. İzmir, Berlin)"
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setSingleLine()

        AlertDialog.Builder(this)
            .setTitle("Şehir ara")
            .setView(input)
            .setPositiveButton("Ara") { _, _ -> runSearch(slot, input.text.toString()) }
            .setNegativeButton("Vazgeç", null)
            .show()
    }

    private fun runSearch(slot: Int, query: String) {
        if (query.trim().length < 2) {
            Toast.makeText(this, "En az 2 harf yaz.", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            val results = try {
                CitySearch.search(query)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                if (results == null) {
                    Toast.makeText(this, "Arama başarısız, internet bağlantısını kontrol et.", Toast.LENGTH_LONG).show()
                } else if (results.isEmpty()) {
                    Toast.makeText(this, "Sonuç bulunamadı.", Toast.LENGTH_LONG).show()
                } else {
                    showResults(slot, results)
                }
            }
        }.start()
    }

    private fun showResults(slot: Int, results: List<CityResult>) {
        val labels = results.map { it.label() }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Birini seç")
            .setItems(labels) { _, which -> saveCity(slot, results[which]) }
            .setNegativeButton("Vazgeç", null)
            .show()
    }

    private fun saveCity(slot: Int, r: CityResult) {
        val zone = if (r.zone.isNotEmpty()) r.zone else "Europe/Istanbul"
        val pref = CityPref(r.name, r.lat, r.lon, zone)
        if (slot == 1) settings.city1 = pref else settings.city2 = pref
        refreshRows()
    }
}