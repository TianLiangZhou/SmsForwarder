package com.idormy.sms.forwarder.data

import com.idormy.sms.forwarder.R
import kotlinx.serialization.Serializable

@Serializable
data class WebNotifySettingVo(
    var webServer: String = "https://api.example.com/push",
    var secret: String = "no secret",
    var method: String = "POST",
    var webParams: String = "mm=11&msg=[msg]") {
    val webNotifyMethodCheckId: Int
        get() = if (method == "POST") {
            R.id.radioWebNotifyMethodPost
        } else {
            R.id.radioWebNotifyMethodGet
        }
}