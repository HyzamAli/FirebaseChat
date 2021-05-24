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
import timber.log.Timber


class ActiveChatsFragment : BaseFragment() {

    private lateinit var binding: FragmentActiveChatsBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ActiveChatListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentActiveChatsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        //Timber.plant(Timber.DebugTree())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ActiveChatListAdapter()
        binding.usersList.adapter = adapter
        observeChats()
        observeResponses()
    }

    private fun observeChats() {
        viewModel.chatManagers.observe(viewLifecycleOwner) { list ->
            Timber.d("notified of list changes %d", list.size)
            if (list.isNotEmpty()){
                Timber.d("name  %s", list[0].user.name)
                val newList = list.toList()
                adapter.submitList(newList)
            } }
    }

    private fun observeResponses() {
        viewModel.response.observe(viewLifecycleOwner) {response ->
            Timber.d("notified of response changes")
            if (response!= FirebaseResponse.SUCCESS) handleError(response)
        }
    }
}