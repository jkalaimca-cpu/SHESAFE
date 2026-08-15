package com.example.data.repository

import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.EmergencyEventDao
import com.example.data.local.dao.SafetyCheckInDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.EmergencyEventEntity
import com.example.data.local.entity.SafetyCheckInEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.remote.CloudSyncManager
import com.example.data.remote.CloudSyncResult
import com.example.data.remote.EmergencyDispatchPayload
import com.example.data.remote.GeminiSafetyService
import com.example.data.remote.OfficialEmergencyServiceApi
import com.example.domain.model.AiThreatAssessment
import kotlinx.coroutines.flow.Flow

class SafetyRepository(
    private val userProfileDao: UserProfileDao,
    private val emergencyContactDao: EmergencyContactDao,
    private val emergencyEventDao: EmergencyEventDao,
    private val safetyCheckInDao: SafetyCheckInDao,
    private val geminiService: GeminiSafetyService,
    private val cloudSyncManager: CloudSyncManager,
    private val officialEmergencyApi: OfficialEmergencyServiceApi
) {
    // User Profile
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    
    suspend fun getUserProfileOnce(): UserProfileEntity? = userProfileDao.getUserProfileOnce()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdate(profile)
    }

    suspend fun setRegistered(registered: Boolean) {
        userProfileDao.setRegistered(registered)
    }

    // Emergency Contacts
    val allContacts: Flow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()

    suspend fun getAllContactsOnce(): List<EmergencyContactEntity> = emergencyContactDao.getAllContactsOnce()

    val primaryContact: Flow<EmergencyContactEntity?> = emergencyContactDao.getPrimaryContact()

    suspend fun addContact(contact: EmergencyContactEntity): Long {
        val id = emergencyContactDao.insertContact(contact)
        if (contact.isPrimary) {
            emergencyContactDao.clearOtherPrimaries(id)
        }
        return id
    }

    suspend fun updateContact(contact: EmergencyContactEntity) {
        emergencyContactDao.updateContact(contact)
        if (contact.isPrimary) {
            emergencyContactDao.clearOtherPrimaries(contact.id)
        }
    }

    suspend fun deleteContact(contact: EmergencyContactEntity) {
        emergencyContactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(id: Long) {
        emergencyContactDao.deleteById(id)
    }

    // Emergency Events
    val allEvents: Flow<List<EmergencyEventEntity>> = emergencyEventDao.getAllEvents()
    val activeEvent: Flow<EmergencyEventEntity?> = emergencyEventDao.getActiveEvent()

    suspend fun recordEmergencyEvent(event: EmergencyEventEntity): Long {
        return emergencyEventDao.insertEvent(event)
    }

    suspend fun updateEmergencyEvent(event: EmergencyEventEntity) {
        emergencyEventDao.updateEvent(event)
    }

    suspend fun resolveEmergencyEvent(eventId: Long, status: String, durationSeconds: Long, notes: String) {
        emergencyEventDao.resolveEvent(eventId, status, durationSeconds, notes)
    }

    suspend fun deleteEventById(id: Long) {
        emergencyEventDao.deleteEventById(id)
    }

    suspend fun clearEventHistory() {
        emergencyEventDao.deleteAll()
    }

    // Safety Check-ins
    val allCheckIns: Flow<List<SafetyCheckInEntity>> = safetyCheckInDao.getAllCheckIns()

    suspend fun addCheckIn(checkIn: SafetyCheckInEntity): Long {
        return safetyCheckInDao.insertCheckIn(checkIn)
    }

    suspend fun completeCheckIn(id: Long) {
        safetyCheckInDao.markCompleted(id)
    }

    suspend fun deleteCheckIn(id: Long) {
        safetyCheckInDao.deleteById(id)
    }

    // AI Threat Assessment
    suspend fun evaluateSafetyThreat(
        audioTranscript: String,
        ambientDecibels: Int,
        isMovementAbnormal: Boolean,
        distressKeywordFound: Boolean,
        locationContext: String,
        timeContext: String,
        userNotes: String
    ): AiThreatAssessment {
        return geminiService.analyzeSafetyThreat(
            audioTranscript = audioTranscript,
            ambientDecibels = ambientDecibels,
            isMovementAbnormal = isMovementAbnormal,
            distressKeywordFound = distressKeywordFound,
            locationContext = locationContext,
            timeContext = timeContext,
            userNotes = userNotes
        )
    }

    // Cloud Sync
    suspend fun performCloudSync(): CloudSyncResult {
        val profile = userProfileDao.getUserProfileOnce()
        val contacts = emergencyContactDao.getAllContactsOnce()
        val events = mutableListOf<EmergencyEventEntity>()
        return cloudSyncManager.syncUserData(profile, contacts, events)
    }

    suspend fun purgeAllUserData() {
        userProfileDao.deleteAll()
        emergencyContactDao.deleteAll()
        emergencyEventDao.deleteAll()
        cloudSyncManager.purgeCloudData()
    }

    // Official Emergency Services CAD Bridge
    suspend fun forwardEmergencyToCadService(payload: EmergencyDispatchPayload) =
        officialEmergencyApi.dispatchToOfficialEmergencyServices(payload)
}
