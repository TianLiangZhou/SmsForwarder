package com.idormy.sms.forwarder.sender.adapter

import android.util.Log
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.EmailSettingVo
import com.smailnet.emailkit.Draft
import com.smailnet.emailkit.EmailKit
import com.smailnet.emailkit.EmailKit.GetSendCallback

object Email : SenderInterface<EmailSettingVo> {
    override suspend fun send(item: EmailSettingVo, message: Message, logger: Logger?) {
        if (item.host.isEmpty() || item.port.isEmpty() || item.fromEmail.isEmpty() || item.pwd.isEmpty() || item.toEmail.isEmpty()) {
            throw RuntimeException("parameter error")
        }
        val server = EmailKit.Config()
            .setSMTP(item.host, item.port.toInt(), item.ssl)
            .setAccount(item.fromEmail)
            .setPassword(item.pwd)
        val draft = Draft()
            .setNickname(item.nickname)
            .setTo(item.toEmail)
            .setSubject(item.title)
            .setText(message.source)
        EmailKit.useSMTPService(server)
            .send(draft, object : GetSendCallback {
                override fun onSuccess() {
                    Log.d("Http email = ", "success")
                    logger?.forwardStatus = ResponseState.Success.value
                    logger?.forwardResponse = "success"
                }
                override fun onFailure(errMsg: String) {
                    Log.d("Http email = ", "faild")
                    logger?.forwardStatus = ResponseState.Failure.value
                    logger?.forwardResponse = errMsg
                }
            })
        EmailKit.destroy()
    }
}