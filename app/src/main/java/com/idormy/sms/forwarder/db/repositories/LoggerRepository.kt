package com.idormy.sms.forwarder.db.repositories

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.idormy.sms.forwarder.db.dao.LoggerDao
import com.idormy.sms.forwarder.db.model.Logger

class LoggerRepository(private val loggerDao: LoggerDao) {

    fun category(type: String) = Pager(
        config= PagingConfig(
            pageSize = 20,
            enablePlaceholders =  true,
            maxSize = 100,
        ),
        pagingSourceFactory = {
            loggerDao.getCategories(type)
        }
    ).flow



    suspend fun okCount(): Long = loggerDao.okCount()
    suspend fun failedCount(): Long = loggerDao.failedCount()

    @WorkerThread
    suspend fun delete(id: Long) {
        loggerDao.delete(id)
    }


    @WorkerThread
    suspend fun insert(logger: Logger): Long = loggerDao.insert(logger)

    @WorkerThread
    suspend fun batchInsert(loggers: List<Logger>) = loggerDao.batchInsert(loggers)
}