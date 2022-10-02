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
import com.idormy.sms.forwarder.utilities.Status
import com.idormy.sms.forwarder.utilities.Worker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class SendWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val messageListString = inputData.getString(Worker.sendMessage)
            val mutableList = Json.decodeFromString<List<Message>>(messageListString!!)
            val rules = Core.rule.getRuleAndSender()
            Log.d("rule size ", rules.size.toString())
            if (rules.isEmpty()) {
                return@withContext Result.failure(workDataOf("send" to "failed"))
            }
            val loggers = mutableListOf<Logger>()
            val time = System.currentTimeMillis()
            for (message in mutableList) {
                for (ruleSender in rules) {
                    if (ruleSender.rule == null || ruleSender.sender == null) {
                        continue
                    }
                    if (ruleSender.rule.status != Status.On.value)  {
                        continue
                    }
                    if (!ruleSender.rule.match(message)) {
                        continue
                    }
                    val logger = Logger(
                        0,
                        message.type.value,
                        message.source?:"",
                        message.content?:"",
                        message.simSlot,
                        ruleSender.rule.id,
                        time
                    )
                    Forwarder.send(ruleSender.sender, message, logger)
                    loggers.add(logger)
                    ruleSender.rule.time = Date().time
                    ruleSender.sender.time = Date().time
                    Core.rule.update(ruleSender.rule)
                    Core.sender.update(ruleSender.sender)
                }
            }
            if (loggers.isNotEmpty()) {
                Core.logger.batchInsert(loggers)
            }
            return@withContext Result.success()
        }
    }
}