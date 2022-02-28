package com.idormy.sms.forwarder.sender.vo

import kotlinx.serialization.Serializable

@Serializable
data class EmailSettingVo(
    val protocol: String = "smtp",
    val host: String = "smtp.gmail.com",
    val port: String = "465",
    val ssl: Boolean = false,
    val fromEmail: String = "example@gmail.com",
    val nickname: String = "example name",
    val pwd: String = "",
    val toEmail: String = "to_example@gmail.com",
    val title: String = "forward subject"
)