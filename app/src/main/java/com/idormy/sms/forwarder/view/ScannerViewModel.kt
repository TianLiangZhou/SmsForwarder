package com.idormy.sms.forwarder.view

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.common.Barcode

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    val workflowState = MutableLiveData<WorkflowState>()
    val detectedBarcode = MutableLiveData<Barcode>()

    enum class WorkflowState {
        NOT_STARTED,
        DETECTING,
        DETECTED,
        CONFIRMING,
        CONFIRMED,
        SEARCHING,
        SEARCHED
    }

    @MainThread
    fun setWorkflowState(workflowState: WorkflowState) {
//        if (workflowState != WorkflowState.CONFIRMED && workflowState != WorkflowState.SEARCHING && workflowState != WorkflowState.SEARCHED) {
//            confirmedObject = null
//        }
        this.workflowState.value = workflowState
    }
}