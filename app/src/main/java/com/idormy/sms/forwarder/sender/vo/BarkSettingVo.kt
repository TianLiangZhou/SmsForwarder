package com.idormy.sms.forwarder.sender.vo

import kotlinx.serialization.Serializable

@Serializable
data class BarkSettingVo(
    var server: String = "https://bark.bms.ink/example/",
    var icon: String? = null
)