package com.idormy.sms.forwarder.provider

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.idormy.sms.forwarder.data.CallInfo
import com.idormy.sms.forwarder.data.Contact
import com.idormy.sms.forwarder.data.SimInfo
import com.idormy.sms.forwarder.utilities.NetworkMode

object Phone {

    fun sim(): MutableList<SimInfo> {
        val simMutableList = mutableListOf<SimInfo>()
        if (ActivityCompat.checkSelfPermission(
                Core.app,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            for (info in Core.subscriptionManager.activeSubscriptionInfoList) {
                simMutableList.add(
                    SimInfo(
                        info.carrierName, info.iccId, info.simSlotIndex + 1, info.number,
                        info.countryIso, "", "", info.subscriptionId,
                    )
                )
            }
        }
        return simMutableList
    }


    fun sendSms(subId: Int, message: String, mobiles: List<String>): Boolean {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Core.smsManager.createForSubscriptionId(subId)
        } else {
            SmsManager.getSmsManagerForSubscriptionId(subId)
        }
        var r = false
        try {
            for (mobile in mobiles) {
                val divideMessage = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(
                    mobile,
                    null,
                    divideMessage,
                    null,
                    null
                )
            }
            r = true
        } catch (_: RuntimeException) { }
        return r
    }

    fun lastCallRecords(phoneNumber: String): CallInfo? {
        val cursor = Core.app.contentResolver.query(
            CallLog.Calls.CONTENT_URI, null,
            CallLog.Calls.NUMBER + " like ?", arrayOf("$phoneNumber%"), CallLog.Calls.DEFAULT_SORT_ORDER
        ) ?: return null
        var callInfo: CallInfo? = null
        while (cursor.moveToNext()) {
            callInfo = CallInfo(
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)),  //姓名
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)),  //号码
                cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)),  //获取通话日期
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)),  //获取通话时长，值为多少秒
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)),  //获取通话类型：1.呼入2.呼出3.未接
                cursor.getInt(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
            )
            cursor.close()
            break
        }
        return callInfo
    }

    fun getContactByNumber(phoneNumber: String): MutableList<Contact> {
        val contacts = mutableListOf<Contact>()
        try {
            val cr = Core.app.contentResolver
            val selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " in(?,?,?) "
            val phone1 = phoneNumber.subSequence(0, 3).toString() + " " + phoneNumber.substring(3, 7) +
                    " " + phoneNumber.substring(7)
            val phone2 = phoneNumber.subSequence(0, 3).toString() + "-" + phoneNumber.substring(3, 7) +
                    "-" + phoneNumber.substring(7)
            val selectionArgs = arrayOf(phoneNumber, phone1, phone2)
            val cursor: Cursor? = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ), selection, selectionArgs, "sort_key")
            if (cursor != null) {
                val displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val mobileNoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                var mobileNo: String?
                var displayName: String?
                while (cursor.moveToNext()) {
                    if (displayNameIndex == -1 || mobileNoIndex == -1) {
                        continue
                    }
                    mobileNo = cursor.getString(mobileNoIndex)
                    displayName = cursor.getString(displayNameIndex)
                    contacts.add(Contact(displayName, mobileNo))
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contacts
    }

    fun getNetworkMode(): NetworkMode {
        val connectivityManager = Core.app.getSystemService(ConnectivityManager::class.java)
        var mode = NetworkMode.None
        connectivityManager?.run {
            mode = getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkMode.Wifi
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkMode.Cellular
                    //for other device how are able to connect with Ethernet
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkMode.Ethernet
                    //for check internet over Bluetooth
                    hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkMode.BlueTooth
                    else -> NetworkMode.None
                }
            } ?: NetworkMode.None
        }
        return mode
    }
}