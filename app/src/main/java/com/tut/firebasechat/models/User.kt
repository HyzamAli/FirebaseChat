package com.tut.firebasechat.models

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
    val phone: String=""
)