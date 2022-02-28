package com.idormy.sms.forwarder.sender.vo

import kotlinx.serialization.Serializable

@Serializable
data class PushPlusSettingVo(
    var token: String = "token",
    var topic: String? = null,
    var template: String = "txt",
    var channel: String = "wechat",
    var webhook: String? = null,
    var callbackUrl: String? = null,
    var validTime: String? = "30"
)