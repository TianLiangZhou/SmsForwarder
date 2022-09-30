package com.idormy.sms.forwarder.sender.adapter

import android.util.Log
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Http
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.BarkSettingVo
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

object Bark: SenderInterface<BarkSettingVo> {

    @Throws(Exception::class)
    override suspend fun send(item: BarkSettingVo, message: Message, logger: Logger?) {
        if (item.server.isEmpty() ||
            (!item.server.startsWith("http://") &&
            !item.server.startsWith("https://"))
        ) {
            throw RuntimeException("`server` parameter error")
        }
        val content = message.content?:""
        val title = message.source?:""

        val reg = Regex("码[:：]?\\s?(\\d{4,8})")
        val matchResult = reg.find(content)
        var isCopy = 0
        var copy = ""
        if (matchResult != null && matchResult.groupValues.size > 1) {
            isCopy = 1
            copy = matchResult.groupValues[1]
        }
        val url = String.format(
            "%s/%s/%s?isArchive=1&group=%s&icon=%s&automaticallyCopy=%d&copy=%s",
            item.server.removeSuffix("/"), withContext(Dispatchers.IO) {
                URLEncoder.encode(title, "UTF-8")
            }, withContext(Dispatchers.IO) {
                URLEncoder.encode(content, "UTF-8")
            },
            "group", item.icon ?: "", isCopy, copy
        )
        val response = Http.client.get(url).body<String>();
        Log.d("Http bk = ", response)
        if (response.contains("\"code\":0")) {
            logger?.forwardStatus = ResponseState.Success.value
        }
        logger?.forwardResponse = response
    }

}