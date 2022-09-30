package com.idormy.sms.forwarder.service

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.BuildProperties


class FrontService : Service() {

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ONE_ID)
        } else {
            Notification.Builder(this)
        }
        builder.setSmallIcon(R.drawable.ic_forwarder)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        try {
            if (BuildProperties.containsKey("ro.product.system.brand")) {
                val brand = BuildProperties.getProperty("ro.product.system.brand")
                if (brand != null && brand.contains("xiaomi", true)) {
                    builder.setContentTitle(getString(R.string.app_name))
                }
            }
        } catch (e: Throwable) {
            println(e)
        }
        builder.setContentText(getString(R.string.notification_content))
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            val notificationChannel = NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN)
            notificationChannel.enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false) //是否显示角标
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val notification = builder.build()
        startForeground(1, notification)
        //Android8.1以下尝试启动主界面，以便动态获取权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        val enabledListenerPackages = NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
        if (Core.dataStore.switchNotify && enabledListenerPackages.contains(applicationContext.packageName)) {
            Core.packageManager.setComponentEnabledSetting(
                ComponentName(Core.app.applicationContext, NotifyService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            Core.packageManager.setComponentEnabledSetting(
                ComponentName(Core.app.applicationContext, NotifyService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        }
    }

    override fun onDestroy() {
        stopForeground(true)
        isRunning = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        return START_STICKY //保证service不被杀死
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        var isRunning = false
        private const val CHANNEL_ONE_ID = "com.idormy.sms.forwarder"
        private const val CHANNEL_ONE_NAME = "com.idormy.sms.forwarderName"
    }
}