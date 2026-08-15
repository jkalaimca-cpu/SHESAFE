package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val canReceiveSms: Boolean = true,
    val canReceiveLiveGps: Boolean = true,
    val avatarColorHex: String = "#E11D48",
    val addedTimestamp: Long = System.currentTimeMillis()
)
