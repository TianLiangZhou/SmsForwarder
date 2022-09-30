package com.idormy.sms.forwarder.sender.adapter

import android.util.Log
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.ServerChanSettingVo
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*


object ServerChan : SenderInterface<ServerChanSettingVo> {
    override suspend fun send(item: ServerChanSettingVo, message: Message, logger: Logger?) {
        if (item.sendKey == null || item.sendKey!!.isEmpty()) {
            throw RuntimeException("`sendKey` parameter error")
        }
        var url = item.sendKey!!
        if (!url.startsWith("https://", true)) {
            url = "https://sctapi.ftqq.com/$url.send"
        }
        val response: String = Http.client.submitForm(
            url,
            Parameters.build {
                append("title", message.source?:"")
                append("desp", message.content?:"")
            },
            false
        ).body()
        Log.d("Http Chan = ", response)
        if (response.contains("\"code\":0")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }
}