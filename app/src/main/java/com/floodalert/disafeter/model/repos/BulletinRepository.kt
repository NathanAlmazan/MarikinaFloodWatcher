package com.floodalert.disafeter.model.repos

import android.util.Log
import com.floodalert.disafeter.model.Bulletin
import com.floodalert.disafeter.model.DbCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class BulletinRepository(private val database: FirebaseFirestore) {

    suspend fun fetchAnnouncements(): Result<List<Bulletin>> {
        return try {
            val documents = database.collection(DbCollections.BULLETIN.db).get().await()

            val bulletinList = ArrayList<Bulletin>()
            for (document in documents) {
                val bulletin = document.toObject<Bulletin>()
                bulletin.uid = document.id
                bulletinList.add(bulletin)
            }

            Result.Success(bulletinList)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun addAnnouncements(bulletin: Bulletin): Result<Bulletin> {
        return try {
            database.collection(DbCollections.BULLETIN.db).add(bulletin).await()
            updateFloodData(bulletin.title)

            Result.Success(bulletin)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun updateAnnouncement(bulletin: Bulletin): Result<Bulletin> {
        return try {
            val updates = mapOf(
                "title" to bulletin.title,
                "description" to bulletin.description,
                "severity" to bulletin.severity
            )

            database.collection(DbCollections.BULLETIN.db)
                .document(bulletin.uid)
                .update(updates)
                .await()

            Result.Success(bulletin)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun deleteAnnouncement(uid: String): Result<String> {
        return try {
            database.collection(DbCollections.BULLETIN.db)
                .document(uid)
                .delete()
                .await()

            updateFloodData("")

            Result.Success(uid)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    private suspend fun updateFloodData(announcement: String) {
        try {
            database.collection(DbCollections.WEATHER.db)
                .document("floodData")
                .update("announcement", announcement)
                .await()
        } catch (ex: Exception) {
            Log.d("error", ex.message.toString())
        }
    }

}