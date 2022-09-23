package com.idormy.sms.forwarder.sender.adapter

import android.util.Base64
import android.util.Log
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.FeiShuSettingVo
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object FeiShu : SenderInterface<FeiShuSettingVo> {

    override suspend fun send(item: FeiShuSettingVo, message: Message, logger: Logger?) {
        if (item.webhook.isEmpty()) {
            throw RuntimeException("`webhook` parameter error")
        }
        var url = item.webhook
        if (!url.startsWith("https://", true)) {
            url = "https://open.feishu.cn/open-apis/bot/v2/hook/$url"
        }
        val postBody = "{\"timestamp\": \"%s\", \"sign\": \"%s\", \"msg_type\": \"text\", \"content\": { \"text\": \"%s\" }}"
        val timestamp = System.currentTimeMillis() / 1000
        var sign = ""
        if (item.secret != null && item.secret!!.isNotEmpty()) {
            sign = getSign(timestamp, item.secret!!)
            Log.d("sign === ", sign)
        }
        val response: String = Http.client.post(url) {
            setBody(postBody.format(timestamp, sign, message.content?:""))
        }.body()
        if (response.contains("\"StatusCode\":0")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }

    @Throws(Exception::class)
    fun getSign(timestamp: Long, secret: String): String {
        val needSignString= "$timestamp\n$secret"
        Log.d("string == ", needSignString)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(needSignString.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val signData = mac.doFinal()
        return Base64.encodeToString(signData, Base64.NO_WRAP);
    }
}