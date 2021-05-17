package com.tut.firebasechat.utilities

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar

object ViewUtility {
    fun showSnack(activity: Activity, message:String, duration: Int = Snackbar.LENGTH_SHORT) {
        val rootView: View = activity.window.decorView.findViewById(android.R.id.content)
        Snackbar.make(rootView, message, duration).show()
    }
}