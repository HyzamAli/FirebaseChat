package com.tut.firebasechat.views.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tut.firebasechat.databinding.FragmentSearchBinding
import com.tut.firebasechat.models.Chat
import com.tut.firebasechat.models.User
import com.tut.firebasechat.utilities.ViewUtility
import com.tut.firebasechat.viewmodels.ChatViewModel
import com.tut.firebasechat.viewmodels.ProfileViewModel
import com.tut.firebasechat.views.adapters.OnUserListClickListener
import com.tut.firebasechat.views.adapters.UsersListAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class SearchFragment : Fragment(), androidx.appcompat.widget.SearchView.OnQueryTextListener, OnUserListClickListener {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var adapter: UsersListAdapter
    private var searchJob: Job? = null
    private var previousQuery = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        binding.collapsingToolbar.setupWithNavController(
            toolbar = binding.toolbar,
            navController = NavHostFragment.findNavController(this))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchView.setOnQueryTextListener(this)
        adapter = UsersListAdapter(this)
        binding.recyclerList.adapter = adapter
    }

    private fun getProfilesByName(usernameQuery: String) {
        searchJob = lifecycleScope.launch {
            profileViewModel.getProfilesByName(usernameQuery).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query?.length?:0<3) {
            ViewUtility.showToast(requireContext(), "Enter at least 3 Characters")
        } else {
            if (previousQuery == query?:"") return false
            searchJob?.let { if (it.isActive) it.cancel()}
            query?.let { getProfilesByName(query.toLowerCase(Locale.getDefault())) }
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean = false

    override fun onUserClicked(user: User?) {
        var chat: Chat? = null
        user?.let {
            if (chatViewModel.userSet.contains(user.id)) {
                val index = chatViewModel.getIndexByUser(user.id)
                chat = chatViewModel.chatManagers.value?.get(index)?.chat
            }
        }
        val action = SearchFragmentDirections.actionSearchToMessages(chat?.docId?:"",
                user?.id?:"")
        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onDestroyView() {
        searchJob?.let { if (it.isActive) it.cancel()}
        super.onDestroyView()
    }
}