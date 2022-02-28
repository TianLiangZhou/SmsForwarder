package com.idormy.sms.forwarder.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.idormy.sms.forwarder.R

open class ProgressToolbarFragment : ToolbarFragment() {

    lateinit var progressBar: LinearProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.linear_progress)
    }
}