package com.idormy.sms.forwarder.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.db.model.LoggerAndRuleAndSender
import kotlinx.coroutines.flow.Flow

@Dao
interface LoggerDao {
    @Query("SELECT * FROM log WHERE _id = :id LIMIT 1")
    operator fun get(id: Long): Flow<Logger>

    @Transaction
    @Query("SELECT * FROM log WHERE type = :type ORDER BY _id DESC")
    fun getCategories(type: String): PagingSource<Int, LoggerAndRuleAndSender>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(logger: Logger): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun batchInsert(loggers: List<Logger>)

    @Query("DELETE FROM log WHERE _id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM log ORDER BY _id DESC LIMIT :limit OFFSET :offset")
    fun page(offset: Int, limit: Int): Flow<List<Logger>>

    @Query("SELECT COUNT(_id) FROM log WHERE forward_status = 2")
    suspend fun okCount(): Long

    @Query("SELECT COUNT(_id) FROM log WHERE forward_status = 0")
    suspend fun failedCount(): Long
}