package com.idormy.sms.forwarder.receiver

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
import android.provider.CallLog
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
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.idormy.sms.forwarder.service.BatteryService
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
import android.content.*
import android.telephony.SmsMessage
import android.util.Log
import com.idormy.sms.forwarder.adapter.SenderAdapter
import com.idormy.sms.forwarder.adapter.AppAdapter
import android.widget.AbsListView
import android.view.View.MeasureSpec
import android.view.MotionEvent
import android.widget.ProgressBar
import android.view.animation.RotateAnimation
import java.lang.Exception
import java.util.*

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receiveAction = intent.action
        val TAG = "SmsBroadcastReceiver"
        Log.d(TAG, "onReceive intent $receiveAction")
        if ("android.provider.Telephony.SMS_RECEIVED" == receiveAction) {
            try {
                if (!SettingUtil.getSwitchEnableSms()) {
                    return
                }
                val extras = intent.extras
                val `object` = Objects.requireNonNull(extras)["pdus"] as Array<Any>?
                if (`object` != null) {

                    //接收手机卡信息
                    var simInfo: String? = ""
                    //卡槽ID，默认卡槽为1
                    var simId = 1
                    try {
                        if (extras!!.containsKey("simId")) {
                            simId = extras.getInt("simId")
                        } else if (extras.containsKey("subscription")) {
                            simId = SimUtil.getSimIdBySubscriptionId(extras.getInt("subscription"))
                        }

                        //自定义备注优先
                        simInfo = if (simId == 2) SettingUtil.getAddExtraSim2() else SettingUtil.getAddExtraSim1()
                        simInfo = if (!simInfo!!.isEmpty()) {
                            "SIM" + simId + "_" + simInfo
                        } else {
                            SimUtil.getSimInfo(simId)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "获取接收手机号失败：" + e.message)
                    }
                    val smsVoList: MutableList<SmsVo> = ArrayList()
                    val format = intent.getStringExtra("format")
                    val mobileToContent: MutableMap<String, String> = HashMap()
                    var date: Date? = Date()
                    for (pdus in `object`) {
                        val pdusMsg = pdus as ByteArray
                        val sms = SmsMessage.createFromPdu(pdusMsg, format)
                        val mobile = sms.originatingAddress ?: continue //发送短信的手机号
                        //下面是获取短信的发送时间
                        date = Date(sms.timestampMillis)
                        var content = mobileToContent[mobile]
                        if (content == null) content = ""
                        content += sms.messageBody.trim { it <= ' ' } //短信内容
                        mobileToContent[mobile] = content
                    }
                    for (mobile in mobileToContent.keys) {
                        smsVoList.add(SmsVo(mobile, mobileToContent[mobile], date, simInfo))
                    }
                    Log.d(TAG, "短信：$smsVoList")
                    SendUtil.send_msg_list(context, smsVoList, simId, "sms")
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "解析短信失败：" + throwable.message)
            }
        }
    }
}