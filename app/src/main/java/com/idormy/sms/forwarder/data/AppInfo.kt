package com.idormy.sms.forwarder.data

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class AppInfo(
    val appName: String?,
    val pkgName: String?,
    val appIcon: @RawValue Drawable?,
    val verName: String?,
    val verCode: Long,
    var appIntent: Intent? = null
): Parcelable