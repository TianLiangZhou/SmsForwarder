package com.idormy.sms.forwarder.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.adapter.BaseAdapter
import com.idormy.sms.forwarder.adapter.LoggerAdapter
import com.idormy.sms.forwarder.databinding.FragmentHomepageBinding
import com.idormy.sms.forwarder.db.model.LoggerAndRuleAndSender
import com.idormy.sms.forwarder.provider.Core
import com.idormy.sms.forwarder.utilities.MessageType
import com.idormy.sms.forwarder.view.LoggerViewModel
import com.idormy.sms.forwarder.view.LoggerViewModelFactory
import com.idormy.sms.forwarder.widget.PagingLoadStateAdapter
import com.idormy.sms.forwarder.widget.observe
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import org.json.JSONObject


class HomepageFragment(val type: MessageType) : Fragment(), BaseAdapter.Listener<LoggerAndRuleAndSender> {

    private val adapter: LoggerAdapter by lazy { LoggerAdapter() }

    private var _binding: FragmentHomepageBinding? = null

//     This property is only valid between onCreateView and
//     onDestroyView.
    private val binding get() = _binding!!

    private val loggerViewModel: LoggerViewModel by activityViewModels {
        LoggerViewModelFactory(Core.logger)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listView = binding.listView
        listView.layoutManager = LinearLayoutManager(requireContext(),  RecyclerView.VERTICAL, false)
        listView.addItemDecoration(DividerItemDecoration(requireContext(), (listView.layoutManager as LinearLayoutManager).orientation))
        listView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(adapter),
            footer = PagingLoadStateAdapter(adapter)
        )
        adapter.listener = this
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        with(loggerViewModel) {
            observe(progressLiveEvent) { show ->
                if (show) (activity as MainActivity).showProgress()
                else (activity as MainActivity).hideProgress()
            }
            observe(errorMessage) { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                loadLogger(type).collectLatest {
                    adapter.submitData(it)
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.collectLatest {
                    binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.distinctUntilChangedBy { it.refresh }
                    .filter { it.refresh is LoadState.NotLoading }
                    .collect { listView.scrollToPosition(0) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditor(item: LoggerAndRuleAndSender) {
        TODO("Not yet implemented")
    }

    override fun onCopy(item: LoggerAndRuleAndSender) {
        TODO("Not yet implemented")
    }

    override fun onDelete(item: LoggerAndRuleAndSender) {
        TODO("Not yet implemented")
    }

    override fun onClick(view: View, item: LoggerAndRuleAndSender) {
        var response = item.logger.forwardResponse
        if (response.isNotEmpty() && response.startsWith("{")) {
            response = JSONObject(response).toString(4)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("请求结果")
            .setMessage(response)
            .create()
            .show()
    }

    override fun onLongClick(view: View, item: LoggerAndRuleAndSender): Boolean {
        return true
    }

    override fun onShare(item: LoggerAndRuleAndSender) {
    }

}