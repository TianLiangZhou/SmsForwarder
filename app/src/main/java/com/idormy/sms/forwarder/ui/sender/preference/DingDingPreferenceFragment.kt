package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.idormy.sms.forwarder.R

class DingDingPreferenceFragment : SenderPreferenceFragment() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val token = findPreference<EditTextPreference>("token")

        token?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.dingding_token_helper)
            } else {
                text
            }
        }
        val secret = findPreference<EditTextPreference>("secret")

        secret?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.dingding_secret_helper)
            } else {
                text
            }
        }

        val atMobiles = findPreference<EditTextPreference>("atMobiles")

        atMobiles?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.dingding_at_helper)
            } else {
                text
            }
        }


    }

    override fun getChildView(): Int {
        return R.xml.pref_sender_dingding
    }

}