package com.idormy.sms.forwarder.db.model

import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.utilities.Mode
import com.idormy.sms.forwarder.utilities.RuleKey
import com.idormy.sms.forwarder.utilities.Status
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

@Parcelize
@Entity(tableName = "rule")
data class Rule(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    @ColumnInfo(name = "name")var name: String = "rule sample",
    @ColumnInfo(name = "type")var category: String = "sms",
    @ColumnInfo(name = "filed")var filed: String = "msg_content",
    @ColumnInfo(name = "tcheck")var mode: String = "contain",
    @ColumnInfo(name = "value")var value: String = "match SMS content",
    @ColumnInfo(name = "sender_id")var senderId: Long = 0L,
    @ColumnInfo(name = "sim_slot")var simSlot: String = "ALL",
    @ColumnInfo(name = "sms_template")var smsTemplate: String = "",
    @ColumnInfo(name = "regex_replace")var regexReplace: String = "",
    @ColumnInfo(name = "time")var time: Long = Date().time,
    @ColumnInfo(name = "status")var status: Int = Status.Off.value,
) : Parcelable {

    @IgnoredOnParcel
    @Ignore
    var switchSmsTemplate = false
    @IgnoredOnParcel
    @Ignore
    var switchRegexReplace = false

    fun match(message: Message): Boolean {
        if (category != message.type.value) {
            return false
        }
        return when(filed) {
            RuleKey.FILED_PHONE_NUM, RuleKey.FILED_PACKAGE_NAME -> {
                if (filed == RuleKey.FILED_PHONE_NUM && simSlot != "ALL" && simSlot != message.simSlot) {
                    return false
                }
                return matchValue(message.source!!)
            }
            RuleKey.FILED_MSG_CONTENT, RuleKey.FILED_INFORM_CONTENT -> matchValue(message.content!!)
            else -> false
        }
    }


    private fun matchValue(source: String): Boolean {
        if (value.isEmpty()) {
            return true
        }
        return when(Mode.from(mode)) {
            Mode.Is -> source == value
            Mode.No -> source != value
            Mode.Contain -> source.contains(value, true)
            Mode.NotContain -> !source.contains(value, true)
            Mode.Start -> source.startsWith(value, true)
            Mode.End -> source.endsWith(value, true)
            Mode.Regex -> {
                val pattern: Pattern?
                try {
                    pattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE)
                } catch (e: PatternSyntaxException) {
                    Log.e("pattern error: ", e.pattern + e.description + e.message)
                    return false
                }
                return pattern.matcher(source).find()
            }
        }
    }
}