package com.idormy.sms.forwarder.db.dao

import androidx.room.*
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.RuleAndSender
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rule WHERE _id = :id LIMIT 1")
    suspend fun get(id: Long): Rule?

    @Query("SELECT * FROM rule ORDER BY _id DESC")
    fun getAll(): Flow<List<Rule>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rule: Rule): Long

    @Query("DELETE FROM rule WHERE _id = :id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("SELECT * FROM rule")
    suspend fun getRuleAndSender(): List<RuleAndSender>

    @Update
    suspend fun update(rule: Rule): Int

    @Query("SELECT * FROM rule WHERE _id IN (:ids)")
    fun getIdSet(ids: List<Long>): Flow<List<Rule>>
}