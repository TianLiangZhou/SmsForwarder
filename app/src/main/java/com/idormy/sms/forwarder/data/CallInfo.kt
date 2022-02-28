package com.idormy.sms.forwarder.model

//姓名
//号码
//获取通话日期
//获取通话时长，值为多少秒
//获取通话类型：1.呼入 2.呼出 3.未接
data class CallInfo(var name: String?, var number: String?, var dateLong: Long?, var duration: Int, var type: Int, var subscriptionId: Int) {
    override fun toString(): String {
        return "CallInfo{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", dateLong=" + dateLong +
                ", duration=" + duration +
                ", type=" + type +
                ", subscriptionId=" + subscriptionId +
                '}'
    }
}