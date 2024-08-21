package com.floodalert.disafeter.model

data class WeatherData(
    val title: String = "",
    val desc: String = "",
    val temperature: Double = 273.15,
    val pressure: Double = 0.0,
    val humidity: Double = 0.0,
    val visibility: Double = 0.0,
    val windSpeed: Double = 0.0,
    val windDirection: Double = 0.0,
)