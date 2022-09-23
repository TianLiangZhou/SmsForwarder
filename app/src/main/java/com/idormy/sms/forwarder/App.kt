package com.idormy.sms.forwarder

import android.app.Application
import androidx.work.Configuration
import com.idormy.sms.forwarder.db.ForwarderDatabase
import com.idormy.sms.forwarder.db.repositories.KeyValueRepository
import com.idormy.sms.forwarder.db.repositories.LoggerRepository
import com.idormy.sms.forwarder.db.repositories.RuleRepository
import com.idormy.sms.forwarder.db.repositories.SenderRepository
import com.idormy.sms.forwarder.preference.RoomPreferenceDataStore
import com.idormy.sms.forwarder.provider.Core
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application(), Configuration.Provider by Core {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { ForwarderDatabase.getDatabase(this, applicationScope) }
    val loggerRepository by lazy { LoggerRepository(database.loggerDao()) }
    val senderRepository by lazy { SenderRepository(database.senderDao()) }
    val ruleRepository by lazy { RuleRepository(database.ruleDao()) }
    val dataStoreRepository by lazy { KeyValueRepository(RoomPreferenceDataStore(database.keyValuePairDao())) }

    override fun onCreate() {
        super.onCreate()
        Core.init(this)
    }
}