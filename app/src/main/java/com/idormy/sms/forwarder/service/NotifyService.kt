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
import android.app.PendingIntent
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
import android.app.Activity
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
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.app.ActivityManager
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
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class NotifyService : NotificationListenerService() {
    /**
     * 发布通知
     *
     * @param sbn 状态栏通知
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        //未开启转发
        if (!SettingUtil.getSwitchEnableAppNotify()) return
        //异常通知跳过
        if (sbn.notification == null) return
        if (sbn.notification.extras == null) return

        //推送通知的应用包名
        val packageName = sbn.packageName
        //自身通知跳过
        if ("com.idormy.sms.forwarder" == packageName) return
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

            //重复通知不再处理
            val prevHash = SettingUtil.getPrevNoticeHash(packageName)
            val currHash = CommonUtil.MD5(packageName + title + text + time)
            Log.d(TAG, "prevHash=$prevHash currHash=$currHash")
            if (prevHash != null && prevHash == currHash) {
                Log.w(TAG, "重复通知不再处理")
                return
            }
            SettingUtil.setPrevNoticeHash(packageName, currHash)
            val smsVo = SmsVo(packageName, text, Date(), title)
            Log.d(TAG, "send_msg$smsVo")
            SendUtil.send_msg(this, smsVo, 1, "app")
        } catch (e: Exception) {
            Log.e(TAG, "onNotificationPosted:", e)
        }
        //NotifyHelper.getInstance().onReceive(sbn);
    }

    /**
     * 通知已删除
     *
     * @param sbn 状态栏通知
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        //未开启转发
        if (!SettingUtil.getSwitchEnableAppNotify()) return
        //异常通知跳过
        if (sbn.notification == null) return
        Log.d(TAG, sbn.packageName)

        //NotifyHelper.getInstance().onRemoved(sbn);
    }

    /**
     * 监听断开
     */
    override fun onListenerDisconnected() {
        //未开启转发
        if (!SettingUtil.getSwitchEnableAppNotify()) return
        Log.d(TAG, "通知侦听器断开连接 - 请求重新绑定")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    companion object {
        const val TAG = "NotifyService"
    }
}