package com.idormy.sms.forwarder.data

import kotlinx.serialization.Serializable

@Serializable
data class SmsSettingVo(
    var simSlot: Int = 0,
    var mobiles: String = "18888888888",
    var onlyNoNetwork: Boolean = false) {
    val smsSimSlotCheckId: Int = 0
//        get() = when (simSlot) {
//            1 -> R.id.btnSmsSimSlot1
//            2 -> R.id.btnSmsSimSlot2
//            else -> R.id.btnSmsSimSlotOrg
//        }
}