package com.idormy.sms.forwarder.adapter

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<Data: Parcelable>(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener{
    val context: Context = view.context
    init {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
    }

    /** binds data to the view holder class. */
    @Throws(Exception::class)
    abstract fun bindData(data: Data)
}