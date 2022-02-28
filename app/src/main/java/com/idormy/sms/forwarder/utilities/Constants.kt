package com.idormy.sms.forwarder.utilities

object Key {

    const val DB = "sms_forwarder.db"

    const val PACKAGE_NAME = "com.idormy.sms.forwarder"

    const val name = "name"

    // sender preference key
    const val server = "server"

    const val icon = "icon"

    const val token = "token"

    const val secret = "secret"

    const val atMobile = "atMobiles"

    const val atAll = "atAll"

    const val webHook = "webhook"

    const val protocol = "protocol"

    const val host = "host"

    const val port = "port"

    const val ssl = "ssl"

    const val fromEmail = "fromEmail"

    const val toEmail = "toEmail"

    const val username = "username"

    const val password = "pwd"

    const val subject = "subject"

    const val topic = "topic"

    const val template = "template"

    const val channel = "channel"

    const val callbackUrl = "callbackUrl"

    const val validTime = "validTime"

    const val corpId = "corpID"

    const val agentId = "agentID"

    const val toUser = "toUser"

    const val accessToken = "accessToken"

    const val expiresIn = "expiresIn"

    const val sendKey = "sendKey"

    const val simSlot = "simSlot"

    const val mobiles = "mobiles"

    const val method = "method"

    const val params = "params"

    const val chatId = "chatId"

    const val authenticator = "authenticator"



    // rule preference key

    const val category = "category"

    const val field = "field"

    const val mode = "mode"

    const val value = "value"

    const val sender = "sender"

    const val smsTemplate = "sms_template"

    const val regexReplace = "regex_replace"

    // system setting
    const val deviceName = "device_name"

    const val switchSms = "switch_sms"

    const val switchCall = "switch_call"

    const val switchNotify = "switch_notify"

    const val switchCustomTemplate = "switch_custom_template"

    const val customTemplate = "custom_template"

    const val retryInterval = "retry_interval"

    const val powerAlarm = "power_alarm"

    const val switchMonitorBattery = "switch_monitor_battery"

    const val switchExcludeRecent = "switch_exclude_recent"

    const val isAutoStartup = "is_auto_startup"

    const val directBootAware = "direct_boot_aware"
}

object Action {

    const val position = "position"

    const val title = "title"

    const val senderId = "sender_id"

    const val ruleId = "rule_id"

    const val senders = "senders"

    const val tips = "tips"
}


object Worker {
    const val updateSender = "update_sender"

    const val sendMessage = "send_message"
}

object RuleKey {
    const val FILED_PHONE_NUM = "phone_num"
    const val FILED_PACKAGE_NAME = "package_name"
    const val FILED_MSG_CONTENT = "msg_content"
    const val FILED_INFORM_CONTENT = "inform_content"
}

enum class NetworkMode(val value: Int) {
    None(0),
    Cellular(1),
    Wifi(2),
    VPN(3),
    BlueTooth(4),
    Ethernet(5);
}

enum class Status(val value: Int) {
    Off(0),
    On(1);
}
enum class MessageType(val value: String) {
    Sms("sms"),
    Call("call"),
    App("app");

    companion object {
        fun from(value: String) = values().first { it.value == value }
    }
}

enum class Mode(val value: String) {
    Is("is"),
    No("notis"),
    Contain("contain"),
    NotContain("notcontain"),
    Start("startwith"),
    End("startend"),
    Regex("regex");

    companion object {
        fun from(value: String) = values().first { it.value == value }
    }
}