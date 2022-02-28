package com.idormy.sms.forwarder.db.repositories

import androidx.annotation.WorkerThread
import com.idormy.sms.forwarder.db.dao.SenderDao
import com.idormy.sms.forwarder.db.model.Sender
import kotlinx.coroutines.flow.Flow

class SenderRepository(private val senderDao: SenderDao) {

    var listener: Listener? = null

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(sender: Sender) = senderDao.insert(sender)

    @WorkerThread
    suspend fun delete(id: Long) {
        listener?.onDelete(id)
        senderDao.delete(id)
    }

    @WorkerThread
    suspend fun getSenderRule() = senderDao.getSenderRule()

    suspend fun get(id: Long) = senderDao.get(id)

    suspend fun update(sender: Sender) = senderDao.update(sender)

    val count: Flow<Long> = senderDao.getOnCount()

    val all: Flow<List<Sender>> = senderDao.getAll()

    val enables: Flow<List<Sender>> = senderDao.getOn()
}