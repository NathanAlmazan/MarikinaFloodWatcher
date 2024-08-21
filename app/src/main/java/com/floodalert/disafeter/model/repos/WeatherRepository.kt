package com.floodalert.disafeter.model.repos

import com.floodalert.disafeter.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/*
 This page handles all the necessary request
 for the weather screen. The listener updates the
 UI if there is changes in the database.
*/

class WeatherRepository(private val database: FirebaseFirestore) {
    private val weatherApi = WeatherRetrofitHelper.getInstance().create(WeatherApi::class.java)
    private val forecastApi = ForecastRetrofitHelper.getInstance().create(ForecastApi::class.java)
    private var listener: ListenerRegistration? = null

    fun addDbListener(
        onDocumentEvent: (FloodData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val query = database.collection(DbCollections.WEATHER.db)
            .document("floodData")

        listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val floodData = snapshot.toObject<FloodData>()
                    onDocumentEvent(floodData!!)
                }
            }

    }

    fun removeDbListener() {
        listener?.remove()
    }

    suspend fun fetchWeatherData(latitude: Double, longitude: Double): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val result = weatherApi
                    .getWeatherData(latitude, longitude)
                    .body() ?: throw Exception("Weather is null.")

                val weatherData = WeatherData(
                    result.weather[0].main,
                    result.weather[0].description,
                    result.main.temp,
                    result.main.pressure,
                    result.main.humidity,
                    result.visibility,
                    result.wind.speed,
                    result.wind.deg
                )
                Result.Success(weatherData)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun fetchCurrentRiver(): Result<List<CurrentRiver>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = forecastApi
                    .getCurrentData()
                    .body() ?: throw Exception("Current river is null.")

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun fetchForecastRiver(): Result<List<ForecastRiver>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = forecastApi
                    .getForecastData()
                    .body() ?: throw Exception("Current river is null.")

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun fetchFloodHistory(): Result<List<FloodData>> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = database.collection(DbCollections.FLOOD.db)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val floodHistory = ArrayList<FloodData>()
                for (document in documents) {
                    floodHistory.add(document.toObject())
                }

                Result.Success(floodHistory)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun fetchFloodSummary(): Result<List<FloodSummary>> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = database.collection(DbCollections.SUMMARY.db)
                    .orderBy("index")
                    .get()
                    .await()

                val floodHistory = ArrayList<FloodSummary>()
                for (document in documents) {
                    floodHistory.add(document.toObject())
                }

                Result.Success(floodHistory)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun fetchUserDetails(uid: String): Result<UserDetails?> {
        return withContext(Dispatchers.IO) {
            try {
                val document = database.collection(DbCollections.USERS.db)
                    .document(uid)
                    .get()
                    .await()

                val userDetail = document.toObject<UserDetails>()
                if (userDetail != null) userDetail.uid = uid

                Result.Success(userDetail)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}