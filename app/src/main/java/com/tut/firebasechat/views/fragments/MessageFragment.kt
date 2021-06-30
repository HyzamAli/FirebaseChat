package com.tut.firebasechat.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.firebase.Timestamp
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentMessageBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.Message
import com.tut.firebasechat.viewmodels.ChatViewModel
import com.tut.firebasechat.viewmodels.MessageViewModel
import com.tut.firebasechat.views.adapters.LiveMessageListAdapter
import com.tut.firebasechat.views.adapters.MessageListAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var viewModel: MessageViewModel
    private val args: MessageFragmentArgs by navArgs()
    private lateinit var messageId: String
    private lateinit var adapter: ConcatAdapter
    private lateinit var previousMessageAdapter: MessageListAdapter
    private lateinit var liveMessageAdapter: LiveMessageListAdapter
    private var jobGetPrevMessages: Job? = null
    private var jobGetLiveMessages: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        binding.toolbar.setupWithNavController(NavHostFragment.findNavController(this))
        binding.toolbar.title = args.user2Name
        viewModel = ViewModelProvider(requireActivity()).get(MessageViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messageId = args.messageId
        previousMessageAdapter = MessageListAdapter()
        liveMessageAdapter = LiveMessageListAdapter()
        adapter = ConcatAdapter(listOf(liveMessageAdapter, previousMessageAdapter))
        binding.recyclerList.adapter = adapter
        binding.btnSubmit.setOnClickListener { postMessage(binding.messageField.text.toString()) }
        binding.messageField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.messageContainer.hint = ""
            else binding.messageContainer.hint = getString(R.string.hint_type_message)
        }
        if (messageId != "") getPreviousMessages()
    }

    private fun postMessage(content: String) {
        if (content.isEmpty()) return
        binding.messageField.setText("")
        if (messageId == "") {
            ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
                .createChat(args.user2).observe(viewLifecycleOwner) { responseWrapper ->
                    if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                        messageId = responseWrapper.data!!
                        getLiveMessageStream(Timestamp.now())
                        postMessage(content)
                    } else {
                        // TODO: handle failure
                    }
                }
        } else {
            viewModel.postMessage(messageId, content).observe(viewLifecycleOwner) { response ->
                if (response != FirebaseResponse.SUCCESS) {
                    // TODO: handle failure
                }
            }
        }
    }

    private fun getPreviousMessages() {
        var firstCollectionData = true
        jobGetPrevMessages = lifecycleScope.launch {
            viewModel.getPrevMessages(messageId).distinctUntilChanged()
                    .collectLatest {
                        if (firstCollectionData) getLiveMessageStream().also {
                            firstCollectionData = false
                        }
                        previousMessageAdapter.submitData(it)
                    }
        }
    }

    private fun getLiveMessageStream(startTime: Timestamp? = null) {
        jobGetLiveMessages = lifecycleScope.launch {
            viewModel.getLiveMessageStream(messageId, startTime).distinctUntilChanged()
                    .collect { responseWrapper ->
                        if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                            responseWrapper.data?.let {
                                if (it.isNotEmpty()) {
                                    val list: MutableList<Message> = it.toMutableList()
                                    list.addAll(liveMessageAdapter.currentList)
                                    withContext(Dispatchers.Main) {
                                        liveMessageAdapter.submitList(list.toList()) {
                                            binding.recyclerList.smoothScrollToPosition(0)
                                        }
                                    }
                                }
                            }
                        } // TODO: handle exception
                    }
        }
    }


    override fun onDestroyView() {
        jobGetPrevMessages?.let { if(it.isActive) it.cancel() }
        jobGetLiveMessages?.let { if(it.isActive) it.cancel() }
        super.onDestroyView()
    }
}