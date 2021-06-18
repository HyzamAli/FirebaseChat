package com.tut.firebasechat.viewmodels

import androidx.lifecycle.*
import com.tut.firebasechat.models.*
import com.tut.firebasechat.repositories.ChatRepository
import com.tut.firebasechat.repositories.ProfileRepository
import com.tut.firebasechat.repositories.ResponseParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.lang.Exception

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository

    val chatManagers: MutableLiveData<MutableList<ChatManager>> =
        MutableLiveData(mutableListOf())

    private val _response: MutableLiveData<FirebaseResponse> = MutableLiveData()

    val userSet: HashSet<String> = HashSet()

    val response: LiveData<FirebaseResponse>
    get() = _response

    init {
        getChats()
    }

    private fun getChats() = viewModelScope.launch {
        repository.getChats()
            .catch { _response.postValue(ResponseParser.parseException(it as Exception))}
            .collect { responseWrapper ->
                if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                    getChatManagers(responseWrapper.data)
                } else {
                    _response.postValue(responseWrapper.response)
                    this.cancel()
                }
            }
    }

    private fun getChatManagers(chats: List<Chat>?) = viewModelScope.launch {
        if (chats == null) this.cancel()
        chats?.forEach { chat ->
            val status = chat.chatStatus
            if (status == STATUS.ADDED) {
                val responseWrapper = ProfileRepository.getProfile(chat.party_id)
                if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                    responseWrapper.data?.let {
                        userSet.add(it.id)
                        chatManagers.value?.add(0, ChatManager(responseWrapper.data, chat))
                        chatManagers.notifyObserverFromThread()
                    }
                } else {
                    _response.postValue(responseWrapper.response)
                    this.cancel()
                }
            } else if (status == STATUS.MODIFIED){
                chatManagers.value?.let {
                    val oldIndex = getIndexByUser(chat.party_id)
                    val oldUser = it[oldIndex].user
                    it.removeAt(oldIndex) // TODO: Add Thread Locking check SearchFragment.kt 82,83
                    it.add(0, ChatManager(oldUser, chat))
                    chatManagers.notifyObserverFromThread()
                }
            } /** To Add provision for chat deletion here */
        }
    }

    suspend fun createChat(user2: String) = repository.createChat(user2)

    fun getIndexByUser(userId: String): Int {
        chatManagers.value?.forEachIndexed { i,chatManager ->
            if (chatManager.user.id == userId) return i
        }
        return -1
    }

    private fun <T> MutableLiveData<T>.notifyObserverFromThread() {
        this.postValue(this.value)
    }
}