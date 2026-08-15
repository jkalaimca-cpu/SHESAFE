package com.example.domain.model

data class SafeZone(
    val id: String,
    val name: String,
    val type: SafeZoneType,
    val address: String,
    val distanceMeters: Int,
    val openStatus: String = "Open 24/7",
    val contactPhone: String = "112",
    val latitude: Double,
    val longitude: Double
)

enum class SafeZoneType {
    POLICE_STATION,
    HOSPITAL_24X7,
    METRO_SECURITY_BOOTH,
    WOMEN_HELP_DESK
}
