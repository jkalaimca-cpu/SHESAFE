package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EmergencyEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyEventDao {
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EmergencyEventEntity>>

    @Query("SELECT * FROM emergency_events WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveEvent(): Flow<EmergencyEventEntity?>

    @Query("SELECT * FROM emergency_events WHERE id = :id")
    suspend fun getEventById(id: Long): EmergencyEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EmergencyEventEntity): Long

    @Update
    suspend fun updateEvent(event: EmergencyEventEntity)

    @Query("UPDATE emergency_events SET status = :status, durationSeconds = :duration, resolutionNotes = :notes WHERE id = :id")
    suspend fun resolveEvent(id: Long, status: String, duration: Long, notes: String)

    @Query("DELETE FROM emergency_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM emergency_events")
    suspend fun deleteAll()
}
