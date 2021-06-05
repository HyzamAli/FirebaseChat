package com.tut.firebasechat.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.tut.firebasechat.models.User
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.Exception

const val USERS_PAGE_SIZE: Int = 20
class UsersSource(private val usernameQuery: String) : PagingSource<DocumentSnapshot, User>() {
    override fun getRefreshKey(state: PagingState<DocumentSnapshot, User>): DocumentSnapshot? = null

    override suspend fun load(params: LoadParams<DocumentSnapshot>):
            LoadResult<DocumentSnapshot, User> {
        val query = if (params.key == null) {
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereGreaterThanOrEqualTo("username", usernameQuery)
                    .whereLessThanOrEqualTo("username", usernameQuery+ '\uf8ff')
                    .limit(USERS_PAGE_SIZE.toLong())
                    .orderBy("username")
                    .get()

        } else {
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereGreaterThanOrEqualTo("username", usernameQuery)
                    .whereLessThanOrEqualTo("username", usernameQuery+ '\uf8ff')
                    .limit(USERS_PAGE_SIZE.toLong())
                    .orderBy("username")
                    .startAfter(params.key as DocumentSnapshot)
                    .get()
        }

        var nextKey: DocumentSnapshot? = null
        return try {
            val snapshot: QuerySnapshot = query.await()
            val users: MutableList<User> = mutableListOf()
            if (!snapshot.isEmpty) {
                nextKey = snapshot.documents.last()
                val currentUid = FirebaseAuth.getInstance().uid
                snapshot.forEach{queryDocumentSnapshot ->
                    val user = queryDocumentSnapshot.toObject(User::class.java)
                    if (user.id != currentUid) users.add(user)
                }
            }
            LoadResult.Page(
                    prevKey = null,
                    nextKey = nextKey,
                    data = users.toList()
            )
        } catch (e: Exception) {
            Timber.e("paging source error: %s", e.message?:"null")
            LoadResult.Error(e)
        }
    }
}