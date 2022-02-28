package com.idormy.sms.forwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.idormy.sms.forwarder.provider.Core

class RebootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val TAG = "RebootBroadcastReceiver"
        Log.d(TAG, "onReceive intent ${intent.action}")
        if (!Core.dataStore.isAutoStartup) {
            return
        }
        val doStart = when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> !Core.directBootAware
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> Core.directBootAware
            else -> Core.directBootAware || Build.VERSION.SDK_INT >= 24 && Core.user.isUserUnlocked
        }
        if (doStart) Core.startService()
    }
}