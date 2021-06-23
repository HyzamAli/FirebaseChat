package com.tut.firebasechat.utilities

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object ViewUtility {
    fun showSnack(activity: Activity, message:String, duration: Int = Snackbar.LENGTH_SHORT,
                  runnable: (() -> Unit)? = null) {
        val rootView: View = activity.window.decorView.findViewById(android.R.id.content)
        if(runnable == null) Snackbar.make(rootView, message, duration).show()
        else {
            Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY"){ runnable.invoke() }
                .show()
        }
    }

    fun showToast(context: Context, message:String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}