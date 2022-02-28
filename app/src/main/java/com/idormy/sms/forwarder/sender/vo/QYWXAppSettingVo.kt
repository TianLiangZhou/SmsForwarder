package com.idormy.sms.forwarder.sender.vo

import kotlinx.serialization.Serializable

@Serializable
data class QYWXAppSettingVo(
    var corpID: String = "id",
    var agentID: String = "agent id",
    var secret: String = "secret",
    var toUser: String = "userid1|userid2",
    var atAll: Boolean = false,
    var accessToken: String? = null,
    var expiresIn: Long? = null) {
    val safeAccessToken: String?
        get() = if (accessToken == null || accessToken!!.isEmpty() || expiresIn == null || System.currentTimeMillis() > expiresIn!!) null else accessToken
}