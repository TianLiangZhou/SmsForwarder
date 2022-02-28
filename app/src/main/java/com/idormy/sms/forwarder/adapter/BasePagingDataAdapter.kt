package com.idormy.sms.forwarder.adapter

import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagingDataAdapter<T: Parcelable>(callback: DiffUtil.ItemCallback<T>): PagingDataAdapter<T, BaseViewHolder<T>>(callback) {

    var listener: BaseAdapter.Listener<T>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, @LayoutRes layout: Int): BaseViewHolder<T> {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(layout, viewGroup, false)
        return viewHolder(layout, view)
    }

    override fun getItemViewType(position: Int): Int {
        return layout()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        Log.d("position ==== ", position.toString())
        val data = getItem(position)
        try {
            if (data != null) {
                holder.bindData(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract fun findItemIndex(id: Long): Int

    /** returns [RecyclerView.ViewHolder] by layouts. */
    protected abstract fun viewHolder(@LayoutRes layout: Int, view: View): BaseViewHolder<T>


    /** returns layout resources by section rows. */
    protected abstract fun layout(): Int
}