package com.example.storynest.Comments

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.max

class ScrollAwareBottomSheetBehavior<V : View> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BottomSheetBehavior<V>(context, attrs) {

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        val canScrollUp = child.canScrollVertically(-1)
        val canScrollDown = child.canScrollVertically(1)

        return if (canScrollUp || canScrollDown) {
            false
        } else {
            super.onInterceptTouchEvent(parent, child, event) // BottomSheet drag
        }
    }

}
