package com.idormy.sms.forwarder.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.idormy.sms.forwarder.db.dao.LoggerDao
import com.idormy.sms.forwarder.db.dao.RuleDao
import com.idormy.sms.forwarder.db.dao.SenderDao
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.Sender
import kotlinx.coroutines.CoroutineScope


@Database(entities = [Logger::class, Rule::class, Sender::class], version = 1, exportSchema = true)
abstract class SenderDatabase : RoomDatabase() {

    abstract fun loggerDao(): LoggerDao
    abstract fun ruleDao(): RuleDao
    abstract fun senderDao(): SenderDao

    companion object {
        @Volatile
        private var INSTANCE: SenderDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): SenderDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SenderDatabase::class.java,
                    "sms_forwarder.db"
                ).build()
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this codelab.
                // .fallbackToDestructiveMigration()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}