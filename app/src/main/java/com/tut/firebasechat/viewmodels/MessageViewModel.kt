package com.tut.firebasechat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.Timestamp
import com.tut.firebasechat.models.Message
import com.tut.firebasechat.models.ResponseWrapper
import com.tut.firebasechat.repositories.AuthRepository
import com.tut.firebasechat.repositories.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class MessageViewModel : ViewModel() {
    private val repository: MessageRepository = MessageRepository

    private val authRepository = AuthRepository

    private val defaultDispatcher = Dispatchers.IO

    fun getPrevMessages(docId: String): Flow<PagingData<Message>> {
        return repository.getPrevMessages(docId)
                .cachedIn(viewModelScope)
    }

    fun getLiveMessageStream(docId: String, timestamp: Timestamp? = null):
            Flow<ResponseWrapper<List<Message>>> {
        return repository.getLiveMessageStream(docId, timestamp)
    }

    fun postMessage(docId: String, content: String) = liveData(defaultDispatcher) {
        val message = Message(sender = authRepository.getFirebaseUser()!!.uid, message = content)
        emit(repository.postMessage(docId, message))
    }
}