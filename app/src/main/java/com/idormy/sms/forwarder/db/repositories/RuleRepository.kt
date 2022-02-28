package com.idormy.sms.forwarder.db.repositories

import androidx.annotation.WorkerThread
import com.idormy.sms.forwarder.db.dao.RuleDao
import com.idormy.sms.forwarder.db.model.Rule
import kotlinx.coroutines.flow.Flow

class RuleRepository(
    private val ruleDao: RuleDao,
) {

    var listener: Listener? = null

    @WorkerThread
    suspend fun insert(rule: Rule) {
        ruleDao.insert(rule)
    }

    @WorkerThread
    suspend fun delete(id: Long) {
        listener?.onDelete(id)
        ruleDao.delete(id)
    }

    @WorkerThread
    suspend fun get(id: Long) = ruleDao.get(id)


    suspend fun getRuleAndSender() = ruleDao.getRuleAndSender()


    @WorkerThread
    suspend fun update(rule: Rule) = ruleDao.update(rule)

    val all: Flow<List<Rule>> = ruleDao.getAll()
}