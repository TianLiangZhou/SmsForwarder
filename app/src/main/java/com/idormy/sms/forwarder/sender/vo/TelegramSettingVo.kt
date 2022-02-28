package com.idormy.sms.forwarder.sender.vo

import kotlinx.serialization.Serializable

@Serializable
data class TelegramSettingVo(
    var apiToken: String = "bot123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11",
    var chatId: String = "chat id",
    var protocol: String = "none",
    var proxyHost: String = "127.0.0.1",
    var proxyPort: String = "1086",
    var proxyAuthenticator: Boolean = false,
    var proxyUsername: String? = null,
    var proxyPassword: String? = null
)
