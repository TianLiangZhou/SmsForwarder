package com.idormy.sms.forwarder.service

import okhttp3.Request.Builder.url
import okhttp3.Request.Builder.get
import okhttp3.Request.Builder.build
import okhttp3.OkHttpClient.newCall
import okhttp3.Call.enqueue
import okhttp3.Response.body
import okhttp3.ResponseBody.string
import okhttp3.Response.code
import okhttp3.Request.Builder.addHeader
import okhttp3.Request.Builder.post
import okhttp3.Credentials.basic
import okhttp3.Response.request
import okhttp3.Request.newBuilder
import okhttp3.Request.Builder.header
import okhttp3.OkHttpClient.newBuilder
import okhttp3.OkHttpClient.Builder.proxy
import okhttp3.OkHttpClient.Builder.proxyAuthenticator
import okhttp3.OkHttpClient.Builder.connectTimeout
import okhttp3.OkHttpClient.Builder.readTimeout
import okhttp3.OkHttpClient.Builder.connectionPool
import okhttp3.OkHttpClient.Builder.build
import okhttp3.Request.Builder.method
import okhttp3.MultipartBody.Builder.setType
import okhttp3.MultipartBody.Builder.addFormDataPart
import okhttp3.MultipartBody.Builder.build
import okhttp3.OkHttpClient.Builder.sslSocketFactory
import okhttp3.OkHttpClient.Builder.hostnameVerifier
import okhttp3.ResponseBody.byteStream
import okhttp3.ResponseBody.contentLength
import android.widget.LinearLayout
import android.widget.EditText
import android.view.LayoutInflater
import com.idormy.sms.forwarder.R
import android.text.TextWatcher
import android.widget.Toast
import android.text.Editable
import android.annotation.SuppressLint
import android.app.*
import com.idormy.sms.forwarder.utils.SettingUtil
import android.content.Intent
import android.provider.BaseColumns
import kotlin.Throws
import com.idormy.sms.forwarder.model.vo.SmsVo
import com.idormy.sms.forwarder.model.RuleModel
import com.idormy.sms.forwarder.utils.RuleLineUtils
import com.idormy.sms.forwarder.model.SenderModel
import com.idormy.sms.forwarder.utils.OSUtil.ROM_TYPE
import com.idormy.sms.forwarder.utils.BuildProperties
import com.idormy.sms.forwarder.utils.OSUtil
import com.idormy.sms.forwarder.utils.DbHelper
import android.database.sqlite.SQLiteDatabase
import com.idormy.sms.forwarder.model.LogModel
import android.content.ContentValues
import com.idormy.sms.forwarder.model.LogTable.LogEntry
import com.idormy.sms.forwarder.model.vo.LogVo
import com.idormy.sms.forwarder.model.SenderTable.SenderEntry
import com.idormy.sms.forwarder.utils.NetUtil
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiInfo
import android.os.Bundle
import com.idormy.sms.forwarder.utils.SimUtil
import com.idormy.sms.forwarder.utils.PhoneUtils.SimInfo
import com.idormy.sms.forwarder.MyApplication
import com.idormy.sms.forwarder.utils.SmsUtil
import androidx.annotation.RequiresApi
import android.os.Build
import android.database.sqlite.SQLiteOpenHelper
import com.idormy.sms.forwarder.utils.InitUtil
import com.idormy.sms.forwarder.utils.RuleLine
import com.idormy.sms.forwarder.utils.RuleUtil
import com.idormy.sms.forwarder.utils.CacheUtil
import com.idormy.sms.forwarder.utils.CertUtils
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import androidx.core.app.NotificationManagerCompat
import android.content.ComponentName
import com.idormy.sms.forwarder.service.NotifyService
import com.idormy.sms.forwarder.utils.CommonUtil
import android.content.pm.PackageInfo
import androidx.core.app.ActivityCompat
import com.idormy.sms.forwarder.utils.PhoneUtils
import android.telephony.TelephonyManager
import android.telephony.SubscriptionManager
import com.idormy.sms.forwarder.utils.PhoneUtils.MethodNotFoundException
import android.telephony.SubscriptionInfo
import android.content.ContentResolver
import android.provider.CallLog
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.idormy.sms.forwarder.utils.Define
import com.idormy.sms.forwarder.model.PhoneBookEntity
import com.idormy.sms.forwarder.utils.ContactHelper
import android.provider.ContactsContract
import com.idormy.sms.forwarder.utils.ContactHelper.InstanceHolder
import kotlin.jvm.JvmStatic
import android.os.PowerManager
import com.idormy.sms.forwarder.utils.KeepAliveUtils
import android.content.pm.ResolveInfo
import com.idormy.sms.forwarder.sender.SendUtil
import com.idormy.sms.forwarder.sender.SenderUtil
import com.idormy.sms.forwarder.model.vo.DingDingSettingVo
import com.alibaba.fastjson.JSON
import com.idormy.sms.forwarder.sender.SenderDingdingMsg
import com.idormy.sms.forwarder.model.vo.EmailSettingVo
import com.idormy.sms.forwarder.sender.SenderMailMsg
import com.idormy.sms.forwarder.model.vo.BarkSettingVo
import com.idormy.sms.forwarder.sender.SenderBarkMsg
import com.idormy.sms.forwarder.model.vo.WebNotifySettingVo
import com.idormy.sms.forwarder.sender.SenderWebNotifyMsg
import com.idormy.sms.forwarder.model.vo.QYWXGroupRobotSettingVo
import com.idormy.sms.forwarder.sender.SenderQyWxGroupRobotMsg
import com.idormy.sms.forwarder.model.vo.QYWXAppSettingVo
import com.idormy.sms.forwarder.sender.SenderQyWxAppMsg
import com.idormy.sms.forwarder.model.vo.ServerChanSettingVo
import com.idormy.sms.forwarder.sender.SenderServerChanMsg
import com.idormy.sms.forwarder.model.vo.TelegramSettingVo
import com.idormy.sms.forwarder.sender.SenderTelegramMsg
import com.idormy.sms.forwarder.model.vo.SmsSettingVo
import com.idormy.sms.forwarder.sender.SenderSmsMsg
import com.idormy.sms.forwarder.model.vo.FeiShuSettingVo
import com.idormy.sms.forwarder.sender.SenderFeishuMsg
import com.idormy.sms.forwarder.model.vo.PushPlusSettingVo
import com.idormy.sms.forwarder.sender.SenderPushPlusMsg
import com.idormy.sms.forwarder.sender.SendHistory
import com.idormy.sms.forwarder.sender.SenderBaseMsg
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.ObservableEmitter
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import io.reactivex.rxjava3.core.ObservableSource
import com.idormy.sms.forwarder.SenderActivity
import com.smailnet.emailkit.EmailKit
import com.smailnet.emailkit.EmailKit.GetSendCallback
import okhttp3.RequestBody
import okhttp3.Route
import okhttp3.MultipartBody
import com.idormy.sms.forwarder.model.AppInfo
import android.widget.ArrayAdapter
import android.view.ViewGroup
import android.widget.TextView
import com.idormy.sms.forwarder.utils.TimeUtil
import com.idormy.sms.forwarder.service.FrontService
import android.graphics.BitmapFactory
import com.idormy.sms.forwarder.MainActivity
import android.os.IBinder
import android.os.BatteryManager
import android.content.ContextWrapper
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.idormy.sms.forwarder.service.BatteryService
import android.content.BroadcastReceiver
import com.idormy.sms.forwarder.receiver.PhoneStateReceiver
import androidx.appcompat.app.AppCompatActivity
import com.idormy.sms.forwarder.RefreshListView.IRefreshListener
import com.idormy.sms.forwarder.adapter.LogAdapter
import com.idormy.sms.forwarder.RefreshListView
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.content.DialogInterface
import com.umeng.analytics.MobclickAgent
import com.idormy.sms.forwarder.AppListActivity
import com.idormy.sms.forwarder.CloneActivity
import com.idormy.sms.forwarder.SettingActivity
import com.idormy.sms.forwarder.AboutActivity
import com.idormy.sms.forwarder.RuleActivity
import com.idormy.sms.forwarder.adapter.RuleAdapter
import android.widget.CompoundButton
import com.idormy.sms.forwarder.receiver.RebootBroadcastReceiver
import com.xuexiang.xupdate.easy.EasyUpdate
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker
import com.idormy.sms.forwarder.view.IPEditText
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.HttpServerRequestCallback
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.umeng.commonsdk.UMConfigure
import android.app.ActivityManager.AppTask
import com.idormy.sms.forwarder.adapter.SenderAdapter
import com.idormy.sms.forwarder.adapter.AppAdapter
import android.content.ClipData
import android.util.Log
import android.widget.AbsListView
import android.view.View.MeasureSpec
import android.view.MotionEvent
import android.widget.ProgressBar
import android.view.animation.RotateAnimation

class FrontService : Service() {
    @SuppressLint("IconColors")
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.drawable.ic_forwarder)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        if (OSUtil.isMIUI()) {
            builder.setContentTitle(getString(R.string.app_name))
        }
        builder.setContentText(getString(R.string.notification_content))
        val intent = Intent(this, MainActivity::class.java)
        @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            val notificationChannel = NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN)
            notificationChannel.enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false) //是否显示角标
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
            builder.setChannelId(CHANNEL_ONE_ID)
        }
        val notification = builder.build()
        startForeground(1, notification)

        //检查权限是否获取
        //PackageManager pm = getPackageManager();
        //PhoneUtils.CheckPermission(pm, this);

        //Android8.1以下尝试启动主界面，以便动态获取权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        // 手机重启，未打开app时，主动获取SIM卡信息
        if (MyApplication.Companion.SimInfoList.isEmpty()) {
            PhoneUtils.Companion.init(this)
            MyApplication.Companion.SimInfoList = PhoneUtils.Companion.getSimMultiInfo()
        }
        if (SettingUtil.getSwitchEnableAppNotify() && CommonUtil.isNotificationListenerServiceEnabled(this)) {
            CommonUtil.toggleNotificationListenerService(this)
        }
    }

    override fun onDestroy() {
        //进行自动重启
        val intent = Intent(this@FrontService, FrontService::class.java)
        //重新开启服务
        startService(intent)
        stopForeground(true)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY //保证service不被杀死
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    //获取当前电量
    @get:SuppressLint("ObsoleteSdkInt")
    private val batteryLevel: Int
        private get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

    companion object {
        private const val TAG = "FrontService"
        private const val CHANNEL_ONE_ID = "com.idormy.sms.forwarder"
        private const val CHANNEL_ONE_NAME = "com.idormy.sms.forwarderName"
        private const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    }
}