package com.tut.firebasechat.models

import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(

    @DocumentId
    @Exclude
    val id: String="",

    @PropertyName("name")
    val name: String="",

    @PropertyName("phone")
    val phone: String="",

    @PropertyName("token")
    val token: String=""

) : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.name == newItem.name && oldItem.phone == newItem.phone
}