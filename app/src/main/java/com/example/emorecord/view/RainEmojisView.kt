package com.example.emorecord.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.emorecord.R
import java.util.Random

class RainEmojisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val emojis = mutableListOf<EmojiParticle>()
    private val random = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var emojiDrawable: Drawable? = null
    private var isRaining = false

    init {
        emojiDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mood_sad)
    }

    private data class EmojiParticle(
        var x: Float,
        var y: Float,
        var rotation: Float,
        var speed: Float,
        var size: Float
    )

    fun startRaining(showSad: Boolean? = false, showHappy: Boolean? = false) {
        isRaining = true
        emojiDrawable = if (showSad== true )ContextCompat.getDrawable(context, R.drawable.ic_mood_sad)
        else if (showHappy == true)  ContextCompat.getDrawable(context, R.drawable.ic_mood_happy)
        else ContextCompat.getDrawable(context, R.drawable.ic_mood_happy)
        repeat(15) {
            createEmoji()
        }
        invalidate()
    }

    private fun createEmoji() {
        val emoji = EmojiParticle(
            x = random.nextFloat() * width,
            y = -100f,
            rotation = random.nextFloat() * 360,
            speed = 5f + random.nextFloat() * 5,
            size = 60f + random.nextFloat() * 40
        )
        emojis.add(emoji)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isRaining) return

        emojis.forEach { emoji ->
            canvas.save()
            canvas.rotate(emoji.rotation, emoji.x + emoji.size/2, emoji.y + emoji.size/2)
            emojiDrawable?.setBounds(
                emoji.x.toInt(),
                emoji.y.toInt(),
                (emoji.x + emoji.size).toInt(),
                (emoji.y + emoji.size).toInt()
            )
            emojiDrawable?.draw(canvas)
            canvas.restore()

            emoji.y += emoji.speed
            emoji.rotation += 1
        }

        emojis.removeAll { it.y > height + 100 }

        if (emojis.isNotEmpty()) {
            invalidate()
        } else {
            isRaining = false
        }
    }
}
