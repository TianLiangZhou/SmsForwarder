package com.idormy.sms.forwarder.ui.sender

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.ScannerActivity
import com.idormy.sms.forwarder.SenderConfigActivity
import com.idormy.sms.forwarder.adapter.BaseAdapter
import com.idormy.sms.forwarder.adapter.SenderAdapter
import com.idormy.sms.forwarder.databinding.FragmentSenderBinding
import com.idormy.sms.forwarder.databinding.LayoutSelectSenderDialogBinding
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.sender.SenderHelper
import com.idormy.sms.forwarder.sender.Types
import com.idormy.sms.forwarder.ui.ProgressToolbarFragment
import com.idormy.sms.forwarder.utilities.Action
import com.idormy.sms.forwarder.utilities.Status
import com.idormy.sms.forwarder.utilities.Worker
import com.idormy.sms.forwarder.view.SenderViewModel
import com.idormy.sms.forwarder.view.SenderViewModelFactory
import com.idormy.sms.forwarder.widget.UndoSnackbarManager
import com.idormy.sms.forwarder.widget.observe
import com.idormy.sms.forwarder.workers.UpdateSenderWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class SenderFragment : ProgressToolbarFragment(), Toolbar.OnMenuItemClickListener, DialogInterface.OnClickListener, BaseAdapter.Listener<Sender> {

    private var _binding: FragmentSenderBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val dialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_sender_title)
            .setAdapter(this.getSenderSetAdapter(), this::selectedSender)
            .create()
    }

    private val scannerResultDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("扫描结果")
            .setPositiveButton("导入", this)
            .setNeutralButton("取消", this)
            .setCancelable(false)
            .create()
    }

    private val senderSets by lazy { resources.getStringArray(R.array.add_sender_menu) }

    private val adapter: SenderAdapter by lazy { SenderAdapter() }

    private val undoManager: UndoSnackbarManager<Sender> by lazy {
        UndoSnackbarManager(main, adapter::undo, adapter::commit)
    }

    private val senderViewModel: SenderViewModel by activityViewModels { SenderViewModelFactory(Core.sender) }

    private var changedData: MutableList<Sender>? = null

    private val permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        startConfig(Sender(type = Types.SMS.value))
    }
    private val main: MainActivity by lazy { requireActivity() as MainActivity }

    private var scannedResult: String? = null

    private val startScannerForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.hasExtra("scanned_result") == true) {
            scannedResult = result.data?.getStringExtra("scanned_result")
            scannerResultDialog.setMessage(
                if (scannedResult!!.startsWith("{") ||  scannedResult!!.startsWith("["))
                    JSONObject(scannedResult!!).toString(4)
                else scannedResult
            )
            scannerResultDialog.show()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.config_manager_menu)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setTitle(R.string.sender_setting)
        changedData = mutableListOf()
        val listView = binding.senderListView
        adapter.listener = this
        listView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        listView.adapter = adapter
        listView.addItemDecoration(DividerItemDecoration(context, (listView.layoutManager as LinearLayoutManager).orientation))
        Core.sender.listener = adapter
        with(senderViewModel) {
            observe(progressLiveEvent) { show ->
                if (show) (activity as MainActivity).showProgress()
                else (activity as MainActivity).hideProgress()
            }
            observe(errorMessage) { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
            observe(senders) {
                adapter.submitList(it)
            }
            if (adapter.itemCount < 1) {
                loadAllSender()
            }
        }
        ItemTouchHelper(TouchCallback()).attachToRecyclerView(listView)
    }

    private fun selectedSender(dialog: DialogInterface, index: Int) {
        val sender = Sender()
        sender.type = index
        if (sender.type == Types.SMS.value) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                startConfig(sender)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_PHONE_STATE)) {
                    main.snackbar("赋予电话卡读取权限").setAction("允许") {
                        permReqLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                    }.show()
                } else {
                    permReqLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                }
            }
        } else {
            startConfig(sender)
        }
    }

    private fun getSenderSetAdapter(): ArrayAdapter<Sender> {
        val senderSets = resources.getStringArray(R.array.add_sender_menu)
        val senderArray = MutableList(senderSets.size) { index: Int ->
            Sender(0, senderSets[index], 0, "", index, 0)
        }

        return object: ArrayAdapter<Sender>(requireActivity(), R.layout.layout_select_sender_dialog, senderArray) {
            @SuppressLint("ViewHolder")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = LayoutSelectSenderDialogBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                view.senderImage.setImageResource(SenderHelper.getImageId(position))
                view.senderName.text = senderArray[position].name;
                return view.root
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (changedData!!.isNotEmpty()) {
            val request = OneTimeWorkRequestBuilder<UpdateSenderWorker>()
                .setInputData(workDataOf(Worker.updateSender to Json.encodeToString(changedData)))
                .build()
            WorkManager.getInstance(requireContext()).enqueue(request)
        }
    }

    override fun onDestroy() {
        changedData = null
        Core.sender.listener = null
        super.onDestroy()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_scan_qr_code -> {
                startScannerForResult.launch(Intent(context, ScannerActivity::class.java))
                true
            }
            R.id.action_manual_settings -> {
                dialog.show()
                true
            }
            else -> false
        }
    }

    private fun startConfig(sender: Sender) {
        Core.dataStore.serialize(sender)
        startActivity(
            Intent(context, SenderConfigActivity::class.java)
                .putExtra(Action.senderId, sender.id)
                .putExtra(Action.position, sender.type)
                .putExtra(Action.title, senderSets[sender.type])
        )
    }

    override fun onEditor(item: Sender) {
        startConfig(item)
    }

    override fun onCopy(item: Sender) {
        item.id = 0
        item.name += "[副本]"
        senderViewModel.save(item)
    }

    override fun onShare(item: Sender) {
        if (!parentFragmentManager.isStateSaved) {
            MainActivity.QRCodeDialog(item.toString()).show(parentFragmentManager, null)
        }
    }

    override fun onDelete(item: Sender) {
        senderViewModel.delete(item.id)
    }

    override fun onClick(view: View,  item: Sender) {
        view.isSelected = !view.isSelected
        if (changedData!!.contains(item)) {
            Log.d("ref ", "equals")
            changedData!!.remove(item)
        } else {
            item.status = if (view.isSelected) Status.On.value else Status.Off.value
            changedData!!.add(item)
        }
    }

    override fun onClick(dialog: DialogInterface?, flag: Int) {
        if (flag != DialogInterface.BUTTON_POSITIVE) {
            return
        }
        if (scannedResult.isNullOrEmpty()) {
            return
        }
        if (!scannedResult!!.startsWith("{")) {
            return
        }
        val obj = JSONObject(scannedResult!!)
        if (!obj.has("type")) {
            return
        }
        val type = obj.getInt("type")
        if (Types.from(type) == null) {
            return
        }
        val sender = Sender()
        sender.type = type
        if (obj.has("name")) {
            sender.name = obj.getString("name")
        }
        if (obj.has("status")) {
            sender.status = obj.getInt("status")
        }
        if (obj.has("json_setting")) {
            sender.jsonSetting = obj.getJSONObject("json_setting").toString()
        }
        senderViewModel.save(sender)
    }

    override fun onLongClick(view: View,  item: Sender): Boolean {
        return false
    }

    inner class TouchCallback: ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.START) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val index = viewHolder.bindingAdapterPosition
            adapter.remove(index)
            undoManager.remove(Pair(index, (viewHolder as SenderAdapter.SenderViewHolder).item))
        }
    }



}