package com.idormy.sms.forwarder.db.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoggerAndRuleAndSender(
    @Embedded
    val logger: Logger,
    @Relation(
        entity = Rule::class,
        parentColumn = "rule_id",
        entityColumn = "_id"
    )
    val relation: RuleAndSender,
): Parcelable
