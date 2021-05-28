package com.tut.firebasechat.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tut.firebasechat.models.Message
import kotlinx.coroutines.flow.Flow

object MessageRepository {

    fun getPrevMessages(messageDocId: String): Flow<PagingData<Message>> {
        return Pager(
                config = PagingConfig(
                        pageSize = PAGE_SIZE.toInt(),
                        enablePlaceholders = false
                ),
                pagingSourceFactory = { ChatMessageSource(messageDocId) }
        ).flow
    }

}