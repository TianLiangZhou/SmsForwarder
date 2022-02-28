package com.idormy.sms.forwarder.ui.system

import android.os.Bundle
import androidx.preference.*
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.Key

class SettingPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = Core.dataStore.store
        Core.dataStore.initAppSetting()
        addPreferencesFromResource(R.xml.pref_setting)
        val deviceName = findPreference<EditTextPreference>(Key.deviceName)!!
        deviceName.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }
        val retry = findPreference<SeekBarPreference>(Key.retryInterval)!!
        retry.min = 0
        retry.max = 60
        retry.showSeekBarValue = true
        retry.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }
        val power = findPreference<SeekBarPreference>(Key.powerAlarm)!!
        power.min = 0
        power.max = 99
        power.showSeekBarValue = true
        power.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            true
        }

        val sms = findPreference<SwitchPreference>(Key.switchSms)!!
        sms.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }
        val call = findPreference<SwitchPreference>(Key.switchCall)!!
        call.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }
        val app = findPreference<SwitchPreference>(Key.switchNotify)!!
        app.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }
        val monitor = findPreference<SwitchPreference>(Key.switchMonitorBattery)!!
        monitor.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }
        val recent = findPreference<SwitchPreference>(Key.switchExcludeRecent)!!
        recent.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, value ->
            true
        }


    }
}