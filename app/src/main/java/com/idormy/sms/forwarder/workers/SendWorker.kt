package com.idormy.sms.forwarder.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.sender.Forwarder
import com.idormy.sms.forwarder.utilities.Worker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SendWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val messageListString = inputData.getString(Worker.sendMessage)
            val mutableList = Json.decodeFromString<List<Message>>(messageListString!!)
            val senders = Core.sender.getSenderRule()
            Log.d("sender and rule ", senders.size.toString())
            if (senders.isEmpty()) {
                return@withContext Result.failure(workDataOf("send" to "failed"))
            }
            val loggers = mutableListOf<Logger>()
            val time = System.currentTimeMillis()
            for (message in mutableList) {
                for (block in senders) {
                    if (block.rules == null || block.rules.isEmpty())  {
                        continue
                    }
                    block.rules.forEach { rule ->
                        if (!rule.match(message)) {
                            return@forEach
                        }
                        val logger = Logger(
                            0, message.type.value, message.source?:"", message.content?:"", message.simSlot, rule.id, time
                        )
                        Forwarder.send(block.sender, message, logger)
                        loggers.add(logger)
                    }
                }
            }
            if (loggers.isNotEmpty()) {
                Core.logger.batchInsert(loggers)
            }
            return@withContext Result.success()
        }
    }
}