/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package com.idormy.sms.forwarder

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.await
import com.google.android.gms.common.internal.Objects
import com.google.android.material.chip.Chip
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.idormy.sms.forwarder.barcode.*
import com.idormy.sms.forwarder.view.ScannerViewModel
import com.idormy.sms.forwarder.view.ScannerViewModel.WorkflowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.tasks.await

@SuppressLint("RestrictedApi")
class ScannerActivity : AppCompatActivity(), ImageAnalysis.Analyzer{

    private lateinit var promptChip: Chip
    private lateinit var currentWorkflowState: WorkflowState
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView
    private lateinit var promptChipAnimator: AnimatorSet

    private var imageProxy: ImageProxy? = null

    private lateinit var cameraReticuleAnimator: CameraReticuleAnimator

    private val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().apply {
        setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    }.build())

    private val scannerViewModel: ScannerViewModel by lazy { ViewModelProvider(this).get(ScannerViewModel::class.java) }

    private val imageAnalysis by lazy {
        ImageAnalysis.Builder().apply {
            setImageQueueDepth(1)
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            setBackgroundExecutor(Dispatchers.Default.asExecutor())
        }.build().also {
            it.setAnalyzer(Dispatchers.Main.immediate.asExecutor(), this)
        }
    }

    private fun closeImage() {
        imageProxy?.close()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 25) getSystemService<ShortcutManager>()!!.reportShortcutUsed("scan")
        setContentView(R.layout.layout_scanner)
        lifecycle.addObserver(scanner)
        previewView = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById(R.id.graphic_overlay)
        promptChip = findViewById(R.id.bottom_prompt_chip)
        promptChipAnimator =
            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
                setTarget(promptChip)
            }
        cameraReticuleAnimator = CameraReticuleAnimator(graphicOverlay)
        requestCamera.launch(Manifest.permission.CAMERA)
        setUpWorkflowModel()
    }

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return
        imageProxy = image
        imageAnalysis.resolutionInfo?.let {
            graphicOverlay.setPreviewSizeInfo(imageAnalysis.resolutionInfo!!.resolution)
        }
        lifecycleScope.launchWhenCreated {
            try {
                process { InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees) }
            } catch (e: Exception) {
                closeImage()
                return@launchWhenCreated
            }
        }
    }

    private fun setUpWorkflowModel() {
        scannerViewModel.workflowState.observe(this) {state ->
            if (state == null || Objects.equal(currentWorkflowState, state)) {
                return@observe
            }
            currentWorkflowState = state
            val wasPromptChipGone = promptChip.visibility == View.GONE
            when(state) {
                WorkflowState.DETECTING -> {
                    promptChip.visibility = View.VISIBLE
                    promptChip.text = "Point your camera at a barcode"
                }
                WorkflowState.CONFIRMING -> {
                    promptChip.visibility = View.VISIBLE
                    promptChip.text = "Move closer to search"
                }
                WorkflowState.SEARCHING -> {
                    promptChip.visibility = View.VISIBLE
                    promptChip.text = "Searching..."
                }
                WorkflowState.DETECTED, WorkflowState.SEARCHED -> {
                    promptChip.visibility = View.GONE
                    closeImage()
                }
                else -> promptChip.visibility = View.GONE
            }
            val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip.visibility == View.VISIBLE
            promptChipAnimator.let {
                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
            }
        }
        scannerViewModel.detectedBarcode.observe(this) { barcode ->
            if (barcode != null && barcode.rawValue != null) {
                setResult(Activity.RESULT_OK, Intent().putExtra("scanned_result", barcode.rawValue))
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentWorkflowState = WorkflowState.NOT_STARTED
        scannerViewModel.setWorkflowState(WorkflowState.DETECTING)
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowState.NOT_STARTED
    }

    override fun onDestroy() {
        closeImage()
        scanner.close()
        imageAnalysis.clearAnalyzer()
        super.onDestroy()
    }

    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) lifecycleScope.launchWhenCreated {
            val cameraProvider = ProcessCameraProvider.getInstance(this@ScannerActivity).await()
            val selector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else CameraSelector.DEFAULT_FRONT_CAMERA
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@ScannerActivity, selector, preview, imageAnalysis)
            } catch (_: IllegalArgumentException) {
            }
        } else permissionMissing()
    }

//    @SuppressLint("UnsafeOptInUsageError")
//    private fun getPreviewSize(camera: Camera): Size? {
//        val characteristics = (getSystemService(Context.CAMERA_SERVICE) as CameraManager).getCameraCharacteristics(Camera2CameraInfo.from(camera.cameraInfo).cameraId)
//        val configs: StreamConfigurationMap? = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//        val previewSizes = configs?.getOutputSizes(SurfaceTexture::class.java)
//        var minAspectRatioDiff = Float.MAX_VALUE
//        val displayAspectRatioInLandscape: Float =
//            if (isPortraitMode(graphicOverlay.context)) {
//                graphicOverlay.height.toFloat() / graphicOverlay.width
//            } else {
//                graphicOverlay.width.toFloat() / graphicOverlay.height
//            }
//        var selectedPair: Size? = null
//        previewSizes?.forEach {previewSize ->
//            if (previewSize.width < 400 || previewSize.width > 1300) {
//                return@forEach
//            }
//            val previewAspectRatio = previewSize.width.toFloat() / previewSize.height.toFloat()
//            val aspectRatioDiff = abs(displayAspectRatioInLandscape - previewAspectRatio)
//            if (abs(aspectRatioDiff - minAspectRatioDiff) <  0.01f) {
//                if (selectedPair == null || selectedPair!!.width < previewSize.width) {
//                    selectedPair = previewSize
//                }
//            } else if (aspectRatioDiff < minAspectRatioDiff) {
//                minAspectRatioDiff = aspectRatioDiff
//                selectedPair = previewSize
//            }
//        }
//        return selectedPair
//    }

    private suspend inline fun process(crossinline image: () -> InputImage) {
        val barcodes = scanner.process(image()).await()
        val barcodeInCenter = barcodes.firstOrNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox)
            box.contains(graphicOverlay.width / 2f, graphicOverlay.height / 2f)
        }
        graphicOverlay.clear()
        if (barcodeInCenter == null) {
            cameraReticuleAnimator.start()
            graphicOverlay.add(BarcodeReticleGraphic(graphicOverlay, cameraReticuleAnimator))
            closeImage()
        } else {
            cameraReticuleAnimator.cancel()
            val sizeProgress = PreferenceUtils.getProgressToMeetBarcodeSizeRequirement(graphicOverlay, barcodeInCenter)
            if (sizeProgress < 1) {
                // Barcode in the camera view is too small, so prompt user to move camera closer.
                graphicOverlay.add(BarcodeConfirmingGraphic(graphicOverlay, barcodeInCenter))
                scannerViewModel.setWorkflowState(WorkflowState.CONFIRMING)
                closeImage()
            } else {
                // Barcode size in the camera view is sufficient.
                if (PreferenceUtils.shouldDelayLoadingBarcodeResult(graphicOverlay.context)) {
                    val loadingAnimator = createLoadingAnimator(graphicOverlay, barcodeInCenter)
                    loadingAnimator.start()
                    graphicOverlay.add(BarcodeLoadingGraphic(graphicOverlay, loadingAnimator))
                    scannerViewModel.setWorkflowState(WorkflowState.SEARCHING)
                } else {
                    scannerViewModel.setWorkflowState(WorkflowState.DETECTED)
                    scannerViewModel.detectedBarcode.setValue(barcodeInCenter)
                }
            }
        }
        graphicOverlay.invalidate()
    }

    private fun permissionMissing() {
        Toast.makeText(this, R.string.add_profile_scanner_permission_required, Toast.LENGTH_SHORT).show()
    }

    /**
     * See also: https://stackoverflow.com/a/31350642/2245107
     */
    override fun shouldUpRecreateTask(targetIntent: Intent?) = super.shouldUpRecreateTask(targetIntent) || isTaskRoot


    override fun onSupportNavigateUp(): Boolean {
        closeImage()
        return super.onSupportNavigateUp()
    }

    private fun createLoadingAnimator(graphicOverlay: GraphicOverlay, barcode: Barcode): ValueAnimator {
        val endProgress = 1.1f
        return ValueAnimator.ofFloat(0f, endProgress).apply {
            duration = 2000
            addUpdateListener {
                if ((animatedValue as Float).compareTo(endProgress) >= 0) {
                    graphicOverlay.clear()
                    scannerViewModel.setWorkflowState(WorkflowState.SEARCHED)
                    scannerViewModel.detectedBarcode.setValue(barcode)
                } else {
                    graphicOverlay.invalidate()
                }
            }
        }
    }

    private fun isPortraitMode(context: Context): Boolean = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}
