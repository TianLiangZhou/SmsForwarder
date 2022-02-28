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
import com.idormy.sms.forwarder.utils.RuleLineUtils
import com.idormy.sms.forwarder.utils.OSUtil.ROM_TYPE
import com.idormy.sms.forwarder.utils.BuildProperties
import com.idormy.sms.forwarder.utils.OSUtil
import com.idormy.sms.forwarder.utils.DbHelper
import android.database.sqlite.SQLiteDatabase
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
import android.text.TextUtils
import android.util.Log
import com.idormy.sms.forwarder.adapter.SenderAdapter
import com.idormy.sms.forwarder.adapter.AppAdapter
import android.widget.AbsListView
import android.view.View.MeasureSpec
import android.view.MotionEvent
import android.widget.ProgressBar
import android.view.animation.RotateAnimation
import com.idormy.sms.forwarder.model.*
import java.text.SimpleDateFormat
import java.util.*

class PhoneStateReceiver : BroadcastReceiver() {
    private var mTelephonyManager: TelephonyManager? = null
    override fun onReceive(context: Context, intent: Intent) {
        if (!SettingUtil.getSwitchEnablePhone()) {
            return
        }
        val action = intent.action
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED == action) {
            //获取来电号码
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (mTelephonyManager == null) {
                mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            }
            val state = mTelephonyManager!!.callState
            Log.d(TAG, "来电信息：state=$state phoneNumber = $phoneNumber")
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {}
                TelephonyManager.CALL_STATE_IDLE -> if (!TextUtils.isEmpty(phoneNumber)) {
                    try {
                        //必须休眠才能获取来电记录
                        Thread.sleep(1000)
                        sendReceiveCallMsg(context, phoneNumber)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {}
            }
        }
    }

    private fun sendReceiveCallMsg(context: Context, phoneNumber: String?) {

        //获取后一条通话记录
        val callInfo: CallInfo = PhoneUtils.Companion.getLastCallInfo(phoneNumber) ?: return
        if (callInfo.getType() != 3) {
            Log.d(TAG, "非未接来电不处理！")
            return
        }
        var name = callInfo.getName()
        Log.d(TAG, "getSubscriptionId = " + callInfo.getSubscriptionId())
        val simId = SimUtil.getSimIdBySubscriptionId(callInfo.getSubscriptionId())
        var simInfo = if (simId == 2) SettingUtil.getAddExtraSim2() else SettingUtil.getAddExtraSim1() //自定义备注优先
        simInfo = if (!simInfo!!.isEmpty()) {
            "SIM" + simId + "_" + simInfo
        } else {
            SimUtil.getSimInfo(simId)
        }
        if (TextUtils.isEmpty(name)) {
            val contacts: List<PhoneBookEntity> = ContactHelper.Companion.getInstance().getContactByNumber(context, phoneNumber)
            if (contacts != null && contacts.size > 0) {
                val phoneBookEntity = contacts[0]
                name = phoneBookEntity.name
            }
            if (TextUtils.isEmpty(name)) name = context.getString(R.string.unknown_number)
        }

        //TODO:同一卡槽同一秒的重复未接来电广播不再重复处理（部分机型会收到两条广播？）
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date())
        val prevHash = SettingUtil.getPrevNoticeHash(phoneNumber)
        val currHash = CommonUtil.MD5(phoneNumber + simInfo + time)
        Log.d(TAG, "prevHash=$prevHash currHash=$currHash")
        if (prevHash != null && prevHash == currHash) {
            Log.w(TAG, "同一卡槽同一秒的重复未接来电广播不再重复处理（部分机型会收到两条广播）")
            return
        }
        SettingUtil.setPrevNoticeHash(phoneNumber, currHash)
        val smsVo = SmsVo(phoneNumber, name + context.getString(R.string.calling), Date(), simInfo)
        Log.d(TAG, "send_msg$smsVo")
        SendUtil.send_msg(context, smsVo, simId, "call")
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
}