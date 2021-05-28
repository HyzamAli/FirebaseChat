package com.tut.firebasechat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.tut.firebasechat.models.Message
import com.tut.firebasechat.repositories.MessageRepository
import kotlinx.coroutines.flow.Flow

class MessageViewModel : ViewModel() {
    private val repository: MessageRepository = MessageRepository

    fun getPrevMessages(docId: String): Flow<PagingData<Message>> {
        return repository.getPrevMessages(docId)
                .cachedIn(viewModelScope)
    }
}