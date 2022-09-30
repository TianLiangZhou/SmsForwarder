package com.idormy.sms.forwarder

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.idormy.sms.forwarder.data.Empty
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.ui.rule.RulePreferenceFragment
import com.idormy.sms.forwarder.view.RuleViewModel
import com.idormy.sms.forwarder.view.RuleViewModelFactory
import com.idormy.sms.forwarder.widget.AlertDialogFragment

class RuleConfigActivity : AppCompatActivity() {


    private val child by lazy { supportFragmentManager.findFragmentById(R.id.content) as RulePreferenceFragment }

    internal val ruleViewModel: RuleViewModel by viewModels { RuleViewModelFactory(Core.rule) }

    class UnsavedChangesDialogFragment: AlertDialogFragment<Empty, Empty>() {
        override fun AlertDialog.Builder.prepare(listener: DialogInterface.OnClickListener) {
            setTitle(R.string.unsaved_changes_prompt)
            setNegativeButton(R.string.no, listener)
            setPositiveButton(R.string.yes, listener)
            setNeutralButton(R.string.cancel, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_rule_config)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_round_close_24)
            setTitle(R.string.new_rule)
        }
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
}