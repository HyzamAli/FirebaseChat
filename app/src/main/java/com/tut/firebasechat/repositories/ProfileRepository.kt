package com.tut.firebasechat.repositories

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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

object ProfileRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val defaultDispatcher = Dispatchers.IO

    private val reference: DocumentReference = FirebaseFirestore
        .getInstance()
        .collection("Users")
        .document(firebaseAuth.currentUser!!.uid)

    private val usersReference: CollectionReference =
            FirebaseFirestore.getInstance().collection("Users")

    suspend fun isProfileExists() = withContext(defaultDispatcher) {
        lateinit var response: ResponseWrapper<Boolean>
        reference.get()
            .addOnSuccessListener { result ->
                response =
                    if(result.exists()) ResponseWrapper(FirebaseResponse.SUCCESS, true)
                    else ResponseWrapper(FirebaseResponse.SUCCESS, false)
            }
            .addOnFailureListener{ response = ResponseParser.parseException<Boolean>(it) }
            .await()
        response
    }

    suspend fun getProfile(uid: String): ResponseWrapper<User> = withContext(defaultDispatcher) {
        lateinit var response: ResponseWrapper<User>
        usersReference.document(uid)
            .get()
            .addOnSuccessListener { result ->
                response =
                    if(result.exists()) {
                        ResponseWrapper(FirebaseResponse.SUCCESS, result.toObject(User::class.java))
                    }
                    else ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
            }
            .addOnFailureListener{ response = ResponseParser.parseException<User>(it) }
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
        lateinit var response: ResponseWrapper<Boolean>
        FirebaseFirestore.getInstance()
                .collection(USER_COLLECTIONS)
                .whereEqualTo(USERNAME_FIELD, username)
                .get()
                .addOnSuccessListener { snapshot ->
                    response =
                        if (snapshot.isEmpty) ResponseWrapper((FirebaseResponse.SUCCESS), false)
                        else ResponseWrapper((FirebaseResponse.SUCCESS), true)
                }.addOnFailureListener{ response = ResponseParser.parseException<Boolean>(it) }
            .await()
        response
    }

    suspend fun putImage(uri: Uri) = withContext(defaultDispatcher) {
        lateinit var response: ResponseWrapper<String>
        val storageRef = FirebaseStorage.getInstance()
                .getReference("${USER_COLLECTIONS}/${FirebaseAuth.getInstance().currentUser!!.uid}.jpg")

        storageRef.putFile(uri)
                .addOnSuccessListener { response = ResponseWrapper(FirebaseResponse.SUCCESS) }
                .addOnFailureListener { response = ResponseParser.parseException<String>(it) }
                .await()

        if (response.response == FirebaseResponse.SUCCESS) {
            storageRef.downloadUrl
                    .addOnSuccessListener {
                        response = ResponseWrapper(FirebaseResponse.SUCCESS, it.toString())
                    }
                    .addOnFailureListener { response = ResponseParser.parseException<String>(it) }
                    .await()
        }
        response
    }

    suspend fun putProfileDetails(user: User?) = withContext(defaultDispatcher) {
        lateinit var response: FirebaseResponse
        if (user == null) response = FirebaseResponse.INVALID_CREDENTIALS
        else {
            reference.set(user)
                    .addOnSuccessListener { response = (FirebaseResponse.SUCCESS) }
                    .addOnFailureListener { response = ResponseParser.parseException(it) }
                    .await()
        }
        response
    }

    suspend fun putFcmToken(token: String): FirebaseResponse {
        lateinit var response: FirebaseResponse
        FirebaseFirestore.getInstance().collection("Users")
            .document(firebaseAuth.currentUser!!.uid)
            .update("token", token)
            .addOnSuccessListener { response = FirebaseResponse.SUCCESS }
            .addOnFailureListener{ response = ResponseParser.parseException(it) }
            .await()
        return response
    }
}