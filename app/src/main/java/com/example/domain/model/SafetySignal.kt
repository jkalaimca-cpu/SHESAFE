package com.example.domain.model

data class SafetySignal(
    val id: String,
    val type: SignalType,
    val value: String,
    val confidencePercent: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isAnomaly: Boolean = false,
    val details: String = ""
)

enum class SignalType {
    VOICE_DISTRESS,
    AUDIO_DECIBEL_SPIKE,
    ACCELEROMETER_SHAKE,
    LOCATION_DEVIATION,
    MISSED_CHECKIN,
    MANUAL_TRIGGER,
    WEARABLE_HEART_SPIKE,
    REPEATED_PIN_FAILURE
}
