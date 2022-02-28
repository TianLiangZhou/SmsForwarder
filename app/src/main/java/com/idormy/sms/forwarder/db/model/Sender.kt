package com.idormy.sms.forwarder.db.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.idormy.sms.forwarder.sender.SenderHelper
import com.idormy.sms.forwarder.sender.Types
import com.idormy.sms.forwarder.sender.vo.*
import com.idormy.sms.forwarder.utilities.Status
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Parcelize
@Entity(tableName = "sender")
@Serializable
data class Sender (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    @ColumnInfo(name = "name") var name: String = "sender sample",
    @ColumnInfo(name = "status")var status: Int = Status.Off.value,
    @ColumnInfo(name = "json_setting")var jsonSetting: String = "",
    @ColumnInfo(name = "type") var type: Int = Types.Bark.value,
    @ColumnInfo(name = "time")val time: Long = Date().time,
) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    @Transient
    var setting: Any? = null
    inline fun <reified T> decodeSetting(): T? {
        if (jsonSetting.isEmpty()) {
            return null
        }
        return Json.decodeFromString<T>(jsonSetting)
    }

    fun encodeSetting() {
        jsonSetting = when(Types.from(type)) {
            Types.DingDing -> Json.encodeToString(setting as DingDingSettingVo)
            Types.Email -> Json.encodeToString(setting as EmailSettingVo)
            Types.Bark -> Json.encodeToString(setting as BarkSettingVo)
            Types.WebNotify -> Json.encodeToString(setting as WebNotifySettingVo)
            Types.QYWXGroup -> Json.encodeToString(setting as QYWXGroupRobotSettingVo)
            Types.QYWX -> Json.encodeToString(setting as QYWXAppSettingVo)
            Types.ServerChan -> Json.encodeToString(setting as ServerChanSettingVo)
            Types.Telegram -> Json.encodeToString(setting as TelegramSettingVo)
            Types.SMS -> Json.encodeToString(setting as SmsSettingVo)
            Types.FeiShu -> Json.encodeToString(setting as FeiShuSettingVo)
            Types.PushPlus -> Json.encodeToString(setting as PushPlusSettingVo)
        }
    }

    fun getImageId() : Int {
        return SenderHelper.getImageId(type)
    }

}