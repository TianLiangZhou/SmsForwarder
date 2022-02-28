package com.idormy.sms.forwarder.ui.rule

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.idormy.sms.forwarder.db.model.Sender

class SenderPreferenceFragment(context: Context, attrs: AttributeSet? = null) : ListPreference(context, attrs) {


    lateinit var senders: List<Sender>


    fun initSenders(senders: List<Sender>) {
        this.senders = senders
        this.entries = senders.map { it.name }.toTypedArray()
        this.entryValues = senders.map { it.id.toString() }.toTypedArray()
    }
}