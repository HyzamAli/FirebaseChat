package com.tut.firebasechat.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.tut.firebasechat.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository
    private val defaultDispatcher = Dispatchers.IO

    fun isUserSignedIn() = repository.isUserSignedIn()

    fun signOutUser() = repository.signOutUser()

    fun requestOTP(phone: String, activity: Activity) = repository.requestOTP(phone, activity)

    fun verifyOTP(code: String) = liveData(defaultDispatcher) {
        emit(repository.verifyOTP(code))
    }
}