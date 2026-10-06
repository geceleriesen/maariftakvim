package com.geceleriesen.maariftakvim.ui.page

import android.content.Context
import android.graphics.Canvas
import android.view.View

class CalendarPageView(context: Context) : View(context) {

    private val renderer = CalendarPageRenderer(FontLoader.numberFace(context))
    private val tick = Runnable { invalidate() }

    var data: PageData? = null
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = data ?: return
        renderer.draw(canvas, width, height, d)
        // Akrep, yelkovan ve saniye kolu her saniye yeniden cizilir
        removeCallbacks(tick)
        postDelayed(tick, 1_000L)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tick)
        super.onDetachedFromWindow()
    }
}
