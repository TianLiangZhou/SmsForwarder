package com.idormy.sms.forwarder.db.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "log")
data class Logger(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    @ColumnInfo(name = "type")val type: String = "sms",
    @ColumnInfo(name = "l_from")val from: String = "",
    @ColumnInfo(name = "content")val content: String = "",
    @ColumnInfo(name = "sim_info")val simInfo: String = "ALL",
    @ColumnInfo(name = "rule_id")val ruleId: Long = 0L,
    @ColumnInfo(name = "time") val time: Long = Date().time,
    @ColumnInfo(name = "forward_status") var forwardStatus: Int = 0,
    @ColumnInfo(name = "forward_response") var forwardResponse: String = ""
) : Parcelable