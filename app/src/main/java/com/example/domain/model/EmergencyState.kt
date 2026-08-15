package com.example.domain.model

sealed class EmergencyState {
    object Idle : EmergencyState()
    
    data class Countdown(
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val triggerReason: String,
        val initiatedTimestamp: Long = System.currentTimeMillis()
    ) : EmergencyState()
    
    data class Active(
        val eventId: Long,
        val triggerReason: String,
        val startTimeMillis: Long,
        val latitude: Double,
        val longitude: Double,
        val address: String,
        val contactsAlerted: Int,
        val sirenPlaying: Boolean,
        val trackingUrl: String
    ) : EmergencyState()
    
    data class Resolved(
        val eventId: Long,
        val durationSeconds: Long,
        val resolutionTimestamp: Long = System.currentTimeMillis()
    ) : EmergencyState()
}
