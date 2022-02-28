package com.idormy.sms.forwarder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.Worker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UpdateSenderWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val senderListString = inputData.getString(Worker.updateSender)
        val mutableList = Json.decodeFromString<MutableList<Sender>>(senderListString!!)
        for (sender in mutableList) {
            Core.sender.update(sender)
        }
        return@withContext Result.success()
    }


}