package com.idormy.sms.forwarder.data

//姓名
//号码
//获取通话日期
//获取通话时长，值为多少秒
//获取通话类型：1.呼入 2.呼出 3.未接
data class CallInfo(
    val name: String?,
    val number: String?,
    val dateLong: Long?,
    val duration: Int,
    val type: Int,
    val subscriptionId: Int
)
