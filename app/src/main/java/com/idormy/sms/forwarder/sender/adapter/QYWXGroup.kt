package com.idormy.sms.forwarder.sender.adapter

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.QYWXGroupRobotSettingVo
import io.ktor.client.request.*

object QYWXGroup : SenderInterface<QYWXGroupRobotSettingVo> {

    override suspend fun send(item: QYWXGroupRobotSettingVo, message: Message, logger: Logger?) {
        if (item.webHook.isEmpty()) {
            throw RuntimeException("`webHook` parameter error")
        }
        var url = item.webHook
        if (!url.startsWith("https://", true)) {
            url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=$url"
        }
        val sendBody = "{\"msgtype\":\"text\",\"text\": {\"content\": \"%s\"}}"
        val response: String = Http.client.post(url) {
            body = sendBody.format(message.content?:"")
        }
        if (response.contains("\"errcode\":0")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }
}