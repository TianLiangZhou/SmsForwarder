package com.idormy.sms.forwarder

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.idormy.sms.forwarder.data.Empty
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.sender.Types
import com.idormy.sms.forwarder.ui.sender.preference.*
import com.idormy.sms.forwarder.utilities.Action
import com.idormy.sms.forwarder.view.SenderViewModel
import com.idormy.sms.forwarder.view.SenderViewModelFactory
import com.idormy.sms.forwarder.widget.AlertDialogFragment

class SenderConfigActivity : AppCompatActivity() {


    private val child by lazy { supportFragmentManager.findFragmentById(R.id.content) as SenderPreferenceFragment}

    internal val dataStoreRepository by lazy { (application as App).dataStoreRepository }

    internal val senderViewModel: SenderViewModel by viewModels {
        SenderViewModelFactory(Core.sender)
    }

    class UnsavedChangesDialogFragment : AlertDialogFragment<Empty, Empty>() {
        override fun AlertDialog.Builder.prepare(listener: DialogInterface.OnClickListener) {
            setTitle(R.string.unsaved_changes_prompt)
            setPositiveButton(R.string.yes, listener)
            setNegativeButton(R.string.no, listener)
            setNeutralButton(android.R.string.cancel, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_sender_config)
        setSupportActionBar(findViewById(R.id.toolbar))
        val title: String = if (savedInstanceState == null) {
            intent.extras!!.getString(Action.title, "")
        } else {
            savedInstanceState.getSerializable(Action.title) as String
        }
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_round_close_24)
            setTitle(title)
        }
        val position: Int = if (savedInstanceState == null) {
            intent.extras!!.getInt(Action.position, 0)
        } else {
            savedInstanceState.getSerializable(Action.position) as Int
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, this.getPreferenceFragment(position), this::class.simpleName)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.save_config, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = child.onOptionsItemSelected(item)

    override fun onBackPressed() {
        if (Core.dataStore.changed) UnsavedChangesDialogFragment().apply {
            key()
        }.show(supportFragmentManager, null) else super.onBackPressed()
    }

    private fun getPreferenceFragment(position: Int): Fragment {

        return when(Types.from(position)) {
            Types.DingDing-> DingDingPreferenceFragment()
            Types.Email-> EmailPreferenceFragment()
            Types.Bark-> BarkPreferenceFragment()
            Types.WebNotify-> WebNotifyPreferenceFragment()
            Types.QYWXGroup-> WXGroupPreferenceFragment()
            Types.QYWX -> WXAppPreferenceFragment()
            Types.ServerChan -> ServerPreferenceFragment()
            Types.Telegram -> TelegramPreferenceFragment()
            Types.SMS -> SmsPreferenceFragment()
            Types.FeiShu -> FeishuPreferenceFragment()
            Types.PushPlus -> PushPlusPreferenceFragment()
            else -> DingDingPreferenceFragment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("destory", "test destory")
    }

}