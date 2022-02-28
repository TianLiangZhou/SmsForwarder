package com.idormy.sms.forwarder.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.idormy.sms.forwarder.R
import com.idormy.sms.forwarder.databinding.ItemLogBinding
import com.idormy.sms.forwarder.db.model.LoggerAndRuleAndSender
import java.text.SimpleDateFormat
import java.util.*


class LoggerAdapter : BasePagingDataAdapter<LoggerAndRuleAndSender>(LoggerComparator) {

    override fun viewHolder(layout: Int, view: View): BaseViewHolder<LoggerAndRuleAndSender> {
        val inflate = ItemLogBinding.inflate(LayoutInflater.from(view.context))
        return LoggerViewHolder(inflate)
    }


    inner class LoggerViewHolder(private val binding: ItemLogBinding) : BaseViewHolder<LoggerAndRuleAndSender>(binding.root) {
        private lateinit var item: LoggerAndRuleAndSender

        override fun onClick(view: View) {
            listener?.apply {
                this.onClick(view, item)
            }
        }

        override fun onLongClick(p0: View): Boolean {
            var result = false
            listener?.apply {
                result = this.onLongClick(p0, item)
            }
            return result
        }

        @SuppressLint("SetTextI18n")
        override fun bindData(data: LoggerAndRuleAndSender) {
            item = data

            val byte = item.logger.content.toByteArray(Charsets.UTF_16)
            if (byte.size > 140) {
                binding.tlogContent.text = item.logger.content.substring(0, 70) + "..."
            } else {
                binding.tlogContent.text = item.logger.content
            }
            binding.tlogFrom.text = item.logger.from
            binding.tlogTime.text = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA).format(Date(item.logger.time))
            binding.tlogRule.text = item.relation.rule?.name
            val status = when(item.logger.forwardStatus) {
                1 -> R.drawable.ic_round_warning
                2 -> R.drawable.ic_round_check
                else -> R.drawable.ic_round_cancel
            }
            binding.tlogStatusImage.setImageResource(status)
            val sim = when(item.logger.simInfo) {
                "SIM1" -> R.drawable.sim1
                "SIM2" -> R.drawable.sim2
                else -> R.drawable.ic_app
            }
            binding.tlogSimImage.setImageResource(sim)
            if (item.relation.sender != null) {
                binding.tlogSenderImage.setImageResource(item.relation.sender!!.getImageId())
            }
        }

    }

    override fun layout() = R.layout.item_log

    override fun findItemIndex(id: Long): Int  {
        return 0
    }


    object LoggerComparator : DiffUtil.ItemCallback<LoggerAndRuleAndSender>() {
        override fun areItemsTheSame(oldItem: LoggerAndRuleAndSender, newItem: LoggerAndRuleAndSender): Boolean {
            return oldItem.logger.id == newItem.logger.id
        }
        override fun areContentsTheSame(oldItem: LoggerAndRuleAndSender, newItem: LoggerAndRuleAndSender): Boolean {
            return oldItem == newItem
        }
    }

}