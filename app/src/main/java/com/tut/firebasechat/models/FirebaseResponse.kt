package com.tut.firebasechat.models

enum class FirebaseResponse {
    SUCCESS,
    NO_INTERNET,
    INVALID_CREDENTIALS,
    QUOTA_EXCEED,
    CODE_SENT,
    OTP_TIMEOUT,
    INVALID_USER,
    FIRE_STORE_EXCEPTION,
    DUPLICATE_USERNAME,
    FAILURE_UNKNOWN /** Unknown Cause, Check log for Failure type and update Enum */
}