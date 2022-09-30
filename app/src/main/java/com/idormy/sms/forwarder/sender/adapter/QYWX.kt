package com.idormy.sms.forwarder.sender.adapter

import android.util.Log
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.QYWXAppSettingVo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class QYWXAppResult(val errcode: Int, val errmsg: String, val expires_in: Int? = 0, val access_token: String? = null)

@Serializable
data class QYWXAppSendBody(var touser: String, var toparty: String, val agentid: String, val text: Content, val msgtype: String = "text")

object QYWX : SenderInterface<QYWXAppSettingVo> {

    override suspend fun send(item: QYWXAppSettingVo, message: Message, logger: Logger?) {
        val corpId = item.corpID
        val agentId = item.agentID
        val secret = item.secret
        if (corpId.isEmpty() || agentId.isEmpty() || secret.isEmpty()) {
            throw RuntimeException("`corpId` or `agentId` or `secret` parameter error")
        }
        var accessToken = ""
        if (item.safeAccessToken == null) {
            val result = withContext(Dispatchers.IO) {
                getAccessToken(item.corpID, item.secret)
            }
            if (result.errcode == 0) {
                accessToken = result.access_token!!
            }
        } else {
            item.accessToken = item.safeAccessToken
        }
        if (accessToken.isEmpty()) {
            throw RuntimeException("Get `access_token` failure")
        }
        val url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=$accessToken"
        val sendBody = QYWXAppSendBody("", "", item.agentID, Content(message.content?:""))
        val response: String = Http.client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(sendBody)
        }.body()
        Log.d("Http WX = ", response)
        if (response.contains("\"errcode\":0")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }

    private suspend fun getAccessToken(corpID: String, secret: String): QYWXAppResult {
        val url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=$corpID&corpsecret=$secret"
        return Http.client.get(url).body()
    }
}