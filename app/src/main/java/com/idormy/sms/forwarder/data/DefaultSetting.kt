package com.idormy.sms.forwarder.data

data class Setting(
    var deviceName: String =  "",
    var switchSms: Boolean = true,
    var switchCall: Boolean = true,
    var switchNotify: Boolean = true,
    var switchCustomTemplate: Boolean = false,
    var retryInterval: Long = 0,
    var powerAlarm: Long  = 0,
    var switchMonitorBattery: Boolean = false,
    var switchExcludeRecent: Boolean = false,
    var customTemplate: String = "",
)
