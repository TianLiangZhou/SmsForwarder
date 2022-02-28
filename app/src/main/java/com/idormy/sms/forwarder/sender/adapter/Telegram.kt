package com.idormy.sms.forwarder.sender.adapter

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.TelegramSettingVo
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class TelegramSendBody(val chat_id: String, val text:String, val parse_mode:String)

object Telegram : SenderInterface<TelegramSettingVo> {
    override suspend fun send(item: TelegramSettingVo, message: Message, logger: Logger?) {
        if (item.apiToken.isEmpty() || item.chatId.isEmpty()) {
            throw RuntimeException("`apiToken` or `chatId` parameter error")
        }

        var url = item.apiToken
        if (!url.startsWith("https://", true)) {
            url = "https://api.telegram.org/bot$url/sendMessage"
        }
        var proxy: ProxyConfig? = null
        when(item.protocol) {
            "http" -> {
                var proxyUrl = item.proxyHost
                if (!proxyUrl.startsWith("https://", true) && !proxyUrl.startsWith("http://", true)) {
                    proxyUrl = "http://$proxyUrl"
                }
                if (item.proxyPort.isNotEmpty() && item.proxyPort != "80" && item.proxyPort != "443") {
                    proxyUrl += ":" + item.proxyPort
                }
                proxy = ProxyBuilder.http(item.proxyPort)
            }
            "socks" -> {
                proxy = ProxyBuilder.socks(item.proxyHost, item.proxyPort.toInt())
            }
        }
        try {
            if (proxy != null) {
                Http.client.engine.config.proxy = proxy
            }
            val response: String = Http.client.post(url) {
                contentType(ContentType.Application.Json)
                body = TelegramSendBody(item.chatId, message.content?:"", "HTML")
            }
            if (response.contains("\"ok\":true")) {
                logger?.forwardStatus = ResponseState.Success.value
            }
            logger?.forwardResponse = response
        } catch (cause: java.net.SocketTimeoutException) {
            logger?.forwardStatus = ResponseState.Failure.value
            logger?.forwardResponse = cause.message ?: "connection timeout"
        }
        if (proxy != null) {
            Http.client.engine.config.proxy = null
        }
    }
}