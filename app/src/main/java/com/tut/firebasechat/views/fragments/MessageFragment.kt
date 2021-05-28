package com.tut.firebasechat.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.tut.firebasechat.databinding.FragmentMessageBinding
import com.tut.firebasechat.viewmodels.MessageViewModel
import com.tut.firebasechat.views.adapters.MessageListAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var viewModel: MessageViewModel
    private val args: MessageFragmentArgs by navArgs()
    private lateinit var adapter: MessageListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MessageViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MessageListAdapter()
        binding.messagesList.adapter = adapter
        getPreviousMessages()
    }

    private fun getPreviousMessages() = lifecycleScope.launch {
        viewModel.getPrevMessages(args.messageId).distinctUntilChanged()
                .collectLatest {
                    adapter.submitData(it)
                }
    }
}