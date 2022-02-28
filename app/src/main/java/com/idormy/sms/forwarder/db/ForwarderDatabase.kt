package com.idormy.sms.forwarder.db

import android.content.Context
import android.database.SQLException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.idormy.sms.forwarder.db.dao.KeyValuePairDao
import com.idormy.sms.forwarder.db.dao.LoggerDao
import com.idormy.sms.forwarder.db.dao.RuleDao
import com.idormy.sms.forwarder.db.dao.SenderDao
import com.idormy.sms.forwarder.db.model.KeyValuePair
import com.idormy.sms.forwarder.db.model.Logger
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.utilities.Key
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executors


@Database(
    entities = [Logger::class, Rule::class, Sender::class, KeyValuePair::class],
    version = 9,
    exportSchema = false
)
abstract class ForwarderDatabase : RoomDatabase() {

    abstract fun loggerDao(): LoggerDao
    abstract fun ruleDao(): RuleDao
    abstract fun senderDao(): SenderDao

    abstract fun keyValuePairDao(): KeyValuePairDao

    companion object {
        @Volatile
        private var INSTANCE: ForwarderDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): ForwarderDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForwarderDatabase::class.java,
                    Key.DB
                ).addMigrations(
                    Migration89,
                ).allowMainThreadQueries()
//                .enableMultiInstanceInvalidation()
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this codelab.
                .fallbackToDestructiveMigration()
                .setQueryCallback({ sqlQuery, bindArgs ->
                    println("SQL Query: $sqlQuery SQL Args: $bindArgs")
                }, Executors.newSingleThreadExecutor())
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }


        object Migration89 : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {

                Log.d("database start", startVersion.toString())
                Log.d("database end", endVersion.toString())
                Log.d("database migration", "start")
                Log.d("path", database.path)
                database.execSQL("CREATE TABLE `log_tmp` (\n" +
                        "    `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                        "    `type` TEXT NOT NULL,\n" +
                        "    `l_from` TEXT NOT NULL,\n" +
                        "    `content` TEXT NOT NULL,\n" +
                        "    `sim_info` TEXT NOT NULL,\n" +
                        "    `rule_id` INTEGER NOT NULL,\n" +
                        "    `time` INTEGER NOT NULL,\n" +
                        "    `forward_status` INTEGER NOT NULL,\n" +
                        "    `forward_response` TEXT NOT NULL\n" +
                        ")")
                database.execSQL("INSERT INTO `log_tmp` (`_id`, `type`, `l_from`, `content`, `sim_info`, `rule_id`, `time`, `forward_status`, `forward_response`) SELECT `_id`, `type`, `l_from`, `content`, `sim_info`, `rule_id`, `time`, `forward_status`, `forward_response` FROM `log`")
                database.execSQL("DROP TABLE IF EXISTS `log`")
                database.execSQL("ALTER TABLE `log_tmp` RENAME TO `log`")


                database.execSQL("CREATE TABLE `rule_tmp` (\n" +
                        "    `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                        "    `name` TEXT NOT NULL,\n" +
                        "    `type` TEXT NOT NULL,\n" +
                        "    `filed` TEXT NOT NULL,\n" +
                        "    `tcheck` TEXT NOT NULL,\n" +
                        "    `value` TEXT NOT NULL,\n" +
                        "    `sender_id` INTEGER NOT NULL,\n" +
                        "    `sim_slot` TEXT NOT NULL,\n" +
                        "    `sms_template` TEXT NOT NULL,\n" +
                        "    `regex_replace` TEXT NOT NULL,\n" +
                        "    `time` INTEGER NOT NULL\n" +
                        ")")
                try {
                    database.execSQL("ALTER TABLE `rule` ADD COLUMN `name` TEXT ")
                } catch (e: SQLException) {}
                database.execSQL("INSERT INTO `rule_tmp` (`_id`, `name`, `type`, `filed`, `tcheck`, `value`, `sender_id`, `sim_slot`, `sms_template`, `regex_replace`, `time`) SELECT `_id`, `name`, `type`, `filed`, `tcheck`, `value`, `sender_id`, `sim_slot`, `sms_template`, `regex_replace`, `time` FROM `rule`")
                database.execSQL("DROP TABLE IF EXISTS `rule`")
                database.execSQL("ALTER TABLE `rule_tmp` RENAME TO `rule`")

                database.execSQL("CREATE TABLE `sender_tmp` (\n" +
                        "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                        " `name` TEXT NOT NULL,\n" +
                        "`status` INTEGER NOT NULL,\n" +
                        " `json_setting` TEXT NOT NULL,\n" +
                        "`type` INTEGER NOT NULL,\n" +
                        " `time` INTEGER NOT NULL)")

                database.execSQL("INSERT INTO `sender_tmp` (`_id`, `name`, `status`, `json_setting`, `type`, `time`) SELECT `_id`, `name`, `status`, `json_setting`, `type`, `time` FROM `sender`")
                database.execSQL("DROP TABLE IF EXISTS `sender`")
                database.execSQL("ALTER TABLE `sender_tmp` RENAME TO `sender`")



                database.execSQL("CREATE TABLE IF NOT EXISTS `KeyValuePair` (`key` TEXT NOT NULL, `valueType` INTEGER NOT NULL, `value` BLOB NOT NULL, PRIMARY KEY(`key`))")

                Log.d("database migration", "success")


            }
        }
    }
}