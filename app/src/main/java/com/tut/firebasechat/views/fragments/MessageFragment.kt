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
import com.tut.firebasechat.databinding.FragmentMessageBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.Message
import com.tut.firebasechat.utilities.ViewUtility
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
        viewModel = ViewModelProvider(requireActivity()).get(MessageViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        previousMessageAdapter = MessageListAdapter()
        liveMessageAdapter = LiveMessageListAdapter()
        adapter = ConcatAdapter(listOf(liveMessageAdapter, previousMessageAdapter))
        binding.recyclerList.adapter = adapter
        binding.btnSubmit.setOnClickListener { postMessage(binding.messageField.text.toString()) }
        getPreviousMessages()
    }

    private fun postMessage(content: String) {
        if (content.isEmpty()) return
        binding.messageField.setText("")
        lifecycleScope.launch {
            val response = viewModel.postMessage(args.messageId, content)
            if (response != FirebaseResponse.SUCCESS) {
                //TODO: proper error handling
                ViewUtility.showSnack(requireActivity(),"Something failed")
            }
        }
    }

    private fun getPreviousMessages() {
        var firstCollectionData = true
        jobGetPrevMessages = lifecycleScope.launch {
            viewModel.getPrevMessages(args.messageId).distinctUntilChanged()
                    .collectLatest {
                        if (firstCollectionData) getLiveMessageStream().also {
                            firstCollectionData = false
                        }
                        previousMessageAdapter.submitData(it)
                    }
        }
    }

    private fun getLiveMessageStream() {
        jobGetLiveMessages = lifecycleScope.launch {
            viewModel.getLiveMessageStream(args.messageId).distinctUntilChanged()
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