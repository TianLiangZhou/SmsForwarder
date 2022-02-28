package com.idormy.sms.forwarder

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Configuration
import com.idormy.sms.forwarder.db.ForwarderDatabase
import com.idormy.sms.forwarder.db.repositories.KeyValueRepository
import com.idormy.sms.forwarder.db.repositories.LoggerRepository
import com.idormy.sms.forwarder.db.repositories.RuleRepository
import com.idormy.sms.forwarder.db.repositories.SenderRepository
import com.idormy.sms.forwarder.preference.RoomPreferenceDataStore
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.service.FrontService
import com.idormy.sms.forwarder.utilities.CommonUtil.getChannelName
import com.idormy.sms.forwarder.utilities.PhoneUtils.SimInfo
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApplication : Application(), Configuration.Provider by Core {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { ForwarderDatabase.getDatabase(this, applicationScope) }
    val loggerRepository by lazy { LoggerRepository(database.loggerDao()) }
    val senderRepository by lazy { SenderRepository(database.senderDao()) }
    val ruleRepository by lazy { RuleRepository(database.ruleDao()) }
    val dataStoreRepository by lazy { KeyValueRepository(RoomPreferenceDataStore(database.keyValuePairDao())) }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        Core.init(this)
        try {
            //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口。
            //建议在宿主App的Application.onCreate函数中调用基础组件库初始化函数。
            UMConfigure.init(this, "60254fc7425ec25f10f4293e", getChannelName(this), UMConfigure.DEVICE_TYPE_PHONE, "")
            // 选用LEGACY_AUTO页面采集模式
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_MANUAL)
            //pro close log
            UMConfigure.setLogEnabled(true)

            //前台服务
            val intent = Intent(this, FrontService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
//            SendHistory.init(this)
//            SettingUtil.init(this)
//            EmailKit.initialize(this)
            //电池状态监听
//            val batteryServiceIntent = Intent(this, BatteryService::class.java)
//            startService(batteryServiceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate:", e)
        }

    }

    companion object {
        private const val TAG = "MyApplication"

        //SIM卡信息
        var SimInfoList: List<SimInfo> = ArrayList()

        //是否关闭页面提示
        var showHelpTip = true
    }
}