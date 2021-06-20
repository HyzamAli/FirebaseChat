package com.tut.firebasechat.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.tut.firebasechat.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository
    private val defaultDispatcher = Dispatchers.IO
    var phoneNumber = "+91"
    var otpCode = ""

    fun isUserSignedIn() = repository.isUserSignedIn()

    fun signOutUser() = repository.signOutUser()

    fun requestOTP(activity: Activity) = repository.requestOTP(phoneNumber, activity)

    fun verifyOTP() = liveData(defaultDispatcher) {
        emit(repository.verifyOTP(otpCode))
    }
}