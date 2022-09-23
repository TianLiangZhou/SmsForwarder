package com.idormy.sms.forwarder.sender.adapter

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Phone
import com.idormy.sms.forwarder.sender.ResponseState
import com.idormy.sms.forwarder.sender.SenderInterface
import com.idormy.sms.forwarder.sender.vo.SmsSettingVo

object Sms : SenderInterface<SmsSettingVo> {
    override suspend fun send(item: SmsSettingVo, message: Message, logger: Logger?) {
        if (item.mobiles.isEmpty()) {
            throw RuntimeException("`mobiles` parameter error")
        }
        val mobiles = item.mobiles.split(",")
        val simMutableList = Phone.sim()
        if (simMutableList.size < 1) {
            throw RuntimeException("No calling card available")
        }
        var index = item.simSlot
        if (index > 0) {
            index -= 1
        }
        val result = Phone.sendSms(simMutableList[index].subscriptionId, message.content?:"", mobiles)
        if (result) {
            logger?.forwardStatus = ResponseState.Success.value
            logger?.forwardResponse = "success"
        } else {
            logger?.forwardStatus = ResponseState.Failure.value
            logger?.forwardResponse = "failure"
        }
    }
}