package com.idormy.sms.forwarder.ui.system

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.FragmentCloneBinding
import com.idormy.sms.forwarder.ui.ToolbarFragment

class HelpFragment : ToolbarFragment() {
    private var _binding: FragmentCloneBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCloneBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.clone)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}