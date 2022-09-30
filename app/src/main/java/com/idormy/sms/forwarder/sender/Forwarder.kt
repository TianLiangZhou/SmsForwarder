package com.idormy.sms.forwarder.sender

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.sender.adapter.*
import com.idormy.sms.forwarder.sender.vo.*

object Forwarder {
    suspend fun send(sender: Sender, message: Message, logger: Logger? = null) {
        try {
            when (Types.from(sender.type)) {
                Types.DingDing -> {
                    val item = sender.setting ?: sender.decodeSetting<DingDingSettingVo>()
                    DingDing.send(item as DingDingSettingVo, message, logger)
                }
                Types.Email -> {
                    val item = sender.setting ?: sender.decodeSetting<EmailSettingVo>()
                    Email.send(item as EmailSettingVo, message, logger)
                }
                Types.Bark -> {
                    val item = sender.setting ?: sender.decodeSetting<BarkSettingVo>()
                    Bark.send(item as BarkSettingVo, message, logger)
                }
                Types.WebNotify -> {
                    val item = sender.setting ?: sender.decodeSetting<WebNotifySettingVo>()
                    WebNotify.send(item as WebNotifySettingVo, message, logger)
                }
                Types.QYWXGroup -> {
                    val item = sender.setting ?: sender.decodeSetting<QYWXGroupRobotSettingVo>()
                    QYWXGroup.send(item as QYWXGroupRobotSettingVo, message, logger)
                }
                Types.QYWX -> {
                    val item = sender.setting ?: sender.decodeSetting<QYWXAppSettingVo>()
                    QYWX.send(item as QYWXAppSettingVo, message, logger)
                }
                Types.ServerChan -> {
                    val item = sender.setting ?: sender.decodeSetting<ServerChanSettingVo>()
                    ServerChan.send(item as ServerChanSettingVo, message, logger)
                }
                Types.Telegram -> {
                    val item = sender.setting ?: sender.decodeSetting<TelegramSettingVo>()
                    Telegram.send(item as TelegramSettingVo, message, logger)
                }
                Types.SMS -> {
                    val item = sender.setting ?: sender.decodeSetting<SmsSettingVo>()
                    Sms.send(item as SmsSettingVo, message, logger)
                }
                Types.FeiShu -> {
                    val item = sender.setting ?: sender.decodeSetting<FeiShuSettingVo>()
                    FeiShu.send(item as FeiShuSettingVo, message, logger)
                }
                Types.PushPlus -> {
                    val item = sender.setting ?: sender.decodeSetting<PushPlusSettingVo>()
                    PushPlus.send(item as PushPlusSettingVo, message, logger)
                }
                else -> {}
            }
        } catch (ex: Throwable) {
            logger?.forwardResponse = ex.message?:"send exception"
        }
    }
}