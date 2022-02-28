package com.idormy.sms.forwarder.ui.sender.preference

import com.idormy.sms.forwarder.R

class SmsPreference(title: String) : FullPreference(title) {
    override fun getChildView(): Int {
        return R.layout.alert_dialog_setview_sms
    }
}