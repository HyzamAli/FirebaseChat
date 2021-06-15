package com.tut.firebasechat.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentActiveChatsBinding
import com.tut.firebasechat.models.ChatManager
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.viewmodels.ChatViewModel
import com.tut.firebasechat.views.adapters.ActiveChatListAdapter


class ActiveChatsFragment : BaseFragment(), ActiveChatListAdapter.ChatClickListener {

    private lateinit var binding: FragmentActiveChatsBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ActiveChatListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentActiveChatsBinding.inflate(inflater, container, false)
        binding.toolbar.setupWithNavController(
            navController = NavHostFragment.findNavController(this),
            configuration = AppBarConfiguration(setOf(R.id.activeChatsFragment)))
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ActiveChatListAdapter(this)
        binding.usersList.adapter = adapter
        binding.btnSearch.setOnClickListener {
            ActiveChatsFragmentDirections.actionToSearchUser().also {
                NavHostFragment.findNavController(this).navigate(it)
            }
        }
        observeChats()
        observeResponses()
    }

    private fun observeChats() {
        viewModel.chatManagers.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()){
                val newList = list.toList()
                adapter.submitList(newList)
            }
        }
    }

    private fun observeResponses() {
        viewModel.response.observe(viewLifecycleOwner) {response ->
            if (response!= FirebaseResponse.SUCCESS) handleError(response)
        }
    }

    override fun onChatClicked(chatManager: ChatManager) {
        val action = ActiveChatsFragmentDirections.actionToMessages(chatManager.chat.docId,
                chatManager.user.id)
        NavHostFragment.findNavController(this).navigate(action)
    }
}