package com.idormy.sms.forwarder.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.ui.system.CloneFragment


class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.run {
            val fragment: CloneFragment
            try {
                fragment = activity.supportFragmentManager.fragments.filterIsInstance<CloneFragment>().first()
            } catch (ex: Throwable) {
                return
            }
            action?.let { Log.d("current action: ", it) }
            when(action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    fragment.wifiState(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                    Log.d("receiver", "state changed")
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    // The peer list has changed! We should probably do something about
                    // that.
                    Log.d("receiver", "peers action")
                    manager.requestPeers(channel, fragment)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d("receiver", "connection action")
                    val networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo?
                    val groupInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP) as WifiP2pGroup?
                    println(groupInfo)
                    // Connection state changed! We should probably do something about
                    // that.
                    if (networkInfo != null && networkInfo.isConnected) {
                        Log.d("receiver", "connection connect")
                        manager.requestConnectionInfo(channel, fragment)
                    } else {
                        fragment.disconnect()
                        Log.d("receiver", "connection disconnect")
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.d("receiver", "this device action")
                    fragment.updateThisDevice(
                        intent.getParcelableExtra(
                            WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                        ) as WifiP2pDevice?
                    )
                }
                else -> {}
            }

        }
    }
}