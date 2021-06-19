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
import com.tut.firebasechat.repositories.AuthRepository
import com.tut.firebasechat.repositories.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository
    private var user: User? = null
    private val app = getApplication<Application>()
    private val defaultDispatcher = Dispatchers.IO

    fun putProfileDetails(name: String, username: String, imgUri: Uri?) = liveData(defaultDispatcher) {
        val checkUsernameExistsResponse = repository.checkUsernameExists(username)

        if (checkUsernameExistsResponse.response == FirebaseResponse.SUCCESS) {

            if (!checkUsernameExistsResponse.data!!) {
                val putImageResponse = imgUri?.let {
                    repository.putImage(imgUri)
                }

                if (putImageResponse == null ||
                    putImageResponse.response == FirebaseResponse.SUCCESS) {

                    val token =
                        getStore().
                        getString(app.getString(R.string.KEY_FCM_TOKEN), "")!!

                    AuthRepository.getFirebaseUser()?.let{ firebaseUser ->

                        user = User(name = name,
                            phone =  firebaseUser.phoneNumber!!,
                            token = token,
                            username = username,
                            dp_url = putImageResponse?.data?:"")

                        val putDetailsResponse = repository.putProfileDetails(user)

                        if (putDetailsResponse ==  FirebaseResponse.SUCCESS) {
                            putProfileCompleted(tokenAdded = true)
                        }
                        emit(putDetailsResponse)
                    }
                } else {
                    emit(putImageResponse.response)
                }
            } else {
                emit(FirebaseResponse.DUPLICATE_USERNAME)
            }
        } else {
            emit(checkUsernameExistsResponse.response)
        }
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