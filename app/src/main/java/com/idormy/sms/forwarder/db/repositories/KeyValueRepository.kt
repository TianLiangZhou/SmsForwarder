package com.idormy.sms.forwarder.db.repositories

import android.os.Parcelable
import androidx.preference.PreferenceDataStore
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.preference.OnPreferenceDataStoreChangeListener
import com.idormy.sms.forwarder.preference.RoomPreferenceDataStore
import com.idormy.sms.forwarder.sender.Types
import com.idormy.sms.forwarder.sender.vo.*
import com.idormy.sms.forwarder.utilities.Key

class KeyValueRepository(val store: RoomPreferenceDataStore) : OnPreferenceDataStoreChangeListener {

    var changed = false

    init {
        store.registerChangeListener(this)
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {

    }

    fun initAppSetting() {
        if (store.getBoolean(Key.switchSms) == null) switchSms = switchSms
        if (store.getBoolean(Key.switchCall) == null) switchCall = switchCall
        if (store.getBoolean(Key.switchNotify) == null) switchNotify = switchNotify
        if (store.getBoolean(Key.isAutoStartup) == null) isAutoStartup = isAutoStartup
    }

    fun serialize(item: Parcelable) {
        when (item) {
            is Rule -> {
                name = item.name
                category = item.category
                filed = item.filed
                mode = item.mode
                value = item.value
                senderId = item.senderId
                simSolt = item.simSlot
            }
            is Sender -> {
                name = item.name
                when (Types.from(item.type)) {
                    Types.DingDing -> {
                        val json = item.decodeSetting<DingDingSettingVo>() ?: DingDingSettingVo()
                        token = json.token
                        secret = json.secret?:""
                        atMobile = json.atMobiles?:""
                        atAll = json.atAll
                    }
                    Types.Email -> {
                        val json = item.decodeSetting<EmailSettingVo>() ?: EmailSettingVo()
                        protocol = json.protocol
                        host = json.host
                        port = json.port
                        ssl = json.ssl
                        fromEmail = json.fromEmail
                        username = json.nickname
                        password = json.pwd
                        toEmail = json.toEmail
                        subject = json.title
                    }
                    Types.Bark -> {
                        val json = item.decodeSetting<BarkSettingVo>() ?: BarkSettingVo()
                        server = json.server
                        icon = json.icon?:""
                    }
                    Types.WebNotify -> {
                        val json = item.decodeSetting<WebNotifySettingVo>() ?: WebNotifySettingVo()
                        server = json.webServer
                        secret = json.secret?:""
                        method = json.method
                        params = json.webParams
                    }
                    Types.QYWXGroup -> {
                        val json = item.decodeSetting<QYWXGroupRobotSettingVo>() ?: QYWXGroupRobotSettingVo()
                        webHook = json.webHook
                    }
                    Types.QYWX -> {
                        val json = item.decodeSetting<QYWXAppSettingVo>() ?: QYWXAppSettingVo()
                        corpId = json.corpID
                        agentId = json.agentID
                        secret = json.secret
                        toUser = json.toUser
                        atAll = json.atAll
                        accessToken = json.accessToken?:""
                        expiresIn = json.expiresIn?:0L
                    }
                    Types.ServerChan -> {
                        val json = item.decodeSetting<ServerChanSettingVo>() ?: ServerChanSettingVo()
                        sendKey = json.sendKey?:""
                    }
                    Types.Telegram -> {
                        val json = item.decodeSetting<TelegramSettingVo>() ?: TelegramSettingVo()
                        token = json.apiToken
                        chatId = json.chatId
                        protocol = json.protocol
                        host = json.proxyHost
                        port = json.proxyPort
                        authenticator = json.proxyAuthenticator
                        username = json.proxyUsername?:""
                        password = json.proxyPassword?:""
                    }
                    Types.SMS -> {
                        val json = item.decodeSetting<SmsSettingVo>() ?: SmsSettingVo()
                        var sim = "ALL"
                        if (json.simSlot > 0) {
                            sim = "SIM" + json.simSlot.toString()
                        }
                        simSolt = sim
                        mobiles = json.mobiles
                    }
                    Types.FeiShu -> {
                        val json = item.decodeSetting<FeiShuSettingVo>() ?: FeiShuSettingVo()
                        webHook = json.webhook
                        secret = json.secret?:""
                    }
                    Types.PushPlus -> {
                        val json = item.decodeSetting<PushPlusSettingVo>() ?: PushPlusSettingVo()
                        token = json.token
                        topic = json.topic?:""
                        template = json.template
                        channel = json.channel
                        webHook = json.webhook?:""
                        callbackUrl = json.callbackUrl ?:""
                        validTime = json.validTime?:""
                    }

                    else -> {}
                }
            }
        }
    }



    fun deserialize(item: Parcelable) {
        when (item) {
            is Rule -> {
                item.name = name
                item.category = category
                item.simSlot = simSolt
                item.filed = filed
                item.mode = mode
                item.value = value
                item.senderId = senderId
                item.smsTemplate = smsTemplate
                item.regexReplace = regexReplace
            }
            is Sender -> {
                item.name = name
                when (Types.from(item.type)) {
                    Types.DingDing -> {
                        item.setting = DingDingSettingVo(token, secret, atMobile, atAll)
                    }
                    Types.Email -> {
                        item.setting = EmailSettingVo(protocol, host, port, ssl, fromEmail, username, password, toEmail, subject)
                    }
                    Types.Bark -> {
                        item.setting = BarkSettingVo(server, icon)
                    }
                    Types.WebNotify -> {
                        item.setting = WebNotifySettingVo(server, secret, method, params)
                    }
                    Types.QYWXGroup -> {
                        item.setting = QYWXGroupRobotSettingVo(webHook)
                    }
                    Types.QYWX -> {
                        item.setting = QYWXAppSettingVo(corpId, agentId, secret, toUser, atAll, accessToken, expiresIn)
                    }
                    Types.ServerChan -> {
                        item.setting = ServerChanSettingVo(sendKey)
                    }
                    Types.Telegram -> {
                        item.setting = TelegramSettingVo(token, chatId, protocol, host, port, authenticator, username, password)
                    }
                    Types.SMS -> {
                        val sim = store.getString(Key.simSlot, "ALL")
                        var index = 0
                        if (sim != null) {
                            if (sim != "ALL" && sim.isNotEmpty()) {
                                index = sim.substring(3).toInt()
                            }
                        }
                        item.setting = SmsSettingVo(index, mobiles, false)
                    }
                    Types.FeiShu -> {
                        item.setting = FeiShuSettingVo(webHook, secret)
                    }
                    Types.PushPlus -> {
                        item.setting = PushPlusSettingVo(token, topic, template, channel, callbackUrl, validTime)
                    }

                    else -> {}
                }
            }
            else -> {}
        }
    }


    var name: String
        get() = store.getString(Key.name) ?: ""
        set(value) = store.putString(Key.name, value)

    var server: String
        get() = store.getString(Key.server) ?: ""
        set(value) = store.putString(Key.server, value)

    var icon: String
        get() = store.getString(Key.icon) ?: ""
        set(value) = store.putString(Key.icon, value)

    var token: String
        get() = store.getString(Key.token) ?: ""
        set(value) = store.putString(Key.token, value)


    var secret: String
        get() = store.getString(Key.secret) ?: ""
        set(value) = store.putString(Key.secret, value)

    var atMobile: String
        get() = store.getString(Key.atMobile) ?: ""
        set(value) = store.putString(Key.atMobile, value)


    var atAll: Boolean
        get() = store.getBoolean(Key.atAll, false)
        set(value) = store.putBoolean(Key.atAll, value)

    var authenticator: Boolean
        get() = store.getBoolean(Key.authenticator, false)
        set(value) = store.putBoolean(Key.authenticator, value)


    var protocol: String
        get() = store.getString(Key.protocol) ?: ""
        set(value) = store.putString(Key.protocol, value)


    var webHook: String
        get() = store.getString(Key.webHook) ?: ""
        set(value) = store.putString(Key.webHook, value)

    var host: String
        get() = store.getString(Key.host) ?: ""
        set(value) = store.putString(Key.host, value)

    var port: String
        get() = store.getString(Key.port) ?: ""
        set(value) = store.putString(Key.port, value)

    var ssl: Boolean
        get() = store.getBoolean(Key.ssl, false)
        set(value) = store.putBoolean(Key.ssl, value)

    var fromEmail: String
        get() = store.getString(Key.fromEmail) ?: ""
        set(value) = store.putString(Key.fromEmail, value)

    var toEmail: String
        get() = store.getString(Key.toEmail) ?: ""
        set(value) = store.putString(Key.toEmail, value)

    var username: String
        get() = store.getString(Key.username) ?: ""
        set(value) = store.putString(Key.username, value)

    var password: String
        get() = store.getString(Key.password) ?: ""
        set(value) = store.putString(Key.password, value)

    var subject: String
        get() = store.getString(Key.subject) ?: ""
        set(value) = store.putString(Key.subject, value)

    var topic: String
        get() = store.getString(Key.topic) ?: ""
        set(value) = store.putString(Key.topic, value)

    var template: String
        get() = store.getString(Key.template) ?: ""
        set(value) = store.putString(Key.template, value)


    var channel: String
        get() = store.getString(Key.channel) ?: ""
        set(value) = store.putString(Key.channel, value)

    var callbackUrl: String
        get() = store.getString(Key.callbackUrl) ?: ""
        set(value) = store.putString(Key.callbackUrl, value)

    var validTime: String
        get() = store.getString(Key.validTime) ?: ""
        set(value) = store.putString(Key.validTime, value)

    var corpId: String
        get() = store.getString(Key.corpId) ?: ""
        set(value) = store.putString(Key.corpId, value)

    var agentId: String
        get() = store.getString(Key.agentId) ?: ""
        set(value) = store.putString(Key.agentId, value)

    var toUser: String
        get() = store.getString(Key.toUser) ?: ""
        set(value) = store.putString(Key.toUser, value)

    var accessToken: String
        get() = store.getString(Key.accessToken) ?: ""
        set(value) = store.putString(Key.accessToken, value)

    var expiresIn: Long
        get() = store.getLong(Key.expiresIn, 0)
        set(value) = store.putLong(Key.expiresIn, value)

    var sendKey: String
        get() = store.getString(Key.sendKey) ?: ""
        set(value) = store.putString(Key.sendKey, value)

    var simSolt: String
        get() = store.getString(Key.simSlot) ?: ""
        set(value) = store.putString(Key.simSlot, value)

    var mobiles: String
        get() = store.getString(Key.mobiles) ?: ""
        set(value) = store.putString(Key.mobiles, value)

    var method: String
        get() = store.getString(Key.method) ?: ""
        set(value) = store.putString(Key.method, value)

    var params: String
        get() = store.getString(Key.params) ?: ""
        set(value) = store.putString(Key.params, value)

    var chatId: String
        get() = store.getString(Key.chatId) ?: ""
        set(value) = store.putString(Key.chatId, value)

    // rule variable
    var filed: String
        get() = store.getString(Key.field) ?: ""
        set(value) = store.putString(Key.field, value)
    var category: String
        get() = store.getString(Key.category) ?: ""
        set(value) = store.putString(Key.category, value)
    var mode: String
        get() = store.getString(Key.mode) ?: ""
        set(value) = store.putString(Key.mode, value)
    var value: String
        get() = store.getString(Key.value) ?: ""
        set(value) = store.putString(Key.value, value)
    var senderId: Long
        get() = (store.getString(Key.sender) ?: "0").toLong()
        set(value) = store.putString(Key.sender, value.toString())
    var smsTemplate: String
        get() = store.getString(Key.smsTemplate) ?: ""
        set(value) = store.putString(Key.smsTemplate, value)

    var regexReplace: String
        get() = store.getString(Key.regexReplace) ?: ""
        set(value) = store.putString(Key.regexReplace, value)

    // system setting
    var deviceName: String
        get() = store.getString(Key.deviceName) ?: ""
        set(value) = store.putString(Key.deviceName, value)
    var switchSms: Boolean
        get() = store.getBoolean(Key.switchSms, true)
        set(value) = store.putBoolean(Key.switchSms, value)
    var switchCall: Boolean
        get() = store.getBoolean(Key.switchCall, true)
        set(value) = store.putBoolean(Key.switchCall, value)
    var switchNotify: Boolean
        get() = store.getBoolean(Key.switchNotify, true)
        set(value) = store.putBoolean(Key.switchNotify, value)
    var switchCustomTemplate: Boolean
        get() = store.getBoolean(Key.switchCustomTemplate, false)
        set(value) = store.putBoolean(Key.switchCustomTemplate, value)
    var retryInterval: Long
        get() = store.getLong(Key.retryInterval, 0)
        set(value) = store.putLong(Key.retryInterval, value)

    var powerAlarm: Long
        get() = store.getLong(Key.powerAlarm, 0)
        set(value) = store.putLong(Key.powerAlarm, value)

    var switchMonitorBattery: Boolean
        get() = store.getBoolean(Key.switchMonitorBattery, false)
        set(value) = store.putBoolean(Key.switchMonitorBattery, value)

    var switchExcludeRecent: Boolean
        get() = store.getBoolean(Key.switchExcludeRecent, false)
        set(value) = store.putBoolean(Key.switchExcludeRecent, value)

    var isAutoStartup: Boolean
        get() = store.getBoolean(Key.isAutoStartup, false)
        set(value) = store.putBoolean(Key.isAutoStartup, value)

    val canToggleLocked: Boolean get() = store.getBoolean(Key.directBootAware) == true
}