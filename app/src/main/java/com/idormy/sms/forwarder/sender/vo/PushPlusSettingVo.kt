package com.idormy.sms.forwarder.data

data class PushPlusSettingVo(
    var token: String = "http://www.pushplus.plus/send/{token}",
    var topic: String? = null,
    var template: String = "txt",
    var channel: String = "wechat",
    var webhook: String? = null,
    var callbackUrl: String? = null,
    var validTime: String? = null
    )