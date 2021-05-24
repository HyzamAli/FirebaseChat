package com.tut.firebasechat.viewmodels

import androidx.lifecycle.*
import com.tut.firebasechat.models.*
import com.tut.firebasechat.repositories.AuthRepository
import com.tut.firebasechat.repositories.ChatRepository
import com.tut.firebasechat.repositories.ProfileRepository
import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly
import timber.log.Timber

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository

    private val currentUser = AuthRepository.getFirebaseUser()!!.uid

    val chatManagers: MutableLiveData<MutableList<ChatManager>> =
        MutableLiveData(mutableListOf())

    private val _response: MutableLiveData<FirebaseResponse> = MutableLiveData()

    val response: LiveData<FirebaseResponse>
    get() = _response

    init {
        Timber.d("Init Chat ViewModel, chat managers size %d", chatManagers.value!!.size)
        getChats()
    }

    private fun getChats() = viewModelScope.launch {
        Timber.d("Trying to get chats")
        val responseWrapper = repository.getChats()
        if (responseWrapper.response == FirebaseResponse.SUCCESS) {
            Timber.d("received chats successfully")
            getChatManagers(responseWrapper.data!!).invokeOnCompletion {
                Timber.d("All done, chat managers size %d", chatManagers.value!!.size)
            }
        } else {
            _response.postValue(responseWrapper.response)
            this.cancel()
        }
    }

    private fun getChatManagers(chats: List<Chat>) = viewModelScope.launch {
        Timber.d("chats size: %s, now trying to get users", chats.size)
        chats.forEach { chat ->
            val secondPartyUid =
                if (chat.sender == currentUser) chat.receiver else chat.sender
            val responseWrapper = ProfileRepository.getProfile(secondPartyUid)
            if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                Timber.d("received user successfully")
                chatManagers.value?.add(ChatManager(responseWrapper.data!!, chat))
                Timber.d("chat managers size: %s", chatManagers.value!!.size)
                chatManagers.notifyObserverFromThread()
            } else {
                _response.postValue(responseWrapper.response)
                this.cancel()
            }
        }
    }

    private fun <T> MutableLiveData<T>.notifyObserverFromThread() {
        this.postValue(this.value)
    }

    @TestOnly
    private fun testDelayAndAdd() = viewModelScope.launch {
        delay(7000)
        val user = User("fdfsdfs","David","+918238907122")
        val chat = Chat("fasdfas","sfsf","fdsfdfdw")
        val chatManager1 = ChatManager(user, chat)
        chatManagers.value?.add(chatManager1)
        chatManagers.notifyObserverFromThread()
        Timber.d("added a new item to list and callback notifier")
    }
}