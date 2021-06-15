package com.tut.firebasechat.repositories

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.tut.firebasechat.models.FirebaseResponse
import timber.log.Timber
import java.lang.Exception

object ResponseParser {
    fun exceptionParser(e: Exception): FirebaseResponse {
        Timber.d("putProfileDetails: Exception %s", e.message?:"null message")
        return when (e) {
            // When No Network Connectivity is present
            is FirebaseNetworkException -> FirebaseResponse.NO_INTERNET
            // When provided Credentials are wrong
            is FirebaseAuthInvalidCredentialsException -> FirebaseResponse.INVALID_CREDENTIALS
            // When too much requests are sent, need to delay before sending again
            is FirebaseTooManyRequestsException -> FirebaseResponse.QUOTA_EXCEED
            // When provided OTP has timed out
            is FirebaseAuthActionCodeException -> FirebaseResponse.OTP_TIMEOUT
            // When user token has expired
            is FirebaseAuthInvalidUserException -> FirebaseResponse.INVALID_USER
            // When exception is not predefined, CHECK LOG and update parser to handle in future
            else -> FirebaseResponse.FAILURE_UNKNOWN
        }
    }
}