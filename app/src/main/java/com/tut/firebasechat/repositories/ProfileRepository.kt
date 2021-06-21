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

    private val usersReference: CollectionReference =
        FirebaseFirestore.getInstance().collection(USER_COLLECTIONS)

    private val reference: DocumentReference =
        usersReference.document(firebaseAuth.currentUser!!.uid)

    suspend fun isProfileExists(): ResponseWrapper<Boolean> = withContext(defaultDispatcher) {
        try {
            val result = reference.get().await()
            if (result.exists()) ResponseWrapper(FirebaseResponse.SUCCESS, true)
            else ResponseWrapper(FirebaseResponse.SUCCESS, false)
        } catch (e: Exception) {
            ResponseParser.parseException<Boolean>(e)
        }
    }

    suspend fun getProfile(uid: String): ResponseWrapper<User> = withContext(defaultDispatcher) {
        try {
            val result = usersReference.document(uid).get().await()
            if (result.exists()) {
                ResponseWrapper(FirebaseResponse.SUCCESS, result.toObject(User::class.java))
            }
            else ResponseWrapper(FirebaseResponse.INVALID_CREDENTIALS)
        } catch (e: Exception) {
            ResponseParser.parseException<User>(e)
        }
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
    suspend fun checkUsernameExists(username: String): ResponseWrapper<Boolean> =
        withContext(defaultDispatcher) {
            try {
                val result = usersReference.whereEqualTo(USERNAME_FIELD, username).get().await()
                if (result.isEmpty) ResponseWrapper((FirebaseResponse.SUCCESS), false)
                else ResponseWrapper((FirebaseResponse.SUCCESS), true)
            } catch (e: Exception) {
                ResponseParser.parseException<Boolean>(e)
            }
    }

    suspend fun putImage(uri: Uri): ResponseWrapper<String> = withContext(defaultDispatcher) {
        val storageRef = FirebaseStorage.getInstance()
            .getReference("${USER_COLLECTIONS}/${FirebaseAuth.getInstance().currentUser!!.uid}.jpg")
        try {
            storageRef.putFile(uri).await()
            val result = storageRef.downloadUrl.await()
            ResponseWrapper(FirebaseResponse.SUCCESS, result.toString())
        } catch (e: Exception) {
            ResponseParser.parseException<String>(e)
        }
    }

    suspend fun putProfileDetails(user: User?): FirebaseResponse = withContext(defaultDispatcher) {
        if (user == null) return@withContext FirebaseResponse.INVALID_CREDENTIALS
        try {
            reference.set(user).await()
            FirebaseResponse.SUCCESS
        } catch (e: Exception) {
            ResponseParser.parseException(e)
        }
    }

    suspend fun putFcmToken(token: String): FirebaseResponse = withContext(defaultDispatcher){
        try {
            usersReference.document(firebaseAuth.currentUser!!.uid).update(TOKEN_FIELD, token).await()
            FirebaseResponse.SUCCESS
        } catch (e: Exception) {
            ResponseParser.parseException(e)
        }
    }
}