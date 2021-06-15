package com.tut.firebasechat.repositories

import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
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

    /**
     * Return true if a username exists, else returns false
    * */
    suspend fun checkUsernameExists(username: String) = withContext(defaultDispatcher) {
        var response: ResponseWrapper<Boolean> = ResponseWrapper((FirebaseResponse.FAILURE_UNKNOWN))
        FirebaseFirestore.getInstance()
                .collection(USER_COLLECTIONS)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { snapshot ->
                    response = if (snapshot.isEmpty) {
                        ResponseWrapper((FirebaseResponse.SUCCESS), false)
                    }
                    else ResponseWrapper((FirebaseResponse.SUCCESS), true)
                }.addOnFailureListener{
                    response = ResponseWrapper((FirebaseResponse.FAILURE_UNKNOWN))
                }.await()
        response
    }

    suspend fun putImage(uri: Uri) = withContext(defaultDispatcher) {
        var response: ResponseWrapper<String> = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        val storageRef = FirebaseStorage.getInstance()
                .getReference("${USER_COLLECTIONS}/${FirebaseAuth.getInstance().currentUser!!.uid}.jpg")

        storageRef.putFile(uri)
                .addOnSuccessListener {response = ResponseWrapper(FirebaseResponse.SUCCESS) }
                .addOnFailureListener {
                    response = ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)}
                .await()
        if (response.response == FirebaseResponse.SUCCESS) {
            storageRef.downloadUrl
                    .addOnSuccessListener { response =
                            ResponseWrapper(FirebaseResponse.SUCCESS, it.toString())}
                    .addOnFailureListener {
                        response =
                            ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)}
                    .await()
        }
        response
    }

    suspend fun putProfileDetails(user: User?) = withContext(defaultDispatcher) {
        var response: FirebaseResponse = FirebaseResponse.FAILURE_UNKNOWN
        if (user == null) response = FirebaseResponse.INVALID_CREDENTIALS
        else {
            reference.set(user)
                    .addOnSuccessListener { response = (FirebaseResponse.SUCCESS) }
                    .addOnFailureListener {
                        response = (FirebaseResponse.FAILURE_UNKNOWN) }
                    .await()
        }
        response
    }

    private fun <T> parseException(exception: Exception): ResponseWrapper<T>{
        Timber.d("putProfileDetails: Exception %s", exception.message?:"null message")
        return when(exception) {
            is FirebaseNetworkException -> ResponseWrapper(FirebaseResponse.NO_INTERNET)
            else -> ResponseWrapper(FirebaseResponse.FAILURE_UNKNOWN)
        }
    }

    suspend fun putFcmToken(token: String): FirebaseResponse {
        var firebaseResponse = FirebaseResponse.SUCCESS
        FirebaseFirestore.getInstance().collection("Users")
            .document(firebaseAuth.currentUser!!.uid)
            .update("token", token)
            .addOnFailureListener{firebaseResponse = FirebaseResponse.FAILURE_UNKNOWN}
            .await()
        return firebaseResponse
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