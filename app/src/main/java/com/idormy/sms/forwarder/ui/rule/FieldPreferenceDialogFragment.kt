package com.idormy.sms.forwarder.ui.rule

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.appcompat.R
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.ListPreferenceDialogFragmentCompat


class FieldPreferenceDialogFragment : ListPreferenceDialogFragmentCompat() {

    var mClickedDialogEntryIndex: Int = -1

    companion object {
        fun newInstance(key: String, enable: IntArray): FieldPreferenceDialogFragment {
            val fragment = FieldPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            b.putIntArray(ENABLE_KEY, enable)
            fragment.arguments = b
            return fragment
        }

        private const val ENABLE_KEY = "enable_key"
    }

    private fun getListPreference(): ListPreference {
        return preference as ListPreference
    }


    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        builder.setAdapter(
            object: ArrayAdapter<CharSequence>(
                requireContext(),
                R.layout.select_dialog_singlechoice_material,
                getListPreference().entries
            ) {
                @SuppressLint("ViewHolder")
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = LayoutInflater.from(context).inflate(R.layout.select_dialog_singlechoice_material, parent, false)
                    val checkedTextView = view.findViewById<CheckedTextView>(android.R.id.text1)
                    checkedTextView.text = getItem(position)
                    if (position == mClickedDialogEntryIndex) {
                        checkedTextView.isChecked = true
                    }
                    if (position !in arguments?.getIntArray(ENABLE_KEY)!!) {
                        checkedTextView.isEnabled = false
                        checkedTextView.isClickable = false
                        checkedTextView.setOnClickListener(null)
                    }
                    return view
                }
            }
        ) { dialogInterface: DialogInterface, i: Int ->
            this.mClickedDialogEntryIndex = i
            // Clicking on an item simulates the positive button click, and dismisses
            // the dialog.

            // Clicking on an item simulates the positive button click, and dismisses
            // the dialog.
            this@FieldPreferenceDialogFragment.onClick(
                dialog!!,
                DialogInterface.BUTTON_POSITIVE
            )
            dialog!!.dismiss()
        }

        // The typical interaction for list-based dialogs is to have click-on-an-item dismiss the
        // dialog instead of the user having to press 'Ok'.
        builder.setPositiveButton(null, null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && mClickedDialogEntryIndex >= 0) {
            val preference = getListPreference()
            val value = preference.entryValues[mClickedDialogEntryIndex].toString()
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
        }
    }
}