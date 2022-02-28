package com.idormy.sms.forwarder.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import com.idormy.sms.forwarder.provider.Core

class RedirectAppSettingPermission: ActivityResultContract<Void?, Boolean>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", Core.app.packageName, null)
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return false
    }
}
