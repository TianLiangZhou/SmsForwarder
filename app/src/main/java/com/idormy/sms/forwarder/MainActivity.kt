package com.idormy.sms.forwarder

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.idormy.sms.forwarder.data.Empty
import com.idormy.sms.forwarder.databinding.LayoutMainBinding
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.service.FrontService
import com.idormy.sms.forwarder.ui.ProgressToolbarFragment
import com.idormy.sms.forwarder.utilities.Action
import com.idormy.sms.forwarder.utilities.RedirectAppSettingPermission
import com.idormy.sms.forwarder.view.HomeViewModel
import com.idormy.sms.forwarder.view.HomeViewModelFactory
import com.idormy.sms.forwarder.widget.AlertDialogFragment
import com.idormy.sms.forwarder.widget.ProgressFragment
import com.idormy.sms.forwarder.widget.hasPermissions
import com.idormy.sms.forwarder.widget.observe

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: LayoutMainBinding

    internal lateinit var drawerLayout: DrawerLayout

    internal lateinit var navController: NavController

    internal lateinit var navHostFragment: NavHostFragment

    private lateinit var navView: NavigationView

    internal var channel: WifiP2pManager.Channel? = null

    internal var manager: WifiP2pManager? = null

    private lateinit var fab: FloatingActionButton

    private lateinit var stats: BottomAppBar

    lateinit var snackbar: CoordinatorLayout private set
    fun snackbar(text: CharSequence = "") = Snackbar.make(snackbar, text, Snackbar.LENGTH_LONG).apply {
        anchorView = fab
    }

    private val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(Core.logger, Core.rule, Core.sender) }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {map ->
        if (map[Manifest.permission.RECEIVE_SMS] == true
            || map[Manifest.permission.READ_PHONE_STATE] == true
            || map[Manifest.permission.READ_CALL_LOG] == true
            || map[Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE] == true) {
            if (!FrontService.isRunning) {
                Core.startService()
            }
        }
    }
    private val permSetting = registerForActivityResult(RedirectAppSettingPermission()) {}

    class CleanLoggerAlertDialog: AlertDialogFragment<Empty, Empty>() {
        override fun AlertDialog.Builder.prepare(listener: DialogInterface.OnClickListener) {
            setTitle(R.string.clean_all_prompt)
            setNegativeButton(R.string.no, listener)
            setPositiveButton(R.string.yes, listener)
        }
    }
    private fun clean() {
        CleanLoggerAlertDialog().apply {
            key(this.javaClass.name)
        }.show(navHostFragment.childFragmentManager, null)
        val homeFragment = navHostFragment.childFragmentManager.fragments[0]
        AlertDialogFragment.setResultListener<CleanLoggerAlertDialog, Empty>(homeFragment) { which, _ ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    homeViewModel.clean()
                    snackbar("已清除所有记录.").show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        snackbar = binding.appBarMain.snackbar

        drawerLayout = binding.drawerLayout
        navView = binding.navView

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.rule_fragment, R.id.sender_fragment,
                R.id.to_about, R.id.to_clone, R.id.to_setting, R.id.to_app_list,
                R.id.to_help
            ), drawerLayout
        )
        navView.setupWithNavController(navController)
        fab = binding.appBarMain.fabService
        stats = binding.appBarMain.stats
        fab.setOnClickListener {clean()}
         navController.addOnDestinationChangedListener { _, dest, _ ->
             if (dest.id == R.id.home) {
                 fab.visibility = View.VISIBLE
                 stats.visibility = View.VISIBLE
             } else {
                 fab.visibility = View.INVISIBLE
                 stats.visibility = View.INVISIBLE
             }
         }
        ViewCompat.setOnApplyWindowInsetsListener(fab) { view, insets ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom +
                    resources.getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fab_bottom_margin)
            }
            insets
        }
        with(homeViewModel) {
            observe(openStats) {
                binding.appBarMain.failedCnt.text = it.failedCount
                binding.appBarMain.okCnt.text = it.okCount
                binding.appBarMain.ruleCnt.text = it.ruleCount
                binding.appBarMain.senderCnt.text = it.senderCount
            }
        }
        binding.appBarMain.okStats.text = resources.getString(R.string.success_record) + ":"
        binding.appBarMain.failedStats.text = resources.getString(R.string.failed_record) + ":"
        binding.appBarMain.ruleStats.text = resources.getString(R.string.rule_setting) + ":"
        binding.appBarMain.senderStats.text = resources.getString(R.string.sender_setting) + ":"
        permissionHandler()
    }

    private fun permissionHandler() {
        val requiredPermissions = listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
        )
        val requiredMessages = listOf(
            "短信通知",
            "通话状态",
            "通话记录",
            "通知监听",
        )
        var isStartService = false
        val requestPermissions = mutableListOf<String>()
        val showMessages = mutableListOf<String>()
        requiredPermissions.forEachIndexed {i, permission ->
            if (hasPermissions(permission)) {
                isStartService = true
            } else if (shouldShowRequestPermissionRationale(permission)) {
                showMessages.add(requiredMessages[i])
            } else {
                requestPermissions.add(permission)
            }
        }
        val otherPermissions = listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.BATTERY_STATS,
        )
        otherPermissions.forEach {
            if (!hasPermissions(it)) {
                requestPermissions.add(it)
            }
        }
        if (showMessages.isNotEmpty()) {
            snackbar("应用需要: " + showMessages.joinToString() + "相关权限才能完成对应的功能转发.").setAction("前往设置") {
                permSetting.launch(null)
            }.show()
        } else if (requestPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(requestPermissions.toTypedArray())
        }
        if (isStartService && !FrontService.isRunning) {
            Core.startService()
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.stats()
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeGroup()
    }

    fun showProgress(tips: String? = null) {
        try {
            val first = navHostFragment.childFragmentManager.fragments.filterIsInstance<ProgressToolbarFragment>().first()
            first.progressBar.show()
        } catch (_: Throwable) {
            ProgressFragment().apply {
                arguments = bundleOf(Action.tips to tips)
            }.show(supportFragmentManager, "Progress")
        }
    }

    fun hideProgress() {
        try {
            val first = navHostFragment.childFragmentManager.fragments.filterIsInstance<ProgressToolbarFragment>().first()
            first.progressBar.hide()
        } catch (_: Throwable) {
            supportFragmentManager.fragments.filterIsInstance<ProgressFragment>().forEach { it.dismiss() }
        }
    }


    fun initWifiP2p() {
        if (manager == null) {
            manager = getSystemService(WifiP2pManager::class.java)
        }
        if (channel == null) {
            channel = manager!!.initialize(this, mainLooper, null)
        }
    }

    fun removeGroup() {
        if (manager != null) {
            manager!!.removeGroup(channel!!, object : WifiP2pManager.ActionListener {
                override fun onFailure(reasonCode: Int) {
                    Log.d("remove group ", "remove group failed. Reason :$reasonCode")
                }
                override fun onSuccess() {
                    Log.d("remove group", "remove group success.")
                }
            })
        }
    }
}