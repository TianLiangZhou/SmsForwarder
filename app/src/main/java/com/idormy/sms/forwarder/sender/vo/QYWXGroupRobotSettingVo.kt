package com.idormy.sms.forwarder.sender.vo

@kotlinx.serialization.Serializable
data class QYWXGroupRobotSettingVo(
    var webHook: String = "https://qyapi.weixin.qq.com/cgi-bin/"
    )