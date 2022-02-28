package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import com.idormy.sms.forwarder.R

class EmailPreferenceFragment : SenderPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val port = findPreference<EditTextPreference>("port")
        port?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        val fromEmail = findPreference<EditTextPreference>("fromEmail")
        fromEmail?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        }
        val toEmail = findPreference<EditTextPreference>("toEmail")
        toEmail?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        }
    }

    override fun getChildView(): Int {
        return R.xml.pref_sender_email
    }

}