package com.tut.firebasechat.repositories

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.tut.firebasechat.models.FirebaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var verificationToken: String = ""
    private val defaultDispatcher = Dispatchers.IO

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
                    response.value = ResponseParser.exceptionParser(e)
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

    suspend fun verifyOTP(code: String) = withContext(defaultDispatcher) {
        lateinit var response: FirebaseResponse
        val credential = PhoneAuthProvider.getCredential(verificationToken, code)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { response = FirebaseResponse.SUCCESS }
            .addOnFailureListener{ exception ->
                response = ResponseParser.exceptionParser(exception)
            }.await()
        response
    }
}