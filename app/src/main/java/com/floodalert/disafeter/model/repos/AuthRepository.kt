package com.floodalert.disafeter.model.repos

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.floodalert.disafeter.model.DbCollections
import com.floodalert.disafeter.model.UserDetails
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun loginUserWithEmailPassword(
        username: String,
        password: String
    ): Result<FirebaseUser?> {
        return try {
            auth.signInWithEmailAndPassword(username, password).await()
            Result.Success(auth.currentUser)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    fun logOutUser() {
        auth.signOut()
    }

    suspend fun registerUserWithEmailPassword(
        username: String,
        password: String
    ): Result<FirebaseUser?> {
        return try {
            auth.createUserWithEmailAndPassword(username, password).await()
            Result.Success(auth.currentUser)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun createUser(
        username: String,
        email: String,
        contact: String,
        uid: String
    ): Result<UserDetails> {
        return try {
            val user = UserDetails(uid, username, email, contact)
            database.collection(DbCollections.USERS.db)
                .document(uid)
                .set(user)
                .await()

            Result.Success(user)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun editUser(
        username: String,
        email: String,
        contact: String,
        uid: String
    ): Result<UserDetails> {
        return try {
            val updates = mapOf(
                "username" to username,
                "contact" to contact,
                "email" to email
            )

            database.collection(DbCollections.USERS.db)
                .document(uid)
                .update(updates)
                .await()

            Result.Success(UserDetails(uid, username, email, contact))
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun setAdmin(
        user: UserDetails
    ): Result<UserDetails> {
        return try {
            database.collection(DbCollections.USERS.db)
                .document(user.uid)
                .update("admin", !user.admin)
                .await()
            Result.Success(user)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun fetchAllUser(): Result<List<UserDetails>> {
        return try {
            val documents = database.collection(DbCollections.USERS.db)
                .orderBy("admin")
                .get()
                .await()

            val evacuationCenters = ArrayList<UserDetails>()
            for (document in documents) {
                val user = document.toObject<UserDetails>()
                user.uid = document.id
                evacuationCenters.add(user)
            }

            Result.Success(evacuationCenters)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }
}