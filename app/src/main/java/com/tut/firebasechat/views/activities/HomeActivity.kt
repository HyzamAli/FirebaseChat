package com.tut.firebasechat.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.tut.firebasechat.R
import com.tut.firebasechat.viewmodels.ProfileViewModel

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ViewModelProvider(this).get(ProfileViewModel::class.java).apply {
            putFcmToken()
        }
    }
}