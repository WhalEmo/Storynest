package com.example.storynest.CustomViews

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView

class CircleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatImageView(context, attrs,defStyle) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A6CF7")
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private var dimBackground: View? = null;

    init {
        this.scaleType = ScaleType.CENTER_CROP

        this.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }

        }
        this.clipToOutline = true

        this.setOnLongClickListener {
            startPopupEffect()
            true
        }
    }

    private fun startPopupEffect() {
        animate().scaleX(1.15f).scaleY(1.15f).setDuration(200).start()
        animateBorderColor(Color.parseColor("#4A6CF7"), Color.parseColor("#545454"))
        addDimBackground()
    }

    private fun addDimBackground(){
        if(this.parent !is ViewGroup) return
        val parentView = this.parent as ViewGroup

        dimBackground = View(this.context).apply {
            setBackgroundColor(Color.parseColor("#88000000"))
            alpha = 0f
            animate().alpha(1f).setDuration(200).start()
        }
        parentView.addView(dimBackground,0)

    }

    private fun reset() {
        animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        animateBorderColor(Color.parseColor("#545454"), Color.parseColor("#4A6CF7"))

        dimBackground?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                (parent as ViewGroup).removeView(dimBackground)
            }
            ?.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP ||
            event.action == MotionEvent.ACTION_CANCEL
        ) {
            reset()
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = width.coerceAtMost(height) / 2f

        // Daire path oluştur
        path.reset()
        path.addCircle(width / 2f, height / 2f, radius, Path.Direction.CW)
        canvas.clipPath(path)

        super.onDraw(canvas)

        canvas.drawCircle(width / 2f, height / 2f, radius - 1f, borderPaint)
    }


    private fun animateBorderColor(fromColor: Int, toColor: Int) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 250

        val startR = Color.red(fromColor)
        val startG = Color.green(fromColor)
        val startB = Color.blue(fromColor)

        val endR = Color.red(toColor)
        val endG = Color.green(toColor)
        val endB = Color.blue(toColor)

        animator.addUpdateListener { valueAnimator ->
            val fraction = valueAnimator.animatedValue as Float
            val newR = (startR + (endR - startR) * fraction).toInt()
            val newG = (startG + (endG - startG) * fraction).toInt()
            val newB = (startB + (endB - startB) * fraction).toInt()
            borderPaint.color = Color.rgb(newR, newG, newB)
            invalidate()
        }

        animator.start()
    }


}