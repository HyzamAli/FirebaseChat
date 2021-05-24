package com.tut.firebasechat.repositories

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.tut.firebasechat.models.Chat
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.ResponseWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

object ChatRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val chatReference: CollectionReference =
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(firebaseAuth.uid?:"")
                    .collection("Chats")
    
    private val defaultDispatcher = Dispatchers.IO

    suspend fun getChats(): ResponseWrapper<List<Chat>> = withContext(defaultDispatcher) {
        var response: ResponseWrapper<List<Chat>> = ResponseWrapper(FirebaseResponse.SUCCESS)
        chatReference.orderBy("time_stamp")
                .get()
                .addOnSuccessListener { result ->
                    val chatList = mutableListOf<Chat>()
                    for (document: DocumentSnapshot in result) {
                        document.toObject(Chat::class.java)?.let { it -> chatList.add(it) }
                    }
                    response = ResponseWrapper(FirebaseResponse.SUCCESS, chatList.toList())
                }
                .addOnFailureListener { exception ->
                    response = parseException(exception)
                }
                .await()
        response
    }

    private fun <T> parseException(exception: Exception): ResponseWrapper<T>{
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        return when(exception) {
            is FirebaseNetworkException -> ResponseWrapper(FirebaseResponse.NO_INTERNET)
            else -> ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }
}