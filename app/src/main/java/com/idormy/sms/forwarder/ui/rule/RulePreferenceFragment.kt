package com.idormy.sms.forwarder.ui.rule

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.RuleConfigActivity
import com.idormy.sms.forwarder.data.Empty
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.databinding.LayoutTestCustomContentBinding
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.preference.OnPreferenceDataStoreChangeListener
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.provider.Phone
import com.idormy.sms.forwarder.sender.Forwarder
import com.idormy.sms.forwarder.utilities.Action
import com.idormy.sms.forwarder.utilities.Key
import com.idormy.sms.forwarder.utilities.MessageType
import com.idormy.sms.forwarder.widget.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RulePreferenceFragment :  PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, OnPreferenceDataStoreChangeListener {

    private lateinit var simSlot: ListPreference
    private lateinit var field: ListPreference
    private lateinit var sender: SenderPreferenceFragment

    private var ruleId: Long = 0L

    private var enableField: IntArray = intArrayOf(0, 1)


    class TestCustomContentDialog : AlertDialogFragment<Empty, Message>() {
        private var binding: LayoutTestCustomContentBinding? = null
        override fun AlertDialog.Builder.prepare(listener: DialogInterface.OnClickListener) {
            setTitle("自定义测试内容")
            setPositiveButton(R.string.ok, listener)
            setNeutralButton(R.string.cancel, listener)
            binding = LayoutTestCustomContentBinding.inflate(LayoutInflater.from(context))
            setView(binding?.root)
        }
        override fun ret(which: Int): Message {
            val source = binding?.textFieldSource?.editText?.text.toString()
            val content = binding?.textFieldValue?.editText?.text.toString()
            return Message(source, content)
        }
        override fun onDestroy() {
            super.onDestroy()
            binding = null
        }

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = Core.dataStore.store
        addPreferencesFromResource(R.xml.pref_create_rule)
        Core.dataStore.store.registerChangeListener(this)
        val name = findPreference<EditTextPreference>("name")
        name?.setOnBindEditTextListener {
            it.maxLines = 1
            it.inputType = InputType.TYPE_CLASS_TEXT
        }
        val entries = mutableListOf("全部")
        val entryValues = mutableListOf("ALL")
        val sim = Phone.sim()
        sim.forEachIndexed { _, s ->
            entries.add("SIM" + s.simSlotIndex)
            entryValues.add("SIM" + s.simSlotIndex)
        }
        simSlot = findPreference("simSlot")!!
        simSlot.entries = entries.toTypedArray()
        simSlot.entryValues = entryValues.toTypedArray()

        val category = findPreference<ListPreference>("category")
        category?.onPreferenceChangeListener = this
        field = findPreference("field")!!
        sender = findPreference("sender")!!
        val senders = requireActivity().intent.getParcelableArrayListExtra<Sender>(Action.senders)?: emptyList()
        sender.initSenders(senders)
        ruleId = requireActivity().intent.getLongExtra(Action.ruleId, 0L)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference is ListPreference && preference.key == "category") {
            simSlot.isEnabled = newValue != "app"
            when (newValue) {
                "sms" -> {
                    enableField = intArrayOf(0, 1)
                }
                "call" -> {
                    enableField = intArrayOf(1)
                }
                "app" -> {
                    enableField = intArrayOf(2, 3)
                }
            }
        }
        Core.dataStore.changed = true
        return true
    }


    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        Log.d("write key = ", key)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference.key) {
            Key.sender -> SenderPreferenceDialogFragment().apply {
                setArg(Key.sender)
                setTargetFragment(this@RulePreferenceFragment, 0)
            }.show(parentFragmentManager, Key.sender)
            Key.field -> FieldPreferenceDialogFragment.newInstance(Key.field, enableField).apply {
                setTargetFragment(this@RulePreferenceFragment, 0)
            }.show(parentFragmentManager, Key.field)
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_delete -> {
                if (ruleId > 0) {
                    (activity as RuleConfigActivity).ruleViewModel.delete(ruleId)
                }
                true
            }
            R.id.action_test -> {
                TestCustomContentDialog().apply {
                    key()
                }.show(parentFragmentManager, null)
                true
            }
            R.id.action_save -> {
                saveExit()
                true
            }
            else -> false
        }
    }

    private fun saveExit() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rule = (activity as RuleConfigActivity).ruleViewModel.get(ruleId)?: Rule()
            Core.dataStore.deserialize(rule)
            (activity as RuleConfigActivity).ruleViewModel.save(rule)
            Core.dataStore.changed = false
            requireActivity().finish()
        }
    }

    private fun test(message: Message) {
        if (message.source!!.isEmpty() && message.content!!.isEmpty()) {
            Toast.makeText(context, "测试的来源和内容不能同时为空", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val rule = withContext(Dispatchers.IO){
                Core.rule.get(ruleId)?: Rule()
            }
            Core.dataStore.deserialize(rule)
            if (rule.senderId < 1) {
                Toast.makeText(context, getString(R.string.new_sender_first), Toast.LENGTH_SHORT).show()
                return@launch
            }
            val sender = withContext(Dispatchers.IO){
                Core.sender.get(rule.senderId)
            }
            if (sender == null) {
                Toast.makeText(context, getString(R.string.new_sender_first), Toast.LENGTH_SHORT).show()
                return@launch
            }
            val msg = Message(message.source, message.content, System.currentTimeMillis(), MessageType.from(rule.category))
            if (rule.match(msg)) {
                Forwarder.send(sender, msg, null)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AlertDialogFragment.setResultListener<RuleConfigActivity.UnsavedChangesDialogFragment, Empty>(this) { which, _ ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> saveExit()
                DialogInterface.BUTTON_NEGATIVE -> requireActivity().finish()
            }
        }
        AlertDialogFragment.setResultListener<TestCustomContentDialog, Message>(this) { which, message ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    test(message!!)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Core.dataStore.store.unregisterChangeListener(this)
    }
}