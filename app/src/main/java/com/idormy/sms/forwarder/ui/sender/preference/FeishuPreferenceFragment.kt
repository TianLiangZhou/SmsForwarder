package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.idormy.sms.forwarder.R

class FeishuPreferenceFragment : SenderPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val webhook = findPreference<EditTextPreference>("webhook")

        webhook?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.feishu_webhook_helper)
            } else {
                text
            }
        }
        val secret = findPreference<EditTextPreference>("secret")

        secret?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.feishu_secret_helper)
            } else {
                text
            }
        }

    }

    override fun getChildView(): Int {
        return R.xml.pref_sender_feishu
    }
}