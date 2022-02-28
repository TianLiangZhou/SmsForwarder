package com.idormy.sms.forwarder.adapter

import android.annotation.SuppressLint
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.idormy.sms.forwarder.db.repositories.Listener

abstract class BaseAdapter<T: Parcelable>: RecyclerView.Adapter<BaseViewHolder<T>>(), Listener {

    interface Listener<T: Parcelable> {
        fun onEditor(item: T)
        fun onCopy(item: T)
        fun onDelete(item: T)
        fun onClick(view: View, item: T)
        fun onLongClick(view: View, item: T): Boolean
    }

    protected var dataSet: MutableList<T> = mutableListOf()


    var listener: Listener<T>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, @LayoutRes layout: Int): BaseViewHolder<T> {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(layout, viewGroup, false)
        return viewHolder(layout, view)
    }

    override fun getItemViewType(position: Int): Int {
        return layout()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val data = dataSet[position]
        try {
            holder.bindData(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(data: List<T>) {
        dataSet = data.toMutableList()
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
    }

    fun undo(actions: List<Pair<Int, T>>) {
        for ((index, item) in actions) {
            dataSet.add(index, item)
            notifyItemInserted(index)
        }
    }

    fun commit(actions: List<Pair<Int, T>>) {
        for ((_, item) in actions) {
            listener?.onDelete(item)
        }
    }

    override fun onDelete(id: Long) {
        val index = this.findItemIndex(id) //dataSet.indexOfFirst { it.id == id}
        if (index < 0) return
        dataSet.removeAt(index)
        notifyItemRemoved(index)
    }

    protected abstract fun findItemIndex(id: Long): Int

    /** returns [RecyclerView.ViewHolder] by layouts. */
    protected abstract fun viewHolder(@LayoutRes layout: Int, view: View): BaseViewHolder<T>


    /** returns layout resources by section rows. */
    protected abstract fun layout(): Int

}