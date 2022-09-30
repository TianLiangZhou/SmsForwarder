package com.idormy.sms.forwarder.ui.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.adapter.AppAdapter
import com.idormy.sms.forwarder.adapter.BaseAdapter
import com.idormy.sms.forwarder.data.AppInfo
import com.idormy.sms.forwarder.databinding.FragmentAppBinding
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.ui.ProgressToolbarFragment
import com.idormy.sms.forwarder.view.AppViewModel
import com.idormy.sms.forwarder.view.AppViewModelFactory
import com.idormy.sms.forwarder.widget.observe

class AppFragment : ProgressToolbarFragment(), BaseAdapter.Listener<AppInfo> {
    private var _binding: FragmentAppBinding? = null

    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adapter: AppAdapter by lazy { AppAdapter() }

    private val main: MainActivity by lazy { requireActivity() as MainActivity }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.app_list)
        val listView = binding.appListView
        adapter.listener = this
        listView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        listView.adapter = adapter
        listView.addItemDecoration(DividerItemDecoration(context, (listView.layoutManager as LinearLayoutManager).orientation))
        with(appViewModel) {
            observe(progressLiveEvent) { show ->
                if (show) main.showProgress()
                else main.hideProgress()
            }
            observe(errorMessage) { msg ->
                main.snackbar(msg).show()
            }
            observe(packages) {
                adapter.submitList(it)
            }
            loadAllPackage()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditor(item: AppInfo) {
        TODO("Not yet implemented")
    }

    override fun onCopy(item: AppInfo) {
        TODO("Not yet implemented")
    }

    override fun onDelete(item: AppInfo) {
        TODO("Not yet implemented")
    }

    override fun onClick(view: View, item: AppInfo) {
        val cm = requireContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("pkgName", item.pkgName)
        cm.setPrimaryClip(mClipData)
        main.snackbar("已复制包名：" + item.pkgName).show()
    }

    override fun onLongClick(view: View, item: AppInfo): Boolean {
        val intent: Intent? = Core.app.packageManager.getLaunchIntentForPackage(item.pkgName!!)
        startActivity(intent)
        return true
    }

    override fun onShare(item: AppInfo) {
        TODO("Not yet implemented")
    }
}