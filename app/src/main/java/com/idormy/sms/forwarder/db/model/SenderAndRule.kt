package com.idormy.sms.forwarder.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class SenderAndRule(
    @Embedded
    val sender: Sender,
    @Relation(
        parentColumn = "_id",
        entityColumn = "sender_id"
    )
    val rules: List<Rule>?,
)
