package com.tut.firebasechat.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tut.firebasechat.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.lang.Exception

object MessageRepository {
    private lateinit var startTime: Timestamp

    fun getPrevMessages(messageDocId: String): Flow<PagingData<Message>> {
        startTime = Timestamp.now()
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE.toInt(),
                        enablePlaceholders = false
                ),
                pagingSourceFactory = { ChatMessageSource(messageDocId, startTime) }
        ).flow
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLiveMessageStream(messageDocId: String) = callbackFlow {
        val subscription = FirebaseFirestore.getInstance()
                .collection(CHAT_COLLECTIONS)
                .document(messageDocId)
                .collection(MESSAGE_COLLECTIONS)
                .whereGreaterThanOrEqualTo(TIME_STAMP_FIELD, startTime)
                .orderBy(TIME_STAMP_FIELD)
                .addSnapshotListener{snapshot, e ->
                    if (e != null) {
                        offer(parseException(e))
                        close(e)
                    }
                    if (snapshot == null) {
                        offer(parseException(FirebaseException("Null Snapshot")))
                        close(e)
                    }
                    try {
                        val chatList = mutableListOf<Message>()
                        for (docChange: DocumentChange in snapshot!!.documentChanges) {
                            docChange.document.toObject(Message::class.java).let {
                                chatList.add(it) }
                        }
                        offer(ResponseWrapper(FirebaseResponse.SUCCESS, chatList.toList()))
                    } catch (e: Exception) {
                        offer(parseException(e))
                        close(e)
                    }
                }
        awaitClose{subscription.remove()}
    }

    private fun <T> parseException(exception: Exception): ResponseWrapper<T> {
        Timber.d("MessageRepository: Exception %s", exception.message?:"null message")
        return when(exception) {
            is FirebaseNetworkException -> ResponseWrapper(FirebaseResponse.NO_INTERNET)
            is FirebaseFirestoreException -> ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS) //TODO: add correct exception
            else -> ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }
}