package com.idormy.sms.forwarder.data

@kotlinx.serialization.Serializable
data class QYWXGroupRobotSettingVo(
    var webHook: String = "https://qyapi.weixin.qq.com/cgi-bin/"
    )