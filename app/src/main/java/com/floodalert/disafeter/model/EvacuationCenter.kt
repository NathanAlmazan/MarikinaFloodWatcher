package com.floodalert.disafeter.model

data class EvacuationCenter(
    var generatedId: String = "",
    val name: String = "",
    val address: String= "",
    var image: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)