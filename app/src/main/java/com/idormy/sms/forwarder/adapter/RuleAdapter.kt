package com.idormy.sms.forwarder.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.ItemRuleBinding
import com.idormy.sms.forwarder.db.model.Rule
import com.idormy.sms.forwarder.db.model.Sender
import com.idormy.sms.forwarder.utilities.Status
import java.text.SimpleDateFormat
import java.util.*


class RuleAdapter : BaseAdapter<Rule>() {


    var senderList: List<Sender> = mutableListOf()

    override fun viewHolder(layout: Int, view: View): BaseViewHolder<Rule> {
        val inflate = ItemRuleBinding.inflate(LayoutInflater.from(view.context))
        return RuleViewHolder(inflate)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    inner class RuleViewHolder(private val binding: ItemRuleBinding): BaseViewHolder<Rule>(binding.root) {
        internal lateinit var item: Rule
        init {
            binding.editorRule.setOnClickListener {
                listener?.apply {
                    onEditor(item)
                }
            }
            binding.copyRule.setOnClickListener {
                listener?.apply {
                    onCopy(item)
                }
            }
            binding.shareRule.setOnClickListener {
                listener?.apply {
                    onShare(item)
                }
            }
        }
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

        @SuppressLint("SetTextI18n")
        override fun bindData(data: Rule) {
            item = data
            binding.ruleMatch.text = data.name
            val sender = senderList.firstOrNull { it.id == data.senderId }
            if (sender != null) {
                binding.ruleSenderImage.setImageResource(sender.getImageId())
                binding.ruleSenderImage.isClickable = false
                binding.ruleSenderImage.isEnabled = false
            }
            itemView.isSelected = data.status == Status.On.value
            val format = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA).format(Date(item.time))
            binding.date.text = "最近匹配: $format"
        }

    }

    override fun layout(): Int = R.layout.item_rule

    override fun findItemIndex(id: Long) = dataSet.indexOfFirst { it.id == id}

}