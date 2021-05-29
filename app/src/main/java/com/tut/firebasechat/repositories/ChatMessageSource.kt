package com.tut.firebasechat.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.*
import com.tut.firebasechat.models.Message
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.Exception

const val PAGE_SIZE: Long = 30
const val MESSAGE_COLLECTIONS = "Messages"

class ChatMessageSource(private val messageDocId: String) :
        PagingSource<DocumentSnapshot, Message>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Message>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>):
            LoadResult<DocumentSnapshot, Message> {
        val query = if (params.key == null) {
            FirebaseFirestore.getInstance().collection(CHAT_COLLECTIONS)
                .document(messageDocId)
                .collection(MESSAGE_COLLECTIONS)
                .orderBy(TIME_STAMP_FIELD, Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)
                .get()
        } else {
            FirebaseFirestore.getInstance().collection(CHAT_COLLECTIONS)
                .document(messageDocId)
                .collection(MESSAGE_COLLECTIONS)
                .orderBy(TIME_STAMP_FIELD, Query.Direction.DESCENDING)
                .startAfter(params.key as DocumentSnapshot)
                .limit(PAGE_SIZE)
                .get()
        }

        var nextKey: DocumentSnapshot? = null

        return try {
            val snapshot: QuerySnapshot = query.await()
            val messages: MutableList<Message> = mutableListOf()
            if (!snapshot.isEmpty) {
                nextKey = snapshot.documents.last()
                snapshot.forEach{queryDocumentSnapshot ->
                    val message = queryDocumentSnapshot.toObject(Message::class.java)
                    messages.add(message)
                }
            }
            LoadResult.Page(
                prevKey = null,
                nextKey = nextKey,
                data = messages.toList()
            )
        } catch (e: Exception) {
            Timber.e("paging source error: %s", e.message?:"null")
            LoadResult.Error(e)
        }
    }
}