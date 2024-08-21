package com.floodalert.disafeter.model.repos

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.floodalert.disafeter.model.DbCollections
import com.floodalert.disafeter.model.EmergencyDirectory
import kotlinx.coroutines.tasks.await

class EmergencyRepository(private val database: FirebaseFirestore) {

    suspend fun fetchDirectories(type: String): Result<List<EmergencyDirectory>> {
        return try {
            val documents = database
                .collection(DbCollections.EMERGENCY.db)
                .document(type)
                .collection("directories")
                .get()
                .await()

            val emergencyDirectories = ArrayList<EmergencyDirectory>()
            for (document in documents) {
                val directory = document.toObject<EmergencyDirectory>()
                directory.generatedId = document.id
                directory.category = type
                emergencyDirectories.add(directory)
            }

            Result.Success(emergencyDirectories)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun createDirectory(directory: EmergencyDirectory): Result<EmergencyDirectory> {
        return try {
            database.collection(DbCollections.EMERGENCY.db)
                .document(directory.category)
                .collection("directories")
                .add(directory)
                .await()

            Result.Success(directory)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun updateDirectory(directory: EmergencyDirectory): Result<EmergencyDirectory> {
        return try {
            val updates = mapOf(
                "name" to directory.name,
                "contact" to directory.contact,
                "type" to directory.type
            )

            database.collection(DbCollections.EMERGENCY.db)
                .document(directory.category)
                .collection("directories")
                .document(directory.generatedId)
                .update(updates)
                .await()

            Result.Success(directory)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun deleteDirectory(directory: EmergencyDirectory): Result<EmergencyDirectory> {
        return try {
            database.collection(DbCollections.EMERGENCY.db)
                .document(directory.category)
                .collection("directories")
                .document(directory.generatedId)
                .delete()
                .await()

            Result.Success(directory)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }
}