package com.tut.firebasechat.models

import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.tut.firebasechat.utilities.DateUtility

data class Message(

        @Exclude
        @DocumentId
        val docId: String="",

        @PropertyName("sender")
        val sender: String="",

        @PropertyName("message")
        val message: String="",

        @PropertyName("time_stamp")
        val time_stamp: Timestamp = Timestamp.now()

) : DiffUtil.ItemCallback<Message>() {

        fun getTimeAsString(): String = DateUtility.getFormattedTimeStamp(time_stamp)

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.docId == newItem.docId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
        }
}
