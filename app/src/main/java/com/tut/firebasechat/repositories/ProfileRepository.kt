package com.tut.firebasechat.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.models.ResponseWrapper
import com.tut.firebasechat.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

object ProfileRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val reference: DocumentReference = FirebaseFirestore
        .getInstance()
        .collection("Users")
        .document(firebaseAuth.currentUser!!.uid)
    private val usersReference: CollectionReference =
            FirebaseFirestore.getInstance().collection("Users")
    private val defaultDispatcher = Dispatchers.IO

    fun isProfileExists(): LiveData<ResponseWrapper<Boolean>> {
        val response: MutableLiveData<ResponseWrapper<Boolean>> = MutableLiveData()
        reference.get()
            .addOnSuccessListener { result ->
                if (result.exists()) response.value = ResponseWrapper(FirebaseResponse.SUCCESS, true)
                else ResponseWrapper(FirebaseResponse.SUCCESS, false)
            }
            .addOnFailureListener{ exception ->
                parseException(exception, response)
            }
        return response
    }

    suspend fun getProfile(uid: String): ResponseWrapper<User> = withContext(defaultDispatcher) {
        var response: ResponseWrapper<User> = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        usersReference.document(uid)
            .get()
            .addOnSuccessListener { result ->
                response = if(result.exists()) {
                    ResponseWrapper(FirebaseResponse.SUCCESS, result.toObject(User::class.java))
                }
                else ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
            }
            .addOnFailureListener{exception -> response = parseException(exception)}
            .await()
        response
    }

    fun getProfilesByName(usernameQuery: String): Flow<PagingData<User>> = Pager(
            config = PagingConfig(
                    pageSize = USERS_PAGE_SIZE,
                    enablePlaceholders = false
            ), pagingSourceFactory = { UsersSource(usernameQuery) }
    ).flow

    fun putProfileDetails(user: User?): LiveData<ResponseWrapper<FirebaseResponse>> {
        val response: MutableLiveData<ResponseWrapper<FirebaseResponse>> = MutableLiveData()
        if (user == null) response.value = ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
        else {
            reference.set(user)
                .addOnSuccessListener { response.value = ResponseWrapper(FirebaseResponse.SUCCESS) }
                .addOnFailureListener { exception -> parseException(exception, response) }
        }
        return response
    }

    private fun <T> parseException(exception: Exception): ResponseWrapper<T>{
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        return when(exception) {
            is FirebaseNetworkException -> ResponseWrapper(FirebaseResponse.NO_INTERNET)
            else -> ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }


    @Deprecated("User new parse exception")
    private fun <T: Any> parseException(exception: Exception, response: MutableLiveData<ResponseWrapper<T>>) {
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        when (exception) {
            is FirebaseNetworkException -> response.value = ResponseWrapper(FirebaseResponse.NO_INTERNET)
            else -> response.value = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }
}