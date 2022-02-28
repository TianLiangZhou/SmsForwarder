package com.idormy.sms.forwarder.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class RuleAndSender(
    @Embedded
    val sender: Sender,
    @Relation(
        parentColumn = "id",
        entityColumn = "userOwnerId"
    )
    val rules: List<Rule>,
)
