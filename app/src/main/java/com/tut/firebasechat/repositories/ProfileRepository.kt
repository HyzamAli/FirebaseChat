package com.tut.firebasechat.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.ResponseWrapper
import com.tut.firebasechat.models.User
import timber.log.Timber

object ProfileRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val reference: DocumentReference = FirebaseFirestore
        .getInstance()
        .collection("Users")
        .document(firebaseAuth.currentUser!!.uid)

    init {
        Timber.plant(Timber.DebugTree())
    }

    fun isProfileExists(): LiveData<ResponseWrapper<Boolean>> {
        val response: MutableLiveData<ResponseWrapper<Boolean>> = MutableLiveData()
        reference.get()
            .addOnSuccessListener { result ->
                if (result.exists()) response.value = ResponseWrapper(FirebaseResponse.SUCCESS, true)
                else ResponseWrapper(FirebaseResponse.SUCCESS, false)
            }
            .addOnFailureListener{ exception ->
                when (exception) {
                    is FirebaseNetworkException -> response.value = ResponseWrapper(FirebaseResponse.NO_INTERNET)
                    else -> response.value = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
                }
            }
        return response
    }

    fun putProfileDetails(user: User?): LiveData<FirebaseResponse> {
        val response: MutableLiveData<FirebaseResponse> = MutableLiveData()
        if (user == null) response.value = FirebaseResponse.INVALID_CREDENTIALS
        else {
            reference.set(user)
                .addOnSuccessListener { response.value = FirebaseResponse.SUCCESS }
                .addOnFailureListener { exception -> parseException(exception, response) }
        }
        return response
    }

    private fun parseException(exception: Exception, response: MutableLiveData<FirebaseResponse>) {
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        when (exception) {
            is FirebaseNetworkException -> response.value = FirebaseResponse.NO_INTERNET
            else -> response.value = FirebaseResponse.FAILURE_UNKNOWN
        }
    }
}