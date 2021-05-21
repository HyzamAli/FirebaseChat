package com.tut.firebasechat.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
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
    private val usersReference: CollectionReference =
            FirebaseFirestore.getInstance().collection("Users")

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
                parseException(exception, response)
            }
        return response
    }

    fun getProfile(uid: String): LiveData<ResponseWrapper<User>> {
        val response: MutableLiveData<ResponseWrapper<User>> = MutableLiveData()
        usersReference.document(uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result.exists()) {
                        response.value = ResponseWrapper(FirebaseResponse.SUCCESS, result.toObject(User::class.java))
                    } else {
                        response.value = ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
                    }
                }.addOnFailureListener{ exception ->
                    parseException(exception, response)
                }
        return response
    }

    fun putProfileDetails(user: User?): LiveData<ResponseWrapper<FirebaseResponse>> {
        val response: MutableLiveData<ResponseWrapper<FirebaseResponse>> = MutableLiveData()
        if (user == null) response.value = ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
        else {
            reference.set(user)
                .addOnSuccessListener { response.value = ResponseWrapper(FirebaseResponse.SUCCESS) }
                .addOnFailureListener { exception -> parseException(exception, response) }
        }
        return response
    }

    private fun <T: Any> parseException(exception: Exception, response: MutableLiveData<ResponseWrapper<T>>) {
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        when (exception) {
            is FirebaseNetworkException -> response.value = ResponseWrapper(FirebaseResponse.NO_INTERNET)
            else -> response.value = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }
}