package com.floodalert.disafeter.model.repos

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.floodalert.disafeter.model.DbCollections
import com.floodalert.disafeter.model.EvacuationCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/*
 This page fetches the evacuation centers
 in the database and handles request for
 directions
*/

class EvacuateRepository(
    private val database: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val directionsApi = DirectionsRetrofitHelper.getInstance().create(DirectionsApi::class.java)

    suspend fun fetchEvacuationCenters(): Result<List<EvacuationCenter>> {
        return try {
            val documents = database.collection(DbCollections.EVACUATE.db)
                .get()
                .await()

            val evacuationCenters = ArrayList<EvacuationCenter>()
            for (document in documents) {
                val center = document.toObject<EvacuationCenter>()
                center.generatedId = document.id
                evacuationCenters.add(center)
            }

            Result.Success(evacuationCenters)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun createEvacuationCenter(
        evacuationCenter: EvacuationCenter,
        imageFile: Uri
    ): Result<EvacuationCenter> {
        return try {
            val result = uploadEvacuationImage(evacuationCenter.image, imageFile)

            if (result is Result.Success<String>) {
                evacuationCenter.image = result.data
                database.collection(DbCollections.EVACUATE.db)
                    .add(evacuationCenter)
                    .await()

                Result.Success(evacuationCenter)
            }

            Result.Success(evacuationCenter)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun updateEvacuationCenter(
        evacuationCenter: EvacuationCenter,
        imageFile: Uri?
    ): Result<EvacuationCenter> {
        return try {
            var updates = mapOf(
                "name" to evacuationCenter.name,
                "address" to evacuationCenter.address,
                "image" to evacuationCenter.image,
                "longitude" to evacuationCenter.longitude,
                "latitude" to evacuationCenter.latitude
            )

            if (imageFile != null) {
                val result = uploadEvacuationImage(evacuationCenter.image, imageFile)
                if (result is Result.Success<String>) {
                    updates = mapOf(
                        "name" to evacuationCenter.name,
                        "address" to evacuationCenter.address,
                        "image" to result.data,
                        "longitude" to evacuationCenter.longitude,
                        "latitude" to evacuationCenter.latitude
                    )
                }
            }

            database.collection(DbCollections.EVACUATE.db)
                .document(evacuationCenter.generatedId)
                .update(updates)
                .await()

            Result.Success(evacuationCenter)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun deleteEvacuationCenter(evacuationCenter: EvacuationCenter): Result<EvacuationCenter> {
        return try {
            database.collection(DbCollections.EVACUATE.db)
                .document(evacuationCenter.generatedId)
                .delete()
                .await()

            Result.Success(evacuationCenter)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun fetchDirection(start: LatLng, destination: LatLng): Result<DirectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val from = start.latitude.toString() + "," + start.longitude.toString()
                val to = destination.latitude.toString() + "," + destination.longitude.toString()

                val result = directionsApi
                    .getDirection(from, to)
                    .body() ?: throw Exception("Response is null.")

                Result.Success(result)
            } catch (ex: Exception) {
                Result.Error(ex)
            }
        }
    }

    private suspend fun uploadEvacuationImage(fileName: String, imageFile: Uri): Result<String> {
        return try {
            val storageRef = storage.reference

            val imageRef = storageRef.child("evacuations/$fileName")
            imageRef.putFile(imageFile).await()
            val imageURL = imageRef.downloadUrl.await().toString()

            Result.Success(imageURL)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }
}