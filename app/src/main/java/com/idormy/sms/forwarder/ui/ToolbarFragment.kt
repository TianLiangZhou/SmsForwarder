package com.idormy.sms.forwarder.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.R

open class ToolbarFragment : Fragment() {

    lateinit var toolbar: Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_round_navigation_menu_24)
        toolbar.setNavigationOnClickListener {
            (activity as MainActivity).drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    open fun onBackPressed(): Boolean = false
}