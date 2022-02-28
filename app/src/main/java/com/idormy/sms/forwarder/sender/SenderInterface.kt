package com.idormy.sms.forwarder.sender

import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger

interface SenderInterface<T> {
    suspend fun send(item: T, message: Message, logger: Logger? = null)
}