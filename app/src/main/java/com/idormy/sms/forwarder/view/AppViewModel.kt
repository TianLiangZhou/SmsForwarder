package com.idormy.sms.forwarder.view

import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idormy.sms.forwarder.data.AppInfo
import com.idormy.sms.forwarder.provider.Core

class AppViewModel : BaseViewModel() {
    private val _packages: MutableLiveData<List<AppInfo>> = MutableLiveData(ArrayList())

    val packages: LiveData<List<AppInfo>> = _packages

    fun loadAllPackage() = launchAsync({
        getAllPackage()
    }, {list ->
        _packages.postValue(list)
    })

    private fun getAllPackage(): List<AppInfo> {
        val list = ArrayList<AppInfo>()
        val pm  = Core.app.packageManager
        try {
            val packages = if (Build.VERSION.SDK_INT >= 33) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong()))
            } else {
                pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            }
            for (pkg in packages) {
                if (pm.getLaunchIntentForPackage(pkg.packageName) == null) {
                    continue
                }
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkg.longVersionCode
                } else {
                    pkg.versionCode.toLong()
                }
                val appInfo = AppInfo(
                    pkg.applicationInfo.loadLabel(pm).toString(),
                    pkg.packageName,
                    pkg.applicationInfo.loadIcon(pm),
                    pkg.versionName,
                    versionCode
                )
                list.add(appInfo)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return list
    }
}

class AppViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}