package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_events")
data class EmergencyEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // MANUAL_SOS, AI_VOICE_DISTRESS, SHAKE_ABNORMAL_MOVEMENT, PIN_DURESS, TIMEOUT_CHECKIN
    val status: String, // ACTIVE, RESOLVED, CANCELLED, FALSE_ALARM
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val riskLevel: String, // LOW, MEDIUM, HIGH, CRITICAL
    val aiConfidencePercent: Int,
    val contactsAlertedCount: Int,
    val isSirenTriggered: Boolean,
    val trackingSessionUrl: String,
    val cancellationPinUsed: Boolean = false,
    val durationSeconds: Long = 0,
    val resolutionNotes: String = ""
)
