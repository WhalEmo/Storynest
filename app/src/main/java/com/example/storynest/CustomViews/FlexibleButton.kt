package com.example.storynest.CustomViews

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.example.storynest.R

class FlexibleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var cornerRadius = 24f
    private var backgroundColor = Color.BLUE
    private var iconDrawable: Drawable? = null
    private var iconSize = 0
    private var iconPadding = 8
    private var iconGravity = 0 // 0 start, 1 end

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FlexibleButton,
            0, 0
        ).apply {

            try {
                backgroundColor = getColor(
                    R.styleable.FlexibleButton_fb_backgroundColor,
                    Color.BLUE
                )

                cornerRadius = getDimension(
                    R.styleable.FlexibleButton_fb_cornerRadius,
                    24f
                )

                iconDrawable = getDrawable(
                    R.styleable.FlexibleButton_fb_icon
                )

                iconSize = getDimensionPixelSize(
                    R.styleable.FlexibleButton_fb_iconSize,
                    0
                )

                iconPadding = getDimensionPixelSize(
                    R.styleable.FlexibleButton_fb_iconPadding,
                    8
                )

                iconGravity = getInt(
                    R.styleable.FlexibleButton_fb_iconGravity,
                    0
                )

            } finally {
                recycle()
            }
        }

        applyBackground()
        applyIcon()
    }

    private fun applyBackground() {
        val drawable = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = this@FlexibleButton.cornerRadius
        }
        background = drawable
    }

    private fun applyIcon() {
        iconDrawable?.let {

            if (iconSize != 0) {
                it.setBounds(0, 0, iconSize, iconSize)
            }

            compoundDrawablePadding = iconPadding

            when{
                iconGravity == 0 -> setCompoundDrawablesRelative(it, null, null, null)
                iconGravity == 1 -> setCompoundDrawablesRelative(null, null, it, null)
                iconGravity == 2 -> setCompoundDrawablesRelative(null, it, null, null)
                iconGravity == 3 -> setCompoundDrawablesRelative(null, null, null, it)
            }
        }
    }

    // Public API (runtime değişiklik için)
    fun setButtonColor(color: Int) {
        backgroundColor = color
        applyBackground()
    }

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        applyBackground()
    }
}
