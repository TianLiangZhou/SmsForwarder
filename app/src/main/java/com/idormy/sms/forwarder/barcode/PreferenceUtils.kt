/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idormy.sms.forwarder.barcode

import android.content.Context
import android.graphics.RectF
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.google.android.gms.common.images.Size
import com.google.mlkit.vision.barcode.common.Barcode

/** Utility class to retrieve shared preferences. */
object PreferenceUtils {

    fun isAutoSearchEnabled(context: Context): Boolean = getBooleanPref(context, "auto_search", false)

    fun isMultipleObjectsMode(context: Context): Boolean = getBooleanPref(context, "multi_object", false)

    fun isClassificationEnabled(context: Context): Boolean = getBooleanPref(context, "classification", false)

    fun saveStringPreference(context: Context, @StringRes prefKeyId: Int, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(context.getString(prefKeyId), value)
            .apply()
    }

    fun getConfirmationTimeMs(context: Context): Int =
        when {
            isMultipleObjectsMode(context) -> 300
            isAutoSearchEnabled(context) ->
                getIntPref(context, "ctias", 1500)

            else -> getIntPref(context, "ctimsR" ,500)
        }

    fun getProgressToMeetBarcodeSizeRequirement(overlay: GraphicOverlay, barcode: Barcode): Float {
        val context = overlay.context
        return if (getBooleanPref(context, "barcode_ebsc",false)) {
            val reticleBoxWidth = getBarcodeReticleBox(overlay).width()
            val barcodeWidth = overlay.translateX(barcode.boundingBox?.width()?.toFloat() ?: 0f)
            val requiredWidth =
                reticleBoxWidth * getIntPref(context, "barcode_mbw", 50) / 100
            (barcodeWidth / requiredWidth).coerceAtMost(1f)
        } else {
            1f
        }
    }

    fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val context = overlay.context
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth =
            overlayWidth * getIntPref(context, "barcode_brw", 80) / 100
        val boxHeight =
            overlayHeight * getIntPref(context, "barcode_brh",35) / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
    }

    fun shouldDelayLoadingBarcodeResult(context: Context): Boolean = getBooleanPref(context, "barcode_dlbr" ,true)


    fun getUserSpecifiedPreviewSize(context: Context): CameraSizePair? {
        return try {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            CameraSizePair(
                Size.parseSize(sharedPreferences.getString("rcpvs", null)!!),
                Size.parseSize(sharedPreferences.getString("rcpts", null)!!)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getIntPref(context: Context, prefKeyId: String, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(prefKeyId, defaultValue)
    }

    private fun getBooleanPref(
        context: Context,
        prefKeyId: String,
        defaultValue: Boolean
    ): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(prefKeyId, defaultValue)
}
