package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.Key


class TelegramPreferenceFragment : SenderPreferenceFragment() {

    private lateinit var host: EditTextPreference
    private lateinit var port: EditTextPreference
    private lateinit var username: EditTextPreference
    private lateinit var password: EditTextPreference
    private lateinit var authenticator: SwitchPreference
    private lateinit var protocol: ListPreference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        protocol = findPreference("protocol")!!
        host = findPreference("host")!!
        port = findPreference("port")!!
        port.setOnBindEditTextListener {editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
        username = findPreference("username")!!
        password = findPreference("pwd")!!
        authenticator = findPreference("authenticator")!!
        protocol.onPreferenceChangeListener = this
        authenticator.onPreferenceChangeListener = this

        val currentProtocol = Core.dataStore.store.getString(Key.protocol, "none")
        if (currentProtocol != "none") {
            host.isEnabled = true
            port.isEnabled = true
        }

        val proxyAuthorization = Core.dataStore.store.getBoolean(Key.authenticator, false)
        if (proxyAuthorization) {
            username.isEnabled = true
            password.isEnabled = true
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        Log.d("newValue", newValue.toString())
        if (preference is ListPreference) {
            if (newValue in arrayOf("http", "socks")) {
                host.isEnabled = true
                port.isEnabled = true
                authenticator.isEnabled = true
            } else {
                host.isEnabled = false
                port.isEnabled = false
                authenticator.isEnabled = false
                username.isEnabled = false
                password.isEnabled = false
            }
        }
        if (preference is SwitchPreference) {
            if (newValue is Boolean && newValue) {
                username.isEnabled = true
                password.isEnabled = true
            } else {
                username.isEnabled = false
                password.isEnabled = false
            }
        }
        return super.onPreferenceChange(preference, newValue)
    }


    override fun getChildView(): Int {
        return R.xml.pref_sender_telegram
    }

}