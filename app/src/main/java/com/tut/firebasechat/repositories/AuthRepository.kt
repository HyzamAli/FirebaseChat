package com.tut.firebasechat.repositories

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.tut.firebasechat.models.FirebaseResponse
import timber.log.Timber
import java.util.concurrent.TimeUnit

object AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var verificationToken: String = ""
    init {
        Timber.plant(Timber.DebugTree())
    }

    fun getFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    fun isUserSignedIn():Boolean = (firebaseAuth.currentUser != null)

    fun signOutUser() = firebaseAuth.signOut()

    fun requestOTP(phone: String, activity: Activity): LiveData<FirebaseResponse> {
        val response: MutableLiveData<FirebaseResponse> = MutableLiveData()
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setActivity(activity)
            .setTimeout(60, TimeUnit.SECONDS)
            .setCallbacks(object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    response.value = FirebaseResponse.SUCCESS
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    parseException(e, response)
                }

                override fun onCodeSent(vToken: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    verificationToken = vToken
                    response.value = FirebaseResponse.CODE_SENT
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        return response
    }

    fun verifyOTP(code: String): LiveData<FirebaseResponse> {
        val response: MutableLiveData<FirebaseResponse> = MutableLiveData()
        val credential = PhoneAuthProvider.getCredential(verificationToken, code)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { response.value = FirebaseResponse.SUCCESS }
            .addOnFailureListener{ exception ->
                parseException(exception, response)
            }
        return response
    }

    private fun parseException(exception: Exception, response: MutableLiveData<FirebaseResponse>) {
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> response.value = FirebaseResponse.INVALID_CREDENTIALS
            is FirebaseTooManyRequestsException -> response.value  = FirebaseResponse.QUOTA_EXCEED
            is FirebaseNetworkException -> response.value  = FirebaseResponse.NO_INTERNET
            else -> response.value = FirebaseResponse.FAILURE_UNKNOWN
        }
    }
}