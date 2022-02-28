package com.idormy.sms.forwarder.data

import kotlinx.serialization.Serializable

@Serializable
data class QYWXAppSettingVo(
    var corpID: String = "id",
    var agentID: String = "agent id",
    var secret: String = "secret",
    var toUser: String = "userid",
    var atAll: Boolean = false,
    var accessToken: String? = null,
    var expiresIn: Long? = null) {
    val safeAccessToken: String?
        get() = if (accessToken?.isEmpty()!! || System.currentTimeMillis() > expiresIn!!) null else accessToken
}