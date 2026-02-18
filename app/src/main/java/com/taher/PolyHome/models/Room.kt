package com.taher.PolyHome.models

data class Room(
    val id: Int,
    val name: String,
    val devices: MutableList<Device> = mutableListOf()
)
