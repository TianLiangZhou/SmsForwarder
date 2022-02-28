package com.idormy.sms.forwarder.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.idormy.sms.forwarder.provider.Core

class BatteryReceiver : BroadcastReceiver() {
    @SuppressLint("DefaultLocale")
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "BatteryReceiver--------------")
        val action = intent.action
        Log.i(TAG, " 0 action:$action")
        Log.i(TAG, "ACTION_BATTERY_CHANGED")
        val status = intent.getIntExtra("status", 0)
        val health = intent.getIntExtra("health", 0)
        //boolean present = intent.getBooleanExtra("present", false);
        val levelCur = intent.getIntExtra("level", 0)
        val scale = intent.getIntExtra("scale", 0)
        //int icon_small = intent.getIntExtra("icon-small", 0);
        val plugged = intent.getIntExtra("plugged", 0)
        val voltage = intent.getIntExtra("voltage", 0)
        val temperature = intent.getIntExtra("temperature", 0)
        //String technology = intent.getStringExtra("technology");
        var msg = ""
        msg += "\n剩余电量：$levelCur%"
        if (scale > 0) msg += "\n充满电量：$scale%"
        if (voltage > 0) msg += """
 
 当前电压：${String.format("%.2f", voltage / 1000f)}V
 """.trimIndent()
        if (temperature > 0) msg += """
 
 当前温度：${String.format("%.2f", temperature / 10f)}℃
 """.trimIndent()
        msg += """
            
            电池状态：${getStatus(status)}
            """.trimIndent()
        if (health > 0) msg += """
 
 健康度：${getHealth(health)}
 """.trimIndent()
        when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> msg += "\n充电器：AC"
            BatteryManager.BATTERY_PLUGGED_USB -> msg += "\n充电器：USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> msg += "\n充电器：无线"
        }
        Log.i(TAG, msg)


        //电量发生变化
        // todo

        //充电状态改变
        if (Core.dataStore.switchMonitorBattery) {
            // todo
        }
    }

    //电池状态
    private fun getStatus(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
            BatteryManager.BATTERY_STATUS_FULL -> "充满电"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "未知"
            else -> "未知"
        }
    }

    //健康度
    private fun getHealth(health: Int): String {
        return when (health) {
            2 -> "良好"
            3 -> "过热"
            4 -> "没电"
            5 -> "过电压"
            6 -> "未知错误"
            7 -> "温度过低"
            1 -> "未知"
            else -> "未知"
        }
    }

    companion object {
        private const val TAG = "BatteryReceiver"
    }
}