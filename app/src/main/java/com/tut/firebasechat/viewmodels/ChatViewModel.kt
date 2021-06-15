package com.tut.firebasechat.viewmodels

import androidx.lifecycle.*
import com.tut.firebasechat.models.*
import com.tut.firebasechat.repositories.AuthRepository
import com.tut.firebasechat.repositories.ChatRepository
import com.tut.firebasechat.repositories.ProfileRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import timber.log.Timber

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository

    val chatManagers: MutableLiveData<MutableList<ChatManager>> =
        MutableLiveData(mutableListOf())

    private val _response: MutableLiveData<FirebaseResponse> = MutableLiveData()

    val userSet: HashSet<String> = HashSet()

    val response: LiveData<FirebaseResponse>
    get() = _response

    init {
        Timber.d("Init Chat ViewModel, chat managers size %d", chatManagers.value!!.size)
        getChats()
    }

    private fun getChats() = viewModelScope.launch {
        Timber.d("Trying to get chats")
        repository.getChats()
            .catch { _response.postValue(FirebaseResponse.FAILURE_UNKNOWN)}
            .collect { responseWrapper ->
                Timber.d("Data change notified from firebase")
                if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                    getChatManagers(responseWrapper.data)
                } else {
                    Timber.d("Error retrieving changes from firebase")
                    _response.postValue(FirebaseResponse.FAILURE_UNKNOWN)
                    this.cancel()
                }
            }
    }

    private fun getChatManagers(chats: List<Chat>?) = viewModelScope.launch {
        if (chats == null) this.cancel()
        Timber.d("chats size: %s, now trying to get users", chats?.size)
        chats?.forEach { chat ->
            val status = chat.chatStatus
            if (status == STATUS.ADDED) {
                val responseWrapper = ProfileRepository.getProfile(chat.party_id)
                if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                    responseWrapper.data?.let {
                        userSet.add(it.id)
                        Timber.d("received user successfully")
                        chatManagers.value?.add(0, ChatManager(responseWrapper.data, chat))
                        Timber.d("chat managers size: %s", chatManagers.value!!.size)
                        chatManagers.notifyObserverFromThread()
                    }
                } else {
                    _response.postValue(responseWrapper.response)
                    this.cancel()
                }
            } else if (status == STATUS.MODIFIED){
                Timber.d("user already present modifying list")
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