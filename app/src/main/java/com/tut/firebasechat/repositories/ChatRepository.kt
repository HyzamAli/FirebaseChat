package com.tut.firebasechat.repositories

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.tut.firebasechat.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object ChatRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val defaultDispatcher = Dispatchers.IO

    private val chatReference: CollectionReference =
            FirebaseFirestore.getInstance()
                    .collection(USER_COLLECTIONS)
                    .document(firebaseAuth.uid?:"")
                    .collection(CHAT_COLLECTIONS)

    private val chatCollectionReference =
            FirebaseFirestore.getInstance()
                    .collection(CHAT_COLLECTIONS)

    /**
     * Experimental method. This method handles callbacks when chats are added and modified without
     * side effects.
     *
     * currently not implemented - list update on DELETION of chats
     * https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/DocumentChange
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChats() = callbackFlow {
        val subscription = chatReference.orderBy(TIME_STAMP_FIELD)
            .addSnapshotListener{ snapshot, e ->
            if (e != null) {
                offer(ResponseParser.parseException<List<Chat>>(e))
                close(e)
            }
            if (snapshot == null) {
                offer(ResponseParser.parseException<List<Chat>>(FirebaseException("Null Snapshot")))
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
                offer(ResponseParser.parseException<List<Chat>>(exception))
                close(e)
            }
        }
        awaitClose{subscription.remove()}
    }

    suspend fun createChat(user2: String): ResponseWrapper<String> =
        withContext(defaultDispatcher) {
            if(user2 == "") return@withContext ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
            val dataMap = mapOf(
                PARTY_ONE_FIELD to  FirebaseAuth.getInstance().currentUser!!.uid,
                PARTY_TWO_FIELD to user2
            )
            try {
                val result = chatCollectionReference.add(dataMap).await()
                ResponseWrapper(FirebaseResponse.SUCCESS, result.id)
            } catch (e: Exception) {
                ResponseParser.parseException<String>(e)
            }
    }
}