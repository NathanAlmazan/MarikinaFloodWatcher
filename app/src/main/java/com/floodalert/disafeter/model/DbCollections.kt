package com.floodalert.disafeter.model

// The tables in the database
enum class DbCollections(val db: String) {
    WEATHER("weather"),
    EVACUATE("evacuation"),
    FLOOD("flood"),
    SUMMARY("summary"),
    EMERGENCY("emergency"),
    BULLETIN("bulletin"),
    USERS("users")
}