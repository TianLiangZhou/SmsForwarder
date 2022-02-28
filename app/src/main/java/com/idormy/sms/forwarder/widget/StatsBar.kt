package com.idormy.sms.forwarder.widget

import android.content.Context
import android.text.format.Formatter
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.R

class StatsBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                         defStyleAttr: Int = R.attr.bottomAppBarStyle) :
        BottomAppBar(context, attrs, defStyleAttr) {
    private lateinit var statusText: TextView
    private lateinit var txText: TextView
    private lateinit var rxText: TextView
    private lateinit var txRateText: TextView
    private lateinit var rxRateText: TextView
    private lateinit var behavior: Behavior
    override fun getBehavior(): Behavior {
        if (!this::behavior.isInitialized) behavior = object : Behavior() {
            override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomAppBar, target: View,
                                        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
                                        type: Int, consumed: IntArray) {
                super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed + dyUnconsumed,
                        dxUnconsumed, 0, type, consumed)
            }
        }
        return behavior
    }

    override fun setOnClickListener(l: OnClickListener?) {

    }

    private fun setStatus(text: CharSequence) {
        statusText.text = text
        TooltipCompat.setTooltipText(this, text)
    }

    fun updateTraffic(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) {
        txText.text = "▲ ${Formatter.formatFileSize(context, txTotal)}"
        rxText.text = "▼ ${Formatter.formatFileSize(context, rxTotal)}"
    }

}
