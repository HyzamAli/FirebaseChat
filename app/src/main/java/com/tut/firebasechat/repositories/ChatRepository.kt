package com.tut.firebasechat.repositories

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.tut.firebasechat.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber


const val USER_COLLECTIONS = "Users"
const val CHAT_COLLECTIONS = "Chats"
const val TIME_STAMP_FIELD = "time_stamp"

object ChatRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val chatReference: CollectionReference =
            FirebaseFirestore.getInstance()
                    .collection(USER_COLLECTIONS)
                    .document(firebaseAuth.uid?:"")
                    .collection(CHAT_COLLECTIONS)

    /**
     * Experimental method. This method handles callbacks when chats are added and modified without
     * side effects.
     *
     * currently not implemented - list update on DELETION of chats
     * https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/DocumentChange
     */
    //TODO: handle errors more specifically
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChats() = callbackFlow {
        val subscription = chatReference.orderBy(TIME_STAMP_FIELD)
            .addSnapshotListener{ snapshot, e ->
            if (e != null) {
                offer(parseException(e))
                close(e)
            }
            if (snapshot == null) {
                offer(parseException(FirebaseException("Null Snapshot")))
                close(e)
            }
            try {
                val chatList = mutableListOf<Chat>()
                for (docChange: DocumentChange in snapshot!!.documentChanges) {
                    val status = when {
                            docChange.newIndex == -1 -> STATUS.REMOVED
                            docChange.oldIndex == -1 -> STATUS.ADDED
                            else -> STATUS.MODIFIED
                    }
                    docChange.document.toObject(Chat::class.java).let {
                        it.chatStatus = status
                        chatList.add(it) }
                }
                offer(ResponseWrapper(FirebaseResponse.SUCCESS, chatList.toList()))
            } catch (exception: Exception) {
                offer(parseException(exception))
                close(e)
            }
        }
        awaitClose{subscription.remove()}
    }

    private fun <T> parseException(exception: Exception): ResponseWrapper<T>{
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        return when(exception) {
            is FirebaseNetworkException -> ResponseWrapper(FirebaseResponse.NO_INTERNET)
            is FirebaseFirestoreException -> ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS) //TODO: add correct exception
            else -> ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }
}