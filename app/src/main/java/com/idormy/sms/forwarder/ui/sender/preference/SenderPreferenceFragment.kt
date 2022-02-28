package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.FragmentSenderDialogBinding


abstract class FullPreference(private val title: String) : PreferenceFragmentCompat() {

    private var _binding: FragmentSenderDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    private var _toolbar: Toolbar? = null

    protected val toolbar get() = _toolbar!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSenderDialogBinding.inflate(inflater, container, false)
        _toolbar = binding.toolbar
        binding.senderDialogLayout.addView(
            inflater.inflate(getChildView(), container, false)
        )
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
        }
        toolbar.title = this.title
        toolbar.inflateMenu(R.menu.sender_dialog_menu)
        toolbar.setOnMenuItemClickListener {
            true
        }
    }


    abstract fun getChildView(): Int
}