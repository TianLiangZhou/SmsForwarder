package com.idormy.sms.forwarder.service

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.Key
import com.idormy.sms.forwarder.utilities.MessageType
import com.idormy.sms.forwarder.utilities.Worker
import com.idormy.sms.forwarder.workers.SendWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class NotifyService : NotificationListenerService() {
    /**
     * 发布通知
     *
     * @param sbn 状态栏通知
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        //未开启转发
        if (!Core.dataStore.switchNotify) {
            return
        }
        //异常通知跳过
        if (sbn.notification == null) return
        if (sbn.notification.extras == null) return
        //推送通知的应用包名
        val packageName = sbn.packageName
        //自身通知跳过
        if (Key.PACKAGE_NAME == packageName) return
        try {
            //通知标题
            var title = ""
            if (sbn.notification.extras["android.title"] != null) {
                title = sbn.notification.extras["android.title"].toString()
            }
            //通知内容
            var text = ""
            if (sbn.notification.extras["android.text"] != null) {
                text = sbn.notification.extras["android.text"].toString()
            }
            if (text.isEmpty() && sbn.notification.tickerText != null) {
                text = sbn.notification.tickerText.toString()
            }
            //不处理空消息（标题跟内容都为空）
            if (title.isEmpty() && text.isEmpty()) return

            //通知时间
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date(sbn.postTime))
            Log.d(
                TAG, String.format(
                    Locale.getDefault(),
                    "onNotificationPosted：\n应用包名：%s\n消息标题：%s\n消息内容：%s\n消息时间：%s\n",
                    packageName, title, text, time
                )
            )
            val notify = mutableListOf(
                Message(packageName, text, System.currentTimeMillis(), MessageType.App)
            )
            val request = OneTimeWorkRequestBuilder<SendWorker>()
                .setInputData(
                    workDataOf(Worker.sendMessage to Json.encodeToString(notify))
                )
                .build()
            WorkManager.getInstance(applicationContext).enqueue(request)
        } catch (e: Exception) {
            Log.e(TAG, "onNotificationPosted:", e)
        }
    }

    /**
     * 通知已删除
     *
     * @param sbn 状态栏通知
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {

    }

    /**
     * 监听断开
     */
    override fun onListenerDisconnected() {
        //未开启转发
        if (!Core.dataStore.switchNotify) {
            return
        }
        Log.d(TAG, "通知侦听器断开连接 - 请求重新绑定")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    companion object {
        const val TAG = "NotifyService"
    }
}