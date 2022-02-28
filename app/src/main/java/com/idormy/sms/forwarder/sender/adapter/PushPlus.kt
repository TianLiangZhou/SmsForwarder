package com.idormy.sms.forwarder.sender.adapter

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.PushPlusSettingVo
import io.ktor.client.request.*


object PushPlus : SenderInterface<PushPlusSettingVo> {
    override suspend fun send(item: PushPlusSettingVo, message: Message, logger: Logger?) {
        if (item.token.isEmpty()) {
            throw RuntimeException("`token` parameter error")
        }
        val url = "http://www.pushplus.plus/send"
        var timestamp = System.currentTimeMillis()
        timestamp += if (item.validTime != null && item.validTime!!.isNotEmpty()) {
            item.validTime!!.toInt() * 1000
        } else {
            30 * 1000
        }
        val sendBody = """
            {"token": "%s", "title": "%s", "content": "%s", "topic": "%s", "template": "%s", "channel": "%s", "webhook": "%s", "callbackUrl":"%s", "timestamp":%d}
        """.trimIndent()
        val response: String = Http.client.post(url) {
            body = sendBody.format(
                item.token,
                message.source?:"",
                message.content?:"",
                item.topic ?: "",
                item.template,
                item.channel,
                item.webhook ?: "",
                item.callbackUrl ?: "",
                timestamp
            )
        }
        if (response.contains("\"code\":200")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }
}