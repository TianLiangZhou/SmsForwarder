package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.idormy.sms.forwarder.R

class WebNotifyPreferenceFragment : SenderPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val server = findPreference<EditTextPreference>("server")
        server?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.WebNotifyWebServerHelper)
            } else {
                text
            }
        }

        val params = findPreference<EditTextPreference>("params")
        params?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.WebNotifyWebParamsHelper)
            } else {
                text
            }
        }

        val secret = findPreference<EditTextPreference>("secret")
        secret?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.WebNotifySecretHelper)
            } else {
                text
            }
        }
        val method = findPreference<ListPreference>("method")
        method?.entries = arrayOf("POST", "GET", "PUT")
        method?.entryValues = arrayOf("POST", "GET", "PUT")
    }

    override fun getChildView(): Int {
        return R.xml.pref_sender_webnotify
    }
}