package com.geceleriesen.maariftakvim.ui.page

import android.content.Context
import android.graphics.Typeface

/** assets/fonts altindaki ozel yazi tipini yukler. Yuklenemezse null doner (sistem fontuna dusulur). */
object FontLoader {

    // Anton: SIL Open Font License 1.1 (assets/fonts/OFL.txt)
    private const val NUMBER_FONT = "fonts/Anton-Regular.ttf"

    fun numberFace(context: Context): Typeface? = try {
        Typeface.createFromAsset(context.assets, NUMBER_FONT)
    } catch (e: Exception) {
        null
    }
}
