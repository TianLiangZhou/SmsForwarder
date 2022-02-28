package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.utilities.Key

class PushPlusPreferenceFragment : SenderPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val port: EditTextPreference = findPreference(Key.validTime)!!
        port.setOnBindEditTextListener {editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }


    override fun getChildView(): Int {
        return R.xml.pref_sender_pushplus
    }

}