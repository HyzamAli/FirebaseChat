package com.tut.firebasechat.repositories

import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.tut.firebasechat.models.Chat
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.ResponseWrapper
import timber.log.Timber

object ChatRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val chatReference: CollectionReference =
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(firebaseAuth.uid?:"")
                    .collection("Chats")

    init {
        Timber.plant(Timber.DebugTree())
    }

    fun getChats() {
        val response: MutableLiveData<ResponseWrapper<List<Chat>>> = MutableLiveData()
        chatReference.orderBy("time_stamp")
                .get()
                .addOnSuccessListener { result ->
                    if (result != null) {
                        val chatList = mutableListOf<Chat>()
                        for (document: DocumentSnapshot in result) {
                            document.toObject(Chat::class.java)?.let { it ->
                                chatList.add(it)
                            }
                        }
                        response.value = ResponseWrapper(FirebaseResponse.SUCCESS, chatList.toList())
                    } else response.value = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
                }
                .addOnFailureListener { exception ->
                    parseException(exception, response)
                }
    }

    private fun <T : Any> parseException(exception: Exception, response: MutableLiveData<ResponseWrapper<T>>) {
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> response.value = ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
            is FirebaseTooManyRequestsException -> response.value  = ResponseWrapper(FirebaseResponse.QUOTA_EXCEED)
            is FirebaseNetworkException -> response.value  = ResponseWrapper(FirebaseResponse.NO_INTERNET)
            else -> response.value = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }
}