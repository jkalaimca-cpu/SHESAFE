package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Sarah Jenkins",
    val phoneNumber: String = "+1 (555) 234-5678",
    val email: String = "sarah.j@example.com",
    val bloodGroup: String = "O+",
    val medicalNotes: String = "Asthmatic, carries inhaler. No known drug allergies.",
    val emergencyPin: String = "1234",
    val distressKeyword: String = "HELP",
    val countdownDurationSeconds: Int = 5,
    val isSilentSosEnabled: Boolean = false,
    val isVoiceDistressEnabled: Boolean = false,
    val isShakeDetectionEnabled: Boolean = false,
    val isAutoSirenEnabled: Boolean = true,
    val isCloudSyncEnabled: Boolean = true,
    val isRegistered: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
