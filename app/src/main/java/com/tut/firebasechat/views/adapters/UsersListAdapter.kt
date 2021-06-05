package com.tut.firebasechat.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tut.firebasechat.databinding.RowUsersBinding
import com.tut.firebasechat.models.User

class UsersListAdapter(private val listener: OnUserListClickListener) : PagingDataAdapter<User, UsersListAdapter.ViewHolder>(User()) {
    class ViewHolder(val binding: RowUsersBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.user = getItem(position)
        holder.binding.root.setOnClickListener { listener.onUserClicked(getItem(position)) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(RowUsersBinding.inflate(LayoutInflater.from(parent.context),
                    parent,
                    false))
}

interface OnUserListClickListener {
    fun onUserClicked(user: User?)
}