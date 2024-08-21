package com.floodalert.disafeter.model.repos

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/*
 This page handles the request of weather
 data using the free API of open weather
*/

object WeatherRetrofitHelper {
    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

interface WeatherApi {

    @GET("weather")
    suspend fun getWeatherData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") appId: String = "5c2c6388d2f6ec57e8f3712c7ddc97c9"
    ): Response<OpenWeather>

}

data class OpenWeather(
    val weather: List<OpenWeatherData>,
    val main: OpenWeatherMain,
    val visibility: Double,
    val wind: OpenWeatherWind
)

data class OpenWeatherData(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class OpenWeatherMain(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val pressure: Double,
    val humidity: Double
)

data class OpenWeatherWind(
    val speed: Double,
    val deg: Double
)