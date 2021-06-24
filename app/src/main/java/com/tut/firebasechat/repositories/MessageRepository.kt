package com.tut.firebasechat.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tut.firebasechat.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

object MessageRepository {
    private lateinit var startTime: Timestamp

    private val defaultDispatcher = Dispatchers.IO
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

    suspend fun postMessage(messageDocId: String, message: Message): FirebaseResponse =
            withContext(defaultDispatcher) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection(CHAT_COLLECTIONS)
                        .document(messageDocId)
                        .collection(MESSAGE_COLLECTIONS)
                        .add(message)
                        .await()
                    FirebaseResponse.SUCCESS
                } catch (e: Exception) {
                    ResponseParser.parseException(e)
                }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLiveMessageStream(messageDocId: String, timeStamp: Timestamp? = null) = callbackFlow {
        val subscription = FirebaseFirestore.getInstance()
                .collection(CHAT_COLLECTIONS)
                .document(messageDocId)
                .collection(MESSAGE_COLLECTIONS)
                .whereGreaterThanOrEqualTo(TIME_STAMP_FIELD, timeStamp?:startTime)
                .orderBy(TIME_STAMP_FIELD,Query.Direction.DESCENDING)
                .addSnapshotListener{snapshot, e ->
                    if (e != null) {
                        offer(ResponseParser.parseException<List<Message>>(e))
                        close(e)
                    }
                    if (snapshot == null) {
                        offer(ResponseParser
                            .parseException<List<Message>>(FirebaseException("Null Snapshot"))
                        )
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
                        offer(ResponseParser.parseException<List<Message>>(e))
                        close(e)
                    }
                }
        awaitClose{subscription.remove()}
    }
}