package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.idormy.sms.forwarder.R

class BarkPreferenceFragment : SenderPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val server = findPreference<EditTextPreference>("server")

        server?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.set_bark_server_helper)
            } else {
                text
            }
        }
        server?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_TEXT_VARIATION_URI
        }
        val icon = findPreference<EditTextPreference>("icon")

        icon?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.set_bark_icon_helper)
            } else {
                text
            }
        }
        icon?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_TEXT_VARIATION_URI
        }


    }


    override fun getChildView(): Int {
        return R.xml.pref_sender_bark
    }
}