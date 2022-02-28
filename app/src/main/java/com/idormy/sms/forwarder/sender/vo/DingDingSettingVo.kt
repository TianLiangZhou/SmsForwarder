package com.idormy.sms.forwarder.data

import kotlinx.serialization.Serializable

@Serializable
data class DingDingSettingVo(
    val token: String = "https://oapi.dingtalk.com/robot/send?access_token=XXXXXX",
    val secret: String = "",
    val atMobiles: String = "18888888888;19999999999",
    val atAll: Boolean = false
)