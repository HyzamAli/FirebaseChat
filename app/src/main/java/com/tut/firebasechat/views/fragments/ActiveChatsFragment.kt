package com.tut.firebasechat.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tut.firebasechat.databinding.FragmentActiveChatsBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.viewmodels.ChatViewModel
import com.tut.firebasechat.views.adapters.ActiveChatListAdapter


class ActiveChatsFragment : BaseFragment() {

    private lateinit var binding: FragmentActiveChatsBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ActiveChatListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentActiveChatsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ActiveChatListAdapter()
        binding.usersList.adapter = adapter
        getChats()
    }

    private fun getChats() {
        viewModel.getChats().observe(viewLifecycleOwner) { response ->
            if (response == FirebaseResponse.SUCCESS) adapter.submitList(viewModel.chatManagers)
            else handleError(response)
        }
    }
}