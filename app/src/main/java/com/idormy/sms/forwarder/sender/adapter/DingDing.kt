package com.idormy.sms.forwarder.sender.adapter

import android.util.Base64
import android.util.Log
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.DingDingSettingVo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


@Serializable
data class DDMessage(var at: At? = null, var text: Content? = null, val msgtype: String = "text")

@Serializable
data class Content(var content: String = "")

@Serializable
data class At(var atMobiles: List<String>? = null, var atUserIds: List<String>? = null, var isAtAll: Boolean = false)


object DingDing : SenderInterface<DingDingSettingVo> {

    override suspend fun send(item: DingDingSettingVo, message: Message, logger: Logger?) {
        if (item.token.isEmpty()) {
            throw RuntimeException("`token` parameter error ")
        }
        var url = item.token
        if (!url.startsWith("https://", true)) {
            url = "https://oapi.dingtalk.com/robot/send?access_token=$url"
        }
        if (!item.secret.isNullOrEmpty()) {
            val timestamp = System.currentTimeMillis()
            url += "&timestamp=$timestamp&sign=" + getSign(timestamp, item.secret)
        }
        val requestData = DDMessage(At(emptyList(), emptyList(), false), Content())
         requestData.at?.isAtAll = item.atAll
        if (!item.atMobiles.isNullOrEmpty()) {
             requestData.at?.atMobiles = item.atMobiles.split(",")
        }
        requestData.text?.content = message.content?:""
        val httpResponse = Http.client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestData)
        }
        val response = httpResponse.body<String>()
        Log.d("Http dd = ", response)
        if (response.contains("\"errcode\":0")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }

    @Throws(Exception::class)
    fun getSign(timestamp: Long, secret: String): String {
        val stringToSign = "$timestamp\n$secret"
        Log.d("string == ", stringToSign)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val signData = mac.doFinal(stringToSign.toByteArray(Charsets.UTF_8))
        val base64 = Base64.encodeToString(signData, Base64.NO_WRAP);
        return URLEncoder.encode(base64,"UTF-8")
    }



}