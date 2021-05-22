package com.tut.firebasechat.viewmodels

import androidx.lifecycle.*
import com.tut.firebasechat.models.Chat
import com.tut.firebasechat.models.ChatManager
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.repositories.AuthRepository
import com.tut.firebasechat.repositories.ChatRepository
import com.tut.firebasechat.repositories.ProfileRepository

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository
    private val currentUser = AuthRepository.getFirebaseUser()!!.uid
    private val _chatManagers: MutableList<ChatManager> = mutableListOf()
    val chatManagers: List<ChatManager>
    get() = _chatManagers.toList()

    fun getChats(): LiveData<FirebaseResponse> {
        val response: MediatorLiveData<FirebaseResponse> = MediatorLiveData()
        response.addSource(repository.getChats()) { responseWrapper ->
            if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                getChatManagers(responseWrapper.data!!, response)
            } else {
                response.value = responseWrapper.response
            }
        }
        return response
    }

    private fun getChatManagers(chats: List<Chat>, response: MediatorLiveData<FirebaseResponse>) {
        chats.forEach{ chat ->
            val secondPartyUid = if (chat.sender == currentUser) chat.receiver else chat.sender
            response.addSource(ProfileRepository.getProfile(secondPartyUid)) { responseWrapper ->
                if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                    _chatManagers.add(ChatManager(responseWrapper.data!!, chat))
                    if (_chatManagers.size == chats.size) response.value = FirebaseResponse.SUCCESS
                } else response.value = responseWrapper.response
            }
        }
    }
}