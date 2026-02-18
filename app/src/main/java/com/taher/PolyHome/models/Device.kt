package com.taher.PolyHome.models


data class Device(
    val id: String,
    val type: String,
    val availableCommands: List<String>,
    val opening: Double? = null,
    val power: Double? = null
)
