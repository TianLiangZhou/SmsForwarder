package com.idormy.sms.forwarder.data

@kotlinx.serialization.Serializable
data class FeiShuSettingVo(
    var webhook: String = "https://open.feishu.cn/open-apis/bot/v2/hook/xxx",
    var secret: String? = null
    )