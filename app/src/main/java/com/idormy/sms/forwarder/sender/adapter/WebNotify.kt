package com.idormy.sms.forwarder.sender.adapter

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.WebNotifySettingVo
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*

object WebNotify : SenderInterface<WebNotifySettingVo> {
    override suspend fun send(item: WebNotifySettingVo, message: Message, logger: Logger?) {
        if (item.webServer.isEmpty()) {
            throw RuntimeException("`webServer` parameter is empty")
        }
        if  (!item.webServer.startsWith("http://") && !item.webServer.startsWith("https://")) {
            throw RuntimeException("`webServer` parameter error")
        }
        var params = item.webParams
        if (item.webParams.contains("[msg]")) {
            params = item.webParams.replace("[msg]", message.content?:"", true)
        }
        val httpMethod = HttpMethod.parse(item.method)
        val contentType = ContentType.Application.FormUrlEncoded
        if (httpMethod != HttpMethod.Get && item.webParams.startsWith("{")) {
            ContentType.Application.Json
        }
        val response: HttpResponse = Http.client.request(item.webServer) {
            method = httpMethod
            if (method != HttpMethod.Get) {
                setBody(TextContent(params, contentType))
            } else {
                url.takeFrom(item.webServer + "?" + item.webParams)
            }
        }
        if (response.status == HttpStatusCode.OK) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response.bodyAsText()
    }
}