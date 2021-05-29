package com.tut.firebasechat.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.tut.firebasechat.databinding.RowMessageRecievedBinding
import com.tut.firebasechat.databinding.RowMessageSentBinding
import com.tut.firebasechat.models.Message

class MessageListAdapter : PagingDataAdapter<Message, MessageListAdapter.ViewHolder>(MessageDiffCallBack()) {

    private val viewTypeSender = 33

    private val viewTypeReceiver = 34

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.sender == FirebaseAuth.getInstance().uid) viewTypeSender
        else viewTypeReceiver
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == viewTypeSender) {
            val binding: RowMessageSentBinding =
                    RowMessageSentBinding.inflate(LayoutInflater.from(parent.context),
                            parent,
                            false)
            ViewHolder(bindingSender = binding)
        } else {
            val binding: RowMessageRecievedBinding =
                    RowMessageRecievedBinding.inflate(LayoutInflater.from(parent.context),
                            parent,
                            false)
            ViewHolder(bindingReceiver = binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindingSender?.let {
            holder.bindingSender.setMessage(getItem(position))
        }
        holder.bindingReceiver?.let {
            holder.bindingReceiver.setMessage(getItem(position))
        }
    }

    class ViewHolder(val bindingSender: RowMessageSentBinding? = null,
                     val bindingReceiver: RowMessageRecievedBinding? = null)
        : RecyclerView.ViewHolder(bindingSender?.root?:bindingReceiver!!.root)

    class MessageDiffCallBack : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.docId == newItem.docId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

    }
}