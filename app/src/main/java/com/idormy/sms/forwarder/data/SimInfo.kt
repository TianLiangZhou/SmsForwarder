package com.idormy.sms.forwarder.data

/**
 * SIM 卡信息
 *
 * 运营商信息：中国移动 中国联通 中国电信
 * 卡槽ID，SimSerialNumber
 * 卡槽id， -1 - 没插入、 0 - 卡槽1 、1 - 卡槽2
 * 号码
 * 城市
 * 设备唯一识别码
 * SIM的编号
 * SIM的 Subscription Id (SIM插入顺序)
 */
data class SimInfo(
    val carrierName: CharSequence? = null,
    val iccId: CharSequence? = null,
    val simSlotIndex: Int = 0,
    val number: CharSequence? = null,
    val countryIso: CharSequence? = null,
    val imei: CharSequence? = null,
    val imsi: CharSequence? = null,
    val subscriptionId: Int = 0
)
