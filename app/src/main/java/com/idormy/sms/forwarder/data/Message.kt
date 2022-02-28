package com.idormy.sms.forwarder.data

import android.os.Parcelable
import com.idormy.sms.forwarder.utilities.MessageType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Message(
    val source: String? = "",
    var content: String? = "",
    val time: Long = 0L,
    val type: MessageType = MessageType.Sms,
    val simSlot: String = "ALL"
) : Parcelable