package com.idormy.sms.forwarder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
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
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val KEY_URL = "com.github.shadowsocks.QRCodeDialog.KEY_URL"

        private val iso88591 = StandardCharsets.ISO_8859_1.newEncoder()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: LayoutMainBinding

    internal lateinit var drawer: DrawerLayout

    internal lateinit var navController: NavController

    internal lateinit var navHostFragment: NavHostFragment

    private lateinit var navigation: NavigationView

    internal var channel: WifiP2pManager.Channel? = null

    internal var manager: WifiP2pManager? = null

    private lateinit var fab: FloatingActionButton

    private lateinit var stats: BottomAppBar

    lateinit var snackbar: CoordinatorLayout private set
    fun snackbar(text: CharSequence = "") = Snackbar.make(snackbar, text, Snackbar.LENGTH_LONG).apply {
        anchorView = fab
    }

    private val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(Core.logger, Core.rule, Core.sender) }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        if (map[Manifest.permission.RECEIVE_SMS] == true || map[Manifest.permission.READ_PHONE_STATE] == true || map[Manifest.permission.READ_CALL_LOG] == true || map[Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE] == true) {
            if (!FrontService.isRunning) {
                Core.startService()
            }
        }
    }
    private val permSetting = registerForActivityResult(RedirectAppSettingPermission()) {}

    class CleanLoggerAlertDialog : AlertDialogFragment<Empty, Empty>() {
        override fun AlertDialog.Builder.prepare(listener: DialogInterface.OnClickListener) {
            setTitle(R.string.clean_all_prompt)
            setNegativeButton(R.string.no, listener)
            setPositiveButton(R.string.yes, listener)
        }
    }

    class QRCodeDialog() : DialogFragment() {
        constructor(url: String) : this() {
            arguments = bundleOf(Pair(KEY_URL, url))
        }

        /**
         * Based on:
         * https://android.googlesource.com/platform/packages/apps/Settings/+/0d706f0/src/com/android/settings/wifi/qrcode/QrCodeGenerator.java
         * https://android.googlesource.com/platform/packages/apps/Settings/+/8a9ccfd/src/com/android/settings/wifi/dpp/WifiDppQrCodeGeneratorFragment.java#153
         */
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = try {
            val url = arguments?.getString(KEY_URL)!!
            val size = resources.getDimensionPixelSize(R.dimen.qrcode_size)
            val hints = mutableMapOf<EncodeHintType, Any>()
            if (!iso88591.canEncode(url)) hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8.name()
            val qrBits = MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, size, size, hints)
            ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(size, size)
                setImageBitmap(Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
                    for (x in 0 until size) for (y in 0 until size) {
                        setPixel(x, y, if (qrBits.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                })
            }
        } catch (e: WriterException) {
            e.message?.let { (activity as MainActivity).snackbar().setText(it).show() }
            dismiss()
            null
        }
    }

    private val customTabsIntent by lazy {
        CustomTabsIntent.Builder().apply {
            setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
            }.build())
            setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark))
            }.build())
        }.build()
    }

    fun launchUrl(uri: String) = try {
        customTabsIntent.launchUrl(this, uri.toUri())
    } catch (_: ActivityNotFoundException) {
        snackbar(uri).show()
    }


    private fun clean() {
        CleanLoggerAlertDialog().apply {
            key(this.javaClass.name)
        }.show(navHostFragment.childFragmentManager, null)
        val homeFragment = navHostFragment.childFragmentManager.fragments[0]
        AlertDialogFragment.setResultListener<CleanLoggerAlertDialog, Empty>(homeFragment) { which, _ ->
            when (which) {
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
        drawer = binding.drawerLayout
        navigation = binding.navView
        fab = binding.appBarMain.fabService
        stats = binding.appBarMain.stats

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_holder) as NavHostFragment
        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(navController.graph, drawer)
        navigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, dest, _ ->
            if (dest.id == R.id.home) {
                fab.visibility = View.VISIBLE
                stats.visibility = View.VISIBLE
            } else {
                fab.visibility = View.INVISIBLE
                stats.visibility = View.INVISIBLE
            }
        }
        navigation.setNavigationItemSelectedListener(this)
        fab.setOnClickListener { clean() }
        ViewCompat.setOnApplyWindowInsetsListener(fab) { view, insets ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin =
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom + resources.getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fab_bottom_margin)
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
        requiredPermissions.forEachIndexed { i, permission ->
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // You need this line to handle the navigation
        if (onNavDestinationSelected(item, navController)) {
            if (navigation.parent is DrawerLayout) {
                drawer.closeDrawer(navigation);
            }
            return true
        }
        if (item.itemId == R.id.help) {
            launchUrl(getString(R.string.git_home_url))
            return false
        }
        return true
    }
}