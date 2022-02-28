package com.idormy.sms.forwarder.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idormy.sms.forwarder.db.model.KeyValuePair

@Dao
interface KeyValuePairDao {
    @Query("SELECT * FROM `KeyValuePair` WHERE `key` = :key")
    suspend fun get(key: String): KeyValuePair?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(value: KeyValuePair): Long

    @Query("DELETE FROM `KeyValuePair` WHERE `key` = :key")
    suspend fun delete(key: String): Int
}