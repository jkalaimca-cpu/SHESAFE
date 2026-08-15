package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safety_checkins")
data class SafetyCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetTimeMillis: Long,
    val destinationAddress: String = "",
    val isCompleted: Boolean = false,
    val autoTriggerSosOnOverdue: Boolean = true,
    val createdTimeMillis: Long = System.currentTimeMillis()
)
