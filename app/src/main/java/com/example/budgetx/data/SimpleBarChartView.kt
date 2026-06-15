package com.example.budgetx

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: Map<String, Float> = emptyMap()

    private val barPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
        isAntiAlias = true
    }

    fun setData(newData: Map<String, Float>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val barWidth = width / (data.size * 2f)
        val maxValue = data.values.maxOrNull() ?: 1f

        var x = barWidth

        data.forEach { (label, value) ->
            val barHeight = (value / maxValue) * (height * 0.7f)

            val left = x
            val top = height - barHeight
            val right = x + barWidth
            val bottom = height.toFloat()

            canvas.drawRect(left, top, right, bottom, barPaint)
            canvas.drawText(label, left, top - 10, textPaint)

            x += barWidth * 2
        }
    }
}