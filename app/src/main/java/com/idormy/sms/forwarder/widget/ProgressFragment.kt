package com.idormy.sms.forwarder.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.ProgressCircularIndicatorBinding
import com.idormy.sms.forwarder.utilities.Action

class ProgressFragment : DialogFragment() {
    private var _binding: ProgressCircularIndicatorBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        isCancelable = false

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ProgressCircularIndicatorBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tips = arguments?.getString(Action.tips)
        if (tips != null) {
            binding.progressTips.text = tips
            binding.progressTips.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.window?.setLayout(width, height)
        }
    }
}