package com.example.storynest.CustomViews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import androidx.dynamicanimation.animation.*
import kotlin.math.abs


class DraggableCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
    }

    private var dX = 0f
    private var dY = 0f

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var isDragging = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                dX = x - event.rawX
                dY = y - event.rawY
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val newX = event.rawX + dX
                val newY = event.rawY + dY

                if (!isDragging) {
                    if (abs(newX - x) > touchSlop || abs(newY - y) > touchSlop) {
                        isDragging = true
                    }
                }

                if (isDragging) {
                    moveWithinBounds(newX, newY)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    snapToEdge()
                }
                isDragging = false
            }
        }
        return true
    }

    private fun moveWithinBounds(newX: Float, newY: Float) {
        val parentView = parent as View

        val maxX = parentView.width - width
        val maxY = parentView.height - height

        x = newX.coerceIn(0f, maxX.toFloat())
        y = newY.coerceIn(0f, maxY.toFloat())
    }

    private fun snapToEdge() {
        val parentView = parent as View
        val middle = parentView.width / 2

        val targetX = if (x + width / 2 < middle) {
            0f
        } else {
            (parentView.width - width).toFloat()
        }

        val springAnim = SpringAnimation(this, DynamicAnimation.X, targetX)
        springAnim.spring.stiffness = SpringForce.STIFFNESS_MEDIUM
        springAnim.spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        springAnim.start()
    }
}
