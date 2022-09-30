package com.idormy.sms.forwarder.ui.sender.preference

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.SenderConfigActivity
import com.idormy.sms.forwarder.data.Empty
import com.idormy.sms.forwarder.data.Message
import com.idormy.sms.forwarder.databinding.LayoutTestCustomContentBinding
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.preference.OnPreferenceDataStoreChangeListener
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.sender.Forwarder
import com.idormy.sms.forwarder.utilities.Action
import com.idormy.sms.forwarder.widget.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


abstract class SenderPreferenceFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, OnPreferenceDataStoreChangeListener {
    private var senderId: Long = 0
    private var position: Int = 0

    class TestCustomContentDialog : AlertDialogFragment<Empty, Message>() {
        private var binding: LayoutTestCustomContentBinding? = null
        override fun AlertDialog.Builder.prepare(listener: DialogInterface.OnClickListener) {
            setTitle("自定义测试内容")
            setPositiveButton(R.string.ok, listener)
            setNeutralButton(R.string.cancel, listener)
            binding = LayoutTestCustomContentBinding.inflate(LayoutInflater.from(context))
            binding?.textFieldSource?.editText?.text = SpannableStringBuilder(getString(R.string.test_phone_num))
            binding?.textFieldValue?.editText?.text = SpannableStringBuilder(getString(R.string.test_sms))
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
        addPreferencesFromResource(getChildView())
        Core.dataStore.store.registerChangeListener(this)
        val name = findPreference<EditTextPreference>("name")
        name?.setOnBindEditTextListener {
            it.maxLines = 1
            it.inputType = InputType.TYPE_CLASS_TEXT
        }
        senderId = requireActivity().intent.getLongExtra(Action.senderId, 0L)
        position = requireActivity().intent.getIntExtra(Action.position, 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AlertDialogFragment.setResultListener<SenderConfigActivity.UnsavedChangesDialogFragment, Empty>(this) { which, _ ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> saveExit()
                DialogInterface.BUTTON_NEGATIVE -> requireActivity().finish()
            }
        }
        AlertDialogFragment.setResultListener<TestCustomContentDialog, Message>(this) { which, message ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> test(message!!)
            }
        }
    }



    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return true
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        Log.d("data store == ", key)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_delete -> {
                if (senderId > 0) {
                    (activity as SenderConfigActivity).senderViewModel.delete(senderId)
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
                this.saveExit()
                true
            }
            else -> false
        }
    }

    private fun saveExit() {
        lifecycleScope.launch(Dispatchers.Main) {
            val sender = (activity as SenderConfigActivity).senderViewModel.get(senderId)?: Sender()
            if (position > 0) {
                sender.type = position
            }
            Core.dataStore.deserialize(sender)
            sender.encodeSetting()
            (activity as SenderConfigActivity).senderViewModel.save(sender)
            Core.dataStore.changed = false
            requireActivity().finish()
        }
    }

    private fun test(message: Message) {
        lifecycleScope.launch(Dispatchers.Main) {
            val sender = (activity as SenderConfigActivity).senderViewModel.get(senderId)?: Sender()
            Core.dataStore.deserialize(sender)
            Forwarder.send(sender, message, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Core.dataStore.store.unregisterChangeListener(this)
    }

    abstract fun getChildView(): Int
}