package com.floodalert.disafeter.model.repos

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/*
 This page handles the request of weather
 data using the free API of open weather
*/

object ForecastRetrofitHelper {
    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl("https://rivercast.automos.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

interface ForecastApi {

    @GET("current")
    suspend fun getCurrentData(): Response<List<CurrentRiver>>

    @GET("forecast")
    suspend fun getForecastData(): Response<List<ForecastRiver>>

}

data class CurrentRiver(
    val id: Long,
    val level: Float,
    val station: String,
    val timestamp: String
)

data class ForecastRiver(
    val level: Float,
    val timestamp: String
)