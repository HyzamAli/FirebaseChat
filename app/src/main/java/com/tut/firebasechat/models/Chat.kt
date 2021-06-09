package com.tut.firebasechat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.tut.firebasechat.utilities.DateUtility

enum class STATUS {
        ADDED,
        MODIFIED,
        REMOVED
}
data class Chat(

        @Exclude
        @DocumentId
        val docId: String="",

        @PropertyName("sender")
        val sender: String="",

        @PropertyName("party_id")
        val party_id: String="",


        @PropertyName("time_stamp")
        val time_stamp: Timestamp = Timestamp.now(),

        @PropertyName("message")
        val message: String = "",

        @Exclude
        var chatStatus: STATUS = STATUS.ADDED
) {
        fun getTimeAsString(): String = DateUtility.getFormattedTimeStamp(time_stamp)
}
