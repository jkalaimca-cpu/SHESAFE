package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SafetyCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyCheckInDao {
    @Query("SELECT * FROM safety_checkins ORDER BY isCompleted ASC, targetTimeMillis ASC")
    fun getAllCheckIns(): Flow<List<SafetyCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: SafetyCheckInEntity): Long

    @Update
    suspend fun updateCheckIn(checkIn: SafetyCheckInEntity)

    @Query("UPDATE safety_checkins SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Delete
    suspend fun deleteCheckIn(checkIn: SafetyCheckInEntity)

    @Query("DELETE FROM safety_checkins WHERE id = :id")
    suspend fun deleteById(id: Long)
}
