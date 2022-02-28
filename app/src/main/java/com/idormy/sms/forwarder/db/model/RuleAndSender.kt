package com.idormy.sms.forwarder.db.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class RuleAndSender(
    @Embedded
    val rule: Rule?,
    @Relation(
        parentColumn = "sender_id",
        entityColumn = "_id"
    )
    val sender: Sender?
): Parcelable
