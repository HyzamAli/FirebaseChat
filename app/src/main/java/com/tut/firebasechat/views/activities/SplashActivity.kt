package com.tut.firebasechat.views.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.tut.firebasechat.viewmodels.AuthViewModel
import com.tut.firebasechat.viewmodels.ProfileViewModel
import timber.log.Timber

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)
        Timber.plant(Timber.DebugTree())
        val viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        if (!viewModel.isUserSignedIn()) {
            Intent(this, AuthActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }
        else {
            val profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
            if (profileViewModel.isProfileCompleted()) {
                Intent(this, HomeActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            }
            else {
                Intent(this, RegistrationActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            }
        }
    }
}