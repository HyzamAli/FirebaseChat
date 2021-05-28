package com.tut.firebasechat.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tut.firebasechat.databinding.RowActiveChatsBinding
import com.tut.firebasechat.models.ChatManager

class ActiveChatListAdapter(private val listener: ChatClickListener) :
    ListAdapter<ChatManager, ActiveChatListAdapter.ViewHolder>(ChatManagerDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowActiveChatsBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.user = getItem(position).user
        holder.binding.chat = getItem(position).chat
        holder.binding.root.setOnClickListener{listener.onChatClicked(getItem(position))}
    }

    class ViewHolder(val binding: RowActiveChatsBinding) : RecyclerView.ViewHolder(binding.root)

    class ChatManagerDiffCallBack : DiffUtil.ItemCallback<ChatManager>() {
        override fun areItemsTheSame(oldItem: ChatManager, newItem: ChatManager): Boolean {
            return oldItem.chat.docId == newItem.chat.docId
        }

        /**
         * When using live message updates in future chat screen, this should be UPDATED to
         * point to the latest message and the timestamp
         */
        override fun areContentsTheSame(oldItem: ChatManager, newItem: ChatManager): Boolean {
            return oldItem.user.name == newItem.user.name &&
                    oldItem.chat.time_stamp == newItem.chat.time_stamp
        }

    }

    interface ChatClickListener{
        fun onChatClicked(chatManager: ChatManager)
    }
}
