package com.tut.firebasechat.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.tut.firebasechat.R
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.repositories.ProfileRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        applicationContext.getSharedPreferences(getString(R.string.STORE), MODE_PRIVATE).edit()
            .apply{
                this.putBoolean(getString(R.string.KEY_FCM_UPDATED), false)
                this.putString(getString(R.string.KEY_FCM_TOKEN), token)
                this.apply()
            }
        FirebaseAuth.getInstance().currentUser?.let {
            GlobalScope.launch {
                ProfileRepository.putFcmToken(token).apply {
                    if (this == FirebaseResponse.SUCCESS) {
                        applicationContext.getSharedPreferences(getString(R.string.STORE), MODE_PRIVATE).edit()
                            .apply{
                                this.putBoolean(getString(R.string.KEY_FCM_UPDATED), true)
                                this.apply()
                            }
                    }
                }
            }
        }
    }
}