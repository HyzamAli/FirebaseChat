package com.tut.firebasechat.models

class ResponseWrapper <T> (
    val response: FirebaseResponse,
    val data:T? = null
)