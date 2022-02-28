package com.idormy.sms.forwarder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ServiceFloatingButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FloatingActionButton(context, attrs, defStyleAttr), DynamicAnimation.OnAnimationEndListener {

    private var checked = true

    override fun onAnimationEnd(animation: DynamicAnimation<*>?, canceled: Boolean, value: Float, velocity: Float) {
        TODO("Not yet implemented")
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (checked) View.mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
        return drawableState
    }

}