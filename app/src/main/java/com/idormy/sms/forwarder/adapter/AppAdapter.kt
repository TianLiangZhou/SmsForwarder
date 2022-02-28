package com.idormy.sms.forwarder.adapter

import android.view.LayoutInflater
import android.view.View
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.data.AppInfo
import com.idormy.sms.forwarder.databinding.ItemAppBinding


class AppAdapter : BaseAdapter<AppInfo>() {

    override fun viewHolder(layout: Int, view: View): BaseViewHolder<AppInfo> {
        val inflate = ItemAppBinding.inflate(LayoutInflater.from(view.context))
        return AppViewHolder(inflate)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    inner class AppViewHolder(private val binding: ItemAppBinding): BaseViewHolder<AppInfo>(binding.root) {
        internal lateinit var item: AppInfo

        override fun onClick(view: View) {
            listener?.apply {
                onClick(view, item)
            }
        }

        override fun onLongClick(view: View): Boolean {
            var result = false
            listener?.apply {
                result = onLongClick(view, item)
            }
            return result
        }

        override fun bindData(data: AppInfo) {
            item = data
            binding.appIcon.setImageDrawable(data.appIcon)
            binding.appName.text = data.appName
            binding.pkgName.text = data.pkgName
            binding.verName.text = data.verName
            binding.verCode.text = data.verCode.toString()
        }

    }

    override fun layout(): Int = R.layout.item_app

    override fun findItemIndex(id: Long) = 0

}