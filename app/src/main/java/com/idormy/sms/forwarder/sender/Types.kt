package com.idormy.sms.forwarder.sender

import com.idormy.sms.forwarder.R

enum class Types(val value: Int) {
    DingDing(0),
    Email(1),
    Bark(2),
    WebNotify(3),
    QYWXGroup(4),
    QYWX(5),
    ServerChan(6),
    Telegram(7),
    SMS(8),
    FeiShu(9),
    PushPlus(10);
    companion object {
        fun from(value: Int) = values().first { it.value == value }
    }
}

enum class ResponseState(val value: Int) {
    Success(2),
    Failure(0);
}

object SenderHelper {
    fun getImageId(value: Int): Int {
        return when(Types.from(value)) {
            Types.DingDing -> R.mipmap.dingding
            Types.Email -> R.mipmap.email
            Types.Bark -> R.mipmap.bark
            Types.WebNotify -> R.mipmap.webhook
            Types.QYWXGroup -> R.mipmap.qywx
            Types.QYWX -> R.mipmap.qywxapp
            Types.ServerChan -> R.mipmap.serverchan
            Types.Telegram -> R.mipmap.telegram
            Types.SMS -> R.mipmap.sms
            Types.FeiShu -> R.mipmap.feishu
            Types.PushPlus -> R.mipmap.pushplus
        }
    }
}