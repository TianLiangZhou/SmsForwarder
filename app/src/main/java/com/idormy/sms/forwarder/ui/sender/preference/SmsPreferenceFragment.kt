package com.idormy.sms.forwarder.ui.sender.preference

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.provider.Phone

class SmsPreferenceFragment : SenderPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val mobiles = findPreference<EditTextPreference>("mobiles")
        mobiles?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            val text = it.text
            if (TextUtils.isEmpty(text)) {
                resources.getString(R.string.SmsMobileHelper)
            } else {
                text
            }
        }
        val entries = mutableListOf("全部")
        val entryValues = mutableListOf("ALL")
        val sim = Phone.sim()
        sim.forEachIndexed { i, s ->
            entries.add("SIM" + s.simSlotIndex)
            entryValues.add("SIM" + s.simSlotIndex)
        }
        val simSlot = findPreference<ListPreference>("simSlot")
        simSlot?.entries = entries.toTypedArray()
        simSlot?.entryValues = entryValues.toTypedArray()


    }

    override fun getChildView(): Int {
        return R.xml.pref_sender_sms
    }
}