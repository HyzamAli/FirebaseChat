package com.tut.firebasechat.repositories

// Path -> /
const val USER_COLLECTIONS = "Users"

// Path ->/
// Path -> /Users/{userDocument}/
const val CHAT_COLLECTIONS = "Chats"

// Path -> /Users/{userDocument}/Chats/{chatDocument}
// Path -> /Chats/{chatDocument}/Messages/{messageDocument}
const val TIME_STAMP_FIELD = "time_stamp"

// Path -> /Users/{userDocument}
const val USERNAME_FIELD = "username"

// Path -> /Users/{userDocument}
const val TOKEN_FIELD = "token"

// Path -> /Users/{userDocument}/Chats/{chatDocument}
const val PARTY_ONE_FIELD = "party_one"

// Path -> /Users/{userDocument}/Chats/{chatDocument}
const val PARTY_TWO_FIELD = "party_two"