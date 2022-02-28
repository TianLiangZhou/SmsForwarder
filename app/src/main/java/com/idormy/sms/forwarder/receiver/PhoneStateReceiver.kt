package com.idormy.sms.forwarder.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.idormy.sms.forwarder.App
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.data.CallInfo
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.provider.Phone
import com.idormy.sms.forwarder.utilities.MessageType
import com.idormy.sms.forwarder.utilities.Worker
import com.idormy.sms.forwarder.workers.SendWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Core.dataStore.switchCall) {
            return
        }
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
            return
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val action = intent.action
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED == action) {
            //获取来电号码
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Core.telephonyManager.callStateForSubscription
            } else {
                Core.telephonyManager.callState
            }
            Log.d(TAG, "来电信息：state=$state phoneNumber = $phoneNumber")
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return
            }
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {}
                TelephonyManager.CALL_STATE_IDLE -> { // 未接来电
                    (Core.app as App).applicationScope.launch {
                        delay(1000L)
                        sendReceiveCallMsg(context, phoneNumber)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {}
            }
        }
    }

    private fun sendReceiveCallMsg(context: Context, phoneNumber: String) {
        //获取后一条通话记录
        val callInfo: CallInfo? = Phone.lastCallRecords(phoneNumber)
        println(callInfo)
        if (callInfo == null || callInfo.type != 3) {
            Log.d(TAG, "非未接来电不处理！")
            return
        }
        var name = callInfo.name
        Log.d(TAG, "getSubscriptionId = " + callInfo.subscriptionId)
        var simInfo = "ALL"
        for (sim in Phone.sim()) {
            if (sim.subscriptionId == callInfo.subscriptionId) {
                simInfo = "SIM${sim.simSlotIndex}"
            }
        }
        if (TextUtils.isEmpty(name)) {
            val contacts = Phone.getContactByNumber(phoneNumber)
            name = if (contacts.isNotEmpty()) {
                contacts[0].name
            } else {
                Core.app.getString(R.string.unknown_number)
            }
        }
        //TODO:同一卡槽同一秒的重复未接来电广播不再重复处理（部分机型会收到两条广播？）
//        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date())
//        val prevHash = SettingUtil.getPrevNoticeHash(phoneNumber)
//        val currHash = CommonUtil.MD5(phoneNumber + simInfo + time)
//        Log.d(TAG, "prevHash=$prevHash currHash=$currHash")
//        if (prevHash != null && prevHash == currHash) {
//            Log.w(TAG, "同一卡槽同一秒的重复未接来电广播不再重复处理（部分机型会收到两条广播）")
//            return
//        }
//        SettingUtil.setPrevNoticeHash(phoneNumber, currHash)
        val calling = mutableListOf(
            Message(phoneNumber, name + context.getString(R.string.calling), System.currentTimeMillis(), MessageType.Call, simInfo)
        )
        val request = OneTimeWorkRequestBuilder<SendWorker>()
            .setInputData(workDataOf(Worker.sendMessage to Json.encodeToString(calling)))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
}