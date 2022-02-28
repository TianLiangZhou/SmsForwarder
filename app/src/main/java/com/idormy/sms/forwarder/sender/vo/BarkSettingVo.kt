package com.idormy.sms.forwarder.data

import kotlinx.serialization.Serializable

@Serializable
data class BarkSettingVo(
    var server: String = "https://bark.bms.ink/example/",
    var icon: String = "https://day.app/assets/images/avatar.jpg"
)