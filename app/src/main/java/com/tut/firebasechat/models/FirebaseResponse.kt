package com.tut.firebasechat.models

enum class FirebaseResponse {
    SUCCESS,
    NO_INTERNET,
    INVALID_CREDENTIALS,
    QUOTA_EXCEED,
    CODE_SENT,
    FAILURE_UNKNOWN /** Unknown Cause, Check log for Failure type and update Enum */
}