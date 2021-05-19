package com.tut.firebasechat.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String
)