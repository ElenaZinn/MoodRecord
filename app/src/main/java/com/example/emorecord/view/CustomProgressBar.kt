package com.example.emorecord.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.example.emorecord.R

// 自定义进度条样式
class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sadColor = ContextCompat.getColor(context, R.color.sad_blue_light)
    private val happyColor = ContextCompat.getColor(context, R.color.happy_pink_light)

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val progress = progress / 100f

        // Draw sad portion
        paint.color = sadColor
        canvas.drawRect(0f, 0f, width * progress, height, paint)

        // Draw happy portion
        paint.color = happyColor
        canvas.drawRect(width * progress, 0f, width, height, paint)
    }
}