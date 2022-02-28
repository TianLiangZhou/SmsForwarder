package com.idormy.sms.forwarder.data

import com.idormy.sms.forwarder.R
import kotlinx.serialization.Serializable
import java.net.Proxy

@Serializable
data class TelegramSettingVo(
    var apiToken: String = "bot123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11",
    var chatId: String = "chat id",
    var protocol: String = "none",
    var proxyHost: String = "127.0.0.1",
    var proxyPort: String = "1086",
    var proxyAuthenticator: Boolean = false,
    var proxyUsername: String? = null,
    var proxyPassword: String? = null) {
    var proxyType = Proxy.Type.DIRECT
    var proxyTypeId = 0
    init {
        proxyType = when (proxyTypeId) {
            R.id.btnProxyHttp -> Proxy.Type.HTTP
            R.id.btnProxySocks -> Proxy.Type.SOCKS
            else -> Proxy.Type.DIRECT
        }
    }

    val proxyTypeCheckId: Int
        get() = when (proxyType) {
            Proxy.Type.HTTP -> R.id.btnProxyHttp
            Proxy.Type.SOCKS -> R.id.btnProxySocks
            else -> R.id.btnProxyNone
        }
}