package com.tut.firebasechat.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.tut.firebasechat.databinding.RowMessageRecievedBinding
import com.tut.firebasechat.databinding.RowMessageSentBinding
import com.tut.firebasechat.models.Message


const val VIEW_TYPE_SENDER = 33

const val VIEW_TYPE_RECEIVER = 34

class ViewHolder(val bindingSender: RowMessageSentBinding? = null,
                 val bindingReceiver: RowMessageRecievedBinding? = null)
    : RecyclerView.ViewHolder(bindingSender?.root?:bindingReceiver!!.root)

class MessageListAdapter : PagingDataAdapter<Message, ViewHolder>(Message()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.sender == FirebaseAuth.getInstance().uid) VIEW_TYPE_SENDER
        else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
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
}

class LiveMessageListAdapter() : ListAdapter<Message, ViewHolder>(Message()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.sender == FirebaseAuth.getInstance().uid) VIEW_TYPE_SENDER
        else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
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
}