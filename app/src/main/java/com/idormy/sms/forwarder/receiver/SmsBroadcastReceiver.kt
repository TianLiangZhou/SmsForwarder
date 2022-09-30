package com.idormy.sms.forwarder.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.MessageType
import com.idormy.sms.forwarder.utilities.Worker
import com.idormy.sms.forwarder.workers.SendWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Core.dataStore.switchSms) {
            return
        }
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) {
            return
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val mutableMap: MutableMap<String, Message> = HashMap()
        val slot = (intent.extras?.getInt("slot")?:0) + 1
        for (message in messages) {
            val mobile = message.displayOriginatingAddress!!
            if (mutableMap.containsKey(mobile)) {
                mutableMap[mobile]?.content += message.messageBody
            } else {
                mutableMap[mobile] = Message(
                    mobile,
                    message.messageBody,
                    message.timestampMillis,
                    MessageType.Sms,
                    "SIM$slot"
                )
            }
        }

        if (mutableMap.isNotEmpty()) {
            val request = OneTimeWorkRequestBuilder<SendWorker>()
                .setInputData(workDataOf(Worker.sendMessage to Json.encodeToString(
                    ArrayList(mutableMap.values)
                )))
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}