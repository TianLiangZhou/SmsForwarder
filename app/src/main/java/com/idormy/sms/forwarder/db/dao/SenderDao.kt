package com.idormy.sms.forwarder.db.dao

import androidx.room.*
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.db.model.SenderAndRule
import kotlinx.coroutines.flow.Flow

@Dao
interface SenderDao {

    @Query("SELECT * FROM sender WHERE _id = :id LIMIT 1")
    suspend fun get(id: Long): Sender?

    @Transaction
    @Query("SELECT * FROM sender WHERE status = 1")
    suspend fun getSenderRule(): List<SenderAndRule>

    @Query("SELECT * FROM sender ORDER BY _id DESC")
    fun getAll(): Flow<List<Sender>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sender: Sender): Long

    @Query("DELETE FROM sender WHERE _id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM sender WHERE _id IN (:ids)")
    fun getIdSet(ids: List<Long>): Flow<List<Sender>>

    @Query("SELECT COUNT(_id) FROM sender WHERE status = 1")
    fun getOnCount(): Flow<Long>

    @Update
    suspend fun update(sender: Sender): Int

    @Query("SELECT * FROM sender WHERE status = 1 ORDER BY _id DESC")
    fun getOn(): Flow<List<Sender>>

}