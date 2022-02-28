package com.idormy.sms.forwarder.ui.system

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.FragmentCloneBinding
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.provider.Phone
import com.idormy.sms.forwarder.receiver.WiFiDirectBroadcastReceiver
import com.idormy.sms.forwarder.ui.ToolbarFragment
import com.idormy.sms.forwarder.utilities.NetworkMode
import com.idormy.sms.forwarder.utilities.RedirectAppSettingPermission


class CloneFragment : ToolbarFragment(), WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private var _binding: FragmentCloneBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var wifiP2pEnabled = 0

    private var isWifiP2pSupported = true

    private var noSupportReason = ""

    private var connectState = ConnectState.None

    private var connectedCount = 0

    private val main: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private val intentFilter = IntentFilter()

    private val permSetting = registerForActivityResult(RedirectAppSettingPermission()) {
        if (it || requestPermission()) {
            main.snackbar("位置权限已获取").show()
        }
    }
    private val permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            wifiP2pEnabled = 0
            main.snackbar("WifiP2P功能需要访问位置权限").setAction("前往设置") {
                permSetting.launch(null)
            }.show()
        } else {
            startListener()
        }
    }

    private lateinit var connectDevice: WifiP2pDevice
    private var thisDevice: WifiP2pDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val msg = checkSupportP2p()
        if (msg.isNotEmpty()) {
            main.snackbar(msg).show()
            isWifiP2pSupported = false
            noSupportReason = msg
        } else {
            main.initWifiP2p()
        }
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCloneBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.clone)
        binding.send.setOnClickListener {
            onActionClick(1)
        }
        binding.receive.setOnClickListener {
            onActionClick(2)
        }
    }


    private fun onActionClick(action: Int) {
        if (!isWifiP2pSupported) {
            main.snackbar(noSupportReason).show()
            return
        }
        if (!isWifiConnected()) {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            return
        }
        wifiP2pEnabled = action
        if (requestPermission()) {
            startListener()
        } else {
            permReqLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startListener() {
        when(wifiP2pEnabled) {
            1 ->  {
                binding.receive.isClickable = false
                binding.receive.isEnabled = false
                main.showProgress("等待设备连接...")
                main.manager!!.createGroup(main.channel!!, object : ActionListener {
                    override fun onSuccess() {
                        Log.d("create group ", " success")
                    }
                    override fun onFailure(point: Int) {
                        Log.d("create group ", " failed")
                        main.snackbar("启动服务失败 :$point").show()
                        main.hideProgress()
                        main.removeGroup()
                    }
                })
            }
            2 -> {
                binding.send.isClickable = false
                binding.send.isEnabled = false
                main.showProgress("发现设备中...")
                main.manager!!.discoverPeers(main.channel!!, object : ActionListener {
                    override fun onSuccess() {
                        Log.d("discover ", " success")
                        // mainActivity.snackbar("启动发现").show()
                    }
                    override fun onFailure(point: Int) {
                        Log.d("discover ", " failed")
                        main.snackbar("启动发现失败 :$point").show()
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(main.manager!!, main.channel!!, main)
        main.registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        main.unregisterReceiver(receiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        main.removeGroup()
    }

    private val deviceAdapter by lazy {
        getPeerAdapter()
    }

    private val dialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("设备列表")
            .setAdapter(deviceAdapter, ::selectedPeer)
            .create()
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        if (peers == null) {
            return
        }
        if (wifiP2pEnabled != 2) {
            return
        }
        if (connectState != ConnectState.None) {
            return
        }
        main.hideProgress()
        Log.d("peers count = ", peers.deviceList.size.toString())
        if (!dialog.isShowing) {
            dialog.show()
        }
        deviceAdapter.clear()
        deviceAdapter.addAll(peers.deviceList)
    }



    private fun selectedPeer(dialog: DialogInterface, index: Int) {
        val item = deviceAdapter.getItem(index) ?: return
        if (item.status != WifiP2pDevice.AVAILABLE && item.status != WifiP2pDevice.CONNECTED) {
            main.snackbar("设备状态不可用").show()
            return
        }
        connectDevice = item
        if (item.status == WifiP2pDevice.CONNECTED) {
            main.manager!!.cancelConnect(main.channel!!, object : ActionListener {
                override fun onSuccess() {
                }
                override fun onFailure(p0: Int) {
                }
            })
            main.removeGroup()
        } else {
            val wifiP2pConfig = WifiP2pConfig()
            wifiP2pConfig.deviceAddress = item.deviceAddress
            wifiP2pConfig.wps.setup = WpsInfo.PBC
            connectState = ConnectState.Wait
            connect(wifiP2pConfig)
            main.showProgress("尝试连接到:" + item.deviceName)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(config: WifiP2pConfig) {
        main.manager!!.connect(main.channel!!, config, object : ActionListener {
            override fun onSuccess() {
                Log.d("execute connect ", " success")
            }
            override fun onFailure(p0: Int) {
                Log.d("execute connect ", " failed")
                main.snackbar("Connect failed. Retry.").show()
            }
        })
    }

    private fun getPeerAdapter(): ArrayAdapter<WifiP2pDevice> {
        return object: ArrayAdapter<WifiP2pDevice>(requireContext(), android.R.layout.simple_list_item_2, ArrayList()) {
            @SuppressLint("ViewHolder", "SetTextI18n")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = LayoutInflater.from(requireContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                val textView2 = view.findViewById<TextView>(android.R.id.text2)
                val device = getItem(position)
                if (device != null) {
                    textView.text = device.deviceName + " - " + getDeviceStatus(device.status)
                    textView2.text = device.deviceAddress
                }
                return view
            }
        }
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        info?.run {
            main.hideProgress()
            if (wifiP2pEnabled == 0) {
                main.removeGroup()
                return
            }
            if (wifiP2pEnabled == 1) {
                main.showProgress("已连接到设备")
            } else {
                main.showProgress("已连接到:" + connectDevice.deviceName)
            }
            connectState = ConnectState.Connected
            Log.d("connection info ", groupOwnerAddress.toString())
            if (groupFormed && isGroupOwner) {

                Log.d("connection info ", "execute socket server")

            } else if (groupFormed) {

                Log.d("connection info ", "receive socket content")
            } else {
                Log.d("connection info ", "invalid")
            }
        }
    }

    private fun checkSupportP2p(): String {
        // Device capability definition check
        if (!Core.app.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            return "Wi-Fi Direct is not supported by this device."
        }
        // Hardware capability check
        val wifiManager = Core.app.getSystemService(WifiManager::class.java)
            ?: return "Cannot get Wi-Fi system service."
        if (!wifiManager.isP2pSupported) {
            return "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off."
        }
        Core.app.getSystemService(WifiP2pManager::class.java) ?: return "Cannot get Wi-Fi Direct system service."
        return ""
    }

    private fun isWifiConnected(): Boolean {
        return Phone.getNetworkMode() == NetworkMode.Wifi
    }

    fun wifiState(state: Boolean) {
        if (state) { // wifi connected
            Log.d("receiver state ", "connect")
        } else { // wifi disconnect
            Log.d("receiver state ", "disconnect")
        }
    }

    fun updateThisDevice(currentDevice: WifiP2pDevice?) {
        thisDevice = currentDevice
        thisDevice?.run {
            Log.d("device name ", deviceName)
            Log.d("device state ", getDeviceStatus(status))
        }
    }

    fun disconnect() {
        connectState = ConnectState.None
        main.hideProgress()
        if (wifiP2pEnabled != 0) {
            main.snackbar("连接已断开.").show()
        }
    }

    private fun requestPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun getDeviceStatus(deviceStatus: Int): String {
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }

    enum class ConnectState {
        None,
        Wait,
        Connected
    }
}