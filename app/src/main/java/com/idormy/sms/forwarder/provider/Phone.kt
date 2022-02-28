package com.idormy.sms.forwarder.provider

import android.os.Build
import android.telephony.SmsManager
import android.util.Log

object Mobile {

    fun SIM() {


    }


    fun sendSms(subId: Int, message: String, mobiles: List<String>) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Core.app.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
        } else {
            SmsManager.getSmsManagerForSubscriptionId(subId)
        }
        Log.d("start send", " ..... ")
        for (mobile in mobiles) {
            val divideMessage = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(
                mobile,
                null,
                divideMessage,
                null,
                null
            )
        }
    }
}