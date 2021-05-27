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

        @PropertyName("receiver")
        val receiver: String="",


        @PropertyName("time_stamp")
        val time_stamp: Timestamp = Timestamp.now(),

        @Exclude
        var chatStatus: STATUS = STATUS.ADDED
) {
        val getTimeAsString: String
        get() = DateUtility.getFormattedTimeStamp(time_stamp)
}
