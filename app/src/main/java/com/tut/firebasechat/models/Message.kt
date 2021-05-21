package com.tut.firebasechat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Message(

        @Exclude
        @DocumentId
        val docId: String="",

        @PropertyName("sender")
        val sender: String="",

        @PropertyName("time_stamp")
        val timeStamp: Timestamp = Timestamp.now(),

        @PropertyName("message")
        val content: String
)
