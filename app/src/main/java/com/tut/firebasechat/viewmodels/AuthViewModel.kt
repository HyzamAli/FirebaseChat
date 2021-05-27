package com.tut.firebasechat.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.tut.firebasechat.repositories.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository

    fun isUserSignedIn() = repository.isUserSignedIn()

    fun signOutUser() = repository.signOutUser()

    fun requestOTP(phone: String, activity: Activity) = repository.requestOTP(phone, activity)

    fun verifyOTP(code: String) = repository.verifyOTP(code)
}