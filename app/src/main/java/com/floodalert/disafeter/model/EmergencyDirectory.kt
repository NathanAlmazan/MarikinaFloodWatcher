package com.floodalert.disafeter.model

data class EmergencyDirectory(
    var generatedId: String = "",
    val name: String = "",
    val contact: String = "",
    val type: String = "",
    var category: String = ""
)
