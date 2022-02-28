package com.idormy.sms.forwarder.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.idormy.sms.forwarder.MainActivity
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.FragmentHomeBinding
import com.idormy.sms.forwarder.ui.ToolbarFragment
import com.idormy.sms.forwarder.utilities.MessageType

class HomeFragment : ToolbarFragment(), Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val viewPager = binding.viewPager
        viewPager.adapter = HomeViewpagerAdapter(this)
        TabLayoutMediator(binding.homeTab, viewPager) {tab, position ->
            val name = when (position) {
                0 -> R.string.sms
                1 -> R.string.call
                2-> R.string.app
                else -> R.string.sms
            }
            tab.text = resources.getString(name)
        }.attach()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.rule_sender)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setTitle(R.string.forward_log)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class HomeViewpagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val fragmentMap = mutableMapOf(
            0 to HomepageFragment(MessageType.Sms),
            1 to HomepageFragment(MessageType.Call),
            2 to HomepageFragment(MessageType.App),
        )

        override fun getItemCount(): Int {
            return fragmentMap.size
        }
        override fun createFragment(position: Int): Fragment {
            Log.d("position", position.toString())
            return fragmentMap[position]!!
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.rule -> {
                (activity as MainActivity).navController.navigate(R.id.rule_fragment)
                true
            }
            R.id.sender -> {
                (activity as MainActivity).navController.navigate(R.id.sender_fragment)
                true
            }
            else -> {
                false
            }
        }

    }
}