package com.tut.firebasechat.utilities

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateUtility {
    private const val DATE_FORMAT = "hh:mm a EEE"

    fun getFormattedTimeStamp(timestamp: Timestamp): String =
        SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            .format(timestamp.toDate())
}