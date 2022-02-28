package com.idormy.sms.forwarder.sender.vo

import kotlinx.serialization.Serializable

@Serializable
data class WebNotifySettingVo(
    var webServer: String = "https://api.example.com/push",
    var secret: String? = null,
    var method: String = "POST",
    var webParams: String = "name=json"
)