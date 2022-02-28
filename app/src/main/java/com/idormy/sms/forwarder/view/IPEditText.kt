package com.idormy.sms.forwarder.view

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
import android.util.AttributeSet
import android.view.*
import com.idormy.sms.forwarder.adapter.SenderAdapter
import com.idormy.sms.forwarder.adapter.AppAdapter
import android.widget.AbsListView
import android.view.View.MeasureSpec
import android.widget.ProgressBar
import android.view.animation.RotateAnimation
import java.util.regex.Pattern

class IPEditText(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    //控件
    private val Edit1: EditText
    private val Edit2: EditText
    private val Edit3: EditText
    private val Edit4: EditText
    private var ip1: String? = null
    private var ip2: String? = null
    private var ip3: String? = null
    private var ip4: String? = null

    init {
        //初始化界面
        val view = LayoutInflater.from(context).inflate(R.layout.iptext, this)
        //绑定
        Edit1 = findViewById(R.id.edit1)
        Edit2 = findViewById(R.id.edit2)
        Edit3 = findViewById(R.id.edit3)
        Edit4 = findViewById(R.id.edit4)
        //初始化函数
        init(context)
    }

    private fun init(context: Context) {
        /*
          监听文本，得到ip段，自动进入下一个输入框
         */
        Edit1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                ip1 = s.toString().trim { it <= ' ' }
                val lenIp1 = ip1!!.length
                if (lenIp1 > 0 && !Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.?$", ip1)) {
                    ip1 = ip1!!.substring(0, lenIp1 - 1)
                    Edit1.setText(ip1)
                    Edit1.setSelection(ip1!!.length)
                    Toast.makeText(context, R.string.invalid_ip, Toast.LENGTH_LONG).show()
                    return
                }
                //非空输入 . 跳到下一个输入框
                if (lenIp1 > 1 && "." == ip1!!.substring(lenIp1 - 1)) {
                    ip1 = ip1!!.substring(0, lenIp1 - 1)
                    Edit1.setText(ip1)
                    Edit2.isFocusable = true
                    Edit2.requestFocus()
                    return
                }
                //已输3位数字，跳到下一个输入框
                if (lenIp1 > 2) {
                    Edit2.isFocusable = true
                    Edit2.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        Edit2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                ip2 = s.toString().trim { it <= ' ' }
                val lenIp2 = ip2!!.length
                if (lenIp2 > 0 && !Pattern.matches("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.?$", ip2)) {
                    ip2 = ip2!!.substring(0, lenIp2 - 1)
                    Edit2.setText(ip2)
                    Edit2.setSelection(ip2!!.length)
                    Toast.makeText(context, R.string.invalid_ip, Toast.LENGTH_LONG).show()
                    return
                }
                //非空输入 . 跳到下一个输入框
                if (lenIp2 > 1 && "." == ip2!!.substring(lenIp2 - 1)) {
                    ip2 = ip2!!.substring(0, lenIp2 - 1)
                    Edit2.setText(ip2)
                    Edit3.isFocusable = true
                    Edit3.requestFocus()
                    return
                }
                //已输3位数字，跳到下一个输入框
                if (lenIp2 > 2) {
                    Edit3.isFocusable = true
                    Edit3.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        Edit3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                ip3 = s.toString().trim { it <= ' ' }
                val lenIp3 = ip3!!.length
                if (lenIp3 > 0 && !Pattern.matches("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.?$", ip3)) {
                    ip3 = ip3!!.substring(0, lenIp3 - 1)
                    Edit3.setText(ip3)
                    Edit3.setSelection(ip3!!.length)
                    Toast.makeText(context, R.string.invalid_ip, Toast.LENGTH_LONG).show()
                    return
                }
                //非空输入 . 跳到下一个输入框
                if (lenIp3 > 1 && "." == ip3!!.substring(lenIp3 - 1)) {
                    ip3 = ip3!!.substring(0, lenIp3 - 1)
                    Edit3.setText(ip3)
                    Edit4.isFocusable = true
                    Edit4.requestFocus()
                    return
                }
                //已输3位数字，跳到下一个输入框
                if (lenIp3 > 2) {
                    Edit4.isFocusable = true
                    Edit4.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        Edit4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                ip4 = s.toString().trim { it <= ' ' }
                val lenIp4 = ip4!!.length
                if (lenIp4 > 0 && !Pattern.matches("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])$", ip4)) {
                    ip4 = ip4!!.substring(0, lenIp4 - 1)
                    Edit4.setText(ip4)
                    Edit4.setSelection(ip4!!.length)
                    Toast.makeText(context, R.string.invalid_ip, Toast.LENGTH_LONG).show()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        /*
           监听控件，空值时del键返回上一输入框
         */Edit2.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent? ->
            if (ip2 == null || ip2!!.isEmpty()) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    Edit1.isFocusable = true
                    Edit1.requestFocus()
                    Edit1.setSelection(ip1!!.length)
                }
            }
            false
        }
        Edit3.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent? ->
            if (ip3 == null || ip3!!.isEmpty()) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    Edit2.isFocusable = true
                    Edit2.requestFocus()
                    Edit2.setSelection(ip2!!.length)
                }
            }
            false
        }
        Edit4.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent? ->
            if (ip4 == null || ip4!!.isEmpty()) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    Edit3.isFocusable = true
                    Edit3.requestFocus()
                    Edit3.setSelection(ip3!!.length)
                }
            }
            false
        }
    }//文本
    /**
     * 成员函数，返回整个ip地址
     */
    /**
     * 成员函数，返回整个ip地址
     */
    var iP: String?
        get() {
            //文本
            val text: String?
            text = if (TextUtils.isEmpty(ip1) || TextUtils.isEmpty(ip2)
                || TextUtils.isEmpty(ip3) || TextUtils.isEmpty(ip4)
            ) {
                null
            } else {
                "$ip1.$ip2.$ip3.$ip4"
            }
            return text
        }
        set(ip) {
            if (ip == null || ip.isEmpty()
                || !Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ip)
            ) {
                ip1 = ""
                ip2 = ""
                ip3 = ""
                ip4 = ""
            } else {
                val ips = ip.split("\\.").toTypedArray()
                ip1 = ips[0]
                ip2 = ips[1]
                ip3 = ips[2]
                ip4 = ips[3]
            }
            Edit1.setText(ip1)
            Edit2.setText(ip2)
            Edit3.setText(ip3)
            Edit4.setText(ip4)
        }
}