package com.idormy.sms.forwarder.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.ItemSenderBinding
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.sender.SenderHelper
import com.idormy.sms.forwarder.utilities.Status
import java.text.SimpleDateFormat
import java.util.*


class SenderAdapter : BaseAdapter<Sender>() {

    override fun viewHolder(layout: Int, view: View): BaseViewHolder<Sender> {
        val inflate = ItemSenderBinding.inflate(LayoutInflater.from(view.context))
        return SenderViewHolder(inflate)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    inner class SenderViewHolder(private val binding: ItemSenderBinding): BaseViewHolder<Sender>(binding.root) {
        internal lateinit var item: Sender
        init {
            binding.editorSender.setOnClickListener {
                listener?.apply {
                    onEditor(item)
                }
            }
            binding.copySender.setOnClickListener {
                listener?.apply {
                    onCopy(item)
                }
            }
        }
        override fun onClick(p0: View) {
            listener?.apply {
                onClick(p0, item)
            }
        }

        override fun onLongClick(p0: View): Boolean {
            var result = false
            listener?.apply {
                result = onLongClick(p0, item)
            }
            return result
        }
        @SuppressLint("SetTextI18n")
        override fun bindData(data: Sender) {
            this.item = data
            binding.senderImage.setImageResource(SenderHelper.getImageId(data.type))
            binding.senderImage.isClickable = false
            binding.senderImage.isEnabled = false
            binding.senderName.text = data.name
            binding.date.text = "最近发送: " + SimpleDateFormat("MM/dd HH:mm", Locale.CHINA).format(Date(data.time))

            println(data)
            itemView.isSelected = data.status == Status.On.value
        }
    }

    override fun layout(): Int = R.layout.item_sender

    override fun findItemIndex(id: Long) = dataSet.indexOfFirst { it.id == id}
}