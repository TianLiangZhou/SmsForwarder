package com.idormy.sms.forwarder.ui.rule

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.LayoutSelectSenderDialogBinding
import com.idormy.sms.forwarder.db.model.Sender

class SenderPreferenceDialogFragment : PreferenceDialogFragmentCompat() {

    private val preference by lazy { getPreference() as SenderPreferenceFragment }

    private var clickIndex: Int = -1

    class SenderIconArrayAdapter(context: Context, @LayoutRes layout: Int, dataSet: List<Sender>): ArrayAdapter<Sender>(context, layout, dataSet) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutSelectSenderDialogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            view.senderImage.setImageResource(getItem(position)?.getImageId() ?: 0)
            view.senderName.text = getItem(position)?.name
            return view.root
        }
    }

    fun setArg(key: String) {
        arguments = bundleOf(ARG_KEY to key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            clickIndex = preference.findIndexOfValue(preference.value)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_sender)
            .setAdapter(SenderIconArrayAdapter(requireContext(), R.layout.layout_select_sender_dialog, preference.senders)) { dialog, i ->
                clickIndex = i

                // Clicking on an item simulates the positive button click, and dismisses
                // the dialog.
                this@SenderPreferenceDialogFragment.onClick(
                    dialog,
                    DialogInterface.BUTTON_POSITIVE
                )
                dialog.dismiss()
            }
            .create()

        return dialog
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && clickIndex >= 0) {
            Log.d("close dialog", "close")
            val value: String = preference.entryValues[clickIndex].toString()
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
        }
    }
}