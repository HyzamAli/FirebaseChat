package com.tut.firebasechat.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.tut.firebasechat.R
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.User
import com.tut.firebasechat.repositories.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository
    private val app = getApplication<Application>()
    private val defaultDispatcher = Dispatchers.IO
    var username: String = ""
    var name: String = ""
    var imageUri: Uri? = null

    fun putNewUserDetails() = liveData(defaultDispatcher) {
        val token = getStore().getString(app.getString(R.string.KEY_FCM_TOKEN), "")!!
        val result = repository.putNewUserDetails(
            username = username, name = name, token = token, imageUri = imageUri
        )
        if (result == FirebaseResponse.SUCCESS) putProfileCompleted(tokenAdded = true)
        emit(result)
    }

    fun getProfilesByName(usernameQuery: String): Flow<PagingData<User>> {
        return repository.getProfilesByName(usernameQuery)
                .cachedIn(viewModelScope)
    }

    fun isProfileExists() = liveData(defaultDispatcher) {
        val response = repository.isProfileExists()
        if (response.response == FirebaseResponse.SUCCESS && response.data!!) putProfileCompleted()
        emit(response)
    }

    fun putFcmToken() {
        val isFcmTokenUpdated =
            getStore().getBoolean(app.getString(R.string.KEY_FCM_UPDATED), false)
        val fcmToken =
            getStore().getString(app.getString(R.string.KEY_FCM_TOKEN), "")
        if (!isFcmTokenUpdated) {
            viewModelScope.launch {
                val response = repository.putFcmToken(fcmToken!!)
                if (response == FirebaseResponse.SUCCESS) {
                    putProfileCompleted(tokenAdded = true)
                }
            }
        }
    }

    fun isProfileCompleted(): Boolean =
            getStore().getBoolean(app.getString(R.string.KEY_PROFILE_UPDATED), false)

    private fun putProfileCompleted(tokenAdded: Boolean = false) =
            getStore().edit().apply {
                this.putBoolean(app.getString(R.string.KEY_PROFILE_UPDATED), true)
                this.putBoolean(app.getString(R.string.KEY_FCM_UPDATED), tokenAdded)
                this.apply()
            }

    private fun getStore(): SharedPreferences =
            app.getSharedPreferences(app.getString(R.string.STORE), 0)
}