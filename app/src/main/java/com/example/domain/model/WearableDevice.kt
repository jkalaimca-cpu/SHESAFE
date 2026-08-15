package com.example.domain.model

data class WearableDevice(
    val id: String,
    val name: String,
    val deviceType: WearableType,
    val isConnected: Boolean,
    val batteryPercent: Int,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val macAddress: String = "AA:BB:CC:DD:EE:01"
)

enum class WearableType {
    SMARTWATCH_WEAR_OS,
    SMART_BAND,
    SAFETY_SMART_RING,
    BLUETOOTH_SOS_BUTTON
}
