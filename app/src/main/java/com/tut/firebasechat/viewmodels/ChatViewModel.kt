package com.tut.firebasechat.viewmodels

import androidx.lifecycle.ViewModel
import com.tut.firebasechat.repositories.ChatRepository

class ChatViewModel: ViewModel() {

    private val repository = ChatRepository

    fun getChats() = repository.getChats()
}