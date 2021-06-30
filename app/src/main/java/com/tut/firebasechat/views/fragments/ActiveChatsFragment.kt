package com.tut.firebasechat.views.fragments

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentActiveChatsBinding
import com.tut.firebasechat.models.ChatManager
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.NetworkUtility
import com.tut.firebasechat.viewmodels.ChatViewModel
import com.tut.firebasechat.views.adapters.ActiveChatListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ActiveChatsFragment : BaseFragment(), ActiveChatListAdapter.ChatClickListener {

    private lateinit var binding: FragmentActiveChatsBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ActiveChatListAdapter
    private lateinit var networkUtility: NetworkUtility
    private var networkConnected = true

    private val connectionRegained = 55
    private val connectionLost = 75

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentActiveChatsBinding.inflate(inflater, container, false)
        binding.toolbar.setupWithNavController(
            navController = NavHostFragment.findNavController(this),
            configuration = AppBarConfiguration(setOf(R.id.activeChatsFragment)))
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        networkUtility = NetworkUtility(requireContext())
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
        observeNetwork()
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

    private fun observeNetwork() {
        networkUtility.observe(viewLifecycleOwner){ isConnected ->
            if (!isConnected) {
                setNetworkBanner(connectionLost)
                binding.noInternetBanner.visibility = View.VISIBLE
                networkConnected = false
            } else {
                if (!networkConnected) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        setNetworkBanner(connectionRegained)
                        delay(3000)
                        if (networkConnected) binding.noInternetBanner.visibility = View.GONE
                    }
                    networkConnected = true
                }
            }
        }
    }

    override fun onChatClicked(chatManager: ChatManager) {
        val action = ActiveChatsFragmentDirections.actionToMessages(
            messageId = chatManager.chat.docId,
            user2 = chatManager.user.id,
            user2Name = chatManager.user.name
        )
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun setNetworkBanner(state: Int) {
        if (state == connectionRegained) {
            binding.noInternetBanner.text = getString(R.string.prompt_online)
            binding.noInternetBanner.setBackgroundColor(
                requireContext().themeColor(R.attr.colorPrimary)
            )
        } else {
            binding.noInternetBanner.text = getString(R.string.prompt_offline)
            binding.noInternetBanner.setBackgroundColor(
                requireContext().themeColor(R.attr.colorError)
            )
        }
    }

    private fun Context.themeColor(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }
}