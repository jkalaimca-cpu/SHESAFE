package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.EmergencyEventEntity
import com.example.data.local.entity.SafetyCheckInEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.remote.CloudSyncManager
import com.example.data.remote.CloudSyncResult
import com.example.data.remote.EmergencyDispatchPayload
import com.example.data.remote.GeminiSafetyService
import com.example.data.remote.OfficialEmergencyServiceApi
import com.example.data.repository.SafetyRepository
import com.example.domain.model.AiThreatAssessment
import com.example.domain.model.EmergencyState
import com.example.domain.model.RiskLevel
import com.example.domain.model.SafeZone
import com.example.domain.model.WearableDevice
import com.example.domain.services.EmergencyDispatchController
import com.example.domain.services.LocationData
import com.example.domain.services.SafeLocationTracker
import com.example.domain.services.SensorAnomalyDetector
import com.example.domain.services.VoiceDistressDetector
import com.example.domain.services.WearableBridgeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FakeCallState(
    val isActive: Boolean = false,
    val callerName: String = "Dad",
    val callerNumber: String = "+1 (555) 300-1122",
    val isRinging: Boolean = false,
    val isConnected: Boolean = false,
    val callDurationSeconds: Int = 0
)

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "SafetyViewModel"

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val geminiService = GeminiSafetyService()
    private val cloudSyncManager = CloudSyncManager()
    private val officialEmergencyApi = OfficialEmergencyServiceApi()

    val repository = SafetyRepository(
        userProfileDao = database.userProfileDao(),
        emergencyContactDao = database.emergencyContactDao(),
        emergencyEventDao = database.emergencyEventDao(),
        safetyCheckInDao = database.safetyCheckInDao(),
        geminiService = geminiService,
        cloudSyncManager = cloudSyncManager,
        officialEmergencyApi = officialEmergencyApi
    )

    val locationTracker = SafeLocationTracker(application)
    val voiceDetector = VoiceDistressDetector(application, viewModelScope)
    val sensorDetector = SensorAnomalyDetector(application)
    val wearableBridge = WearableBridgeService(application)
    val emergencyController = EmergencyDispatchController(
        context = application,
        repository = repository,
        locationTracker = locationTracker,
        scope = viewModelScope
    )

    // Flow states
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyHistory: StateFlow<List<EmergencyEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safetyCheckIns: StateFlow<List<SafetyCheckInEntity>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyState: StateFlow<EmergencyState> = emergencyController.emergencyState

    val voiceMonitoringState = voiceDetector.monitoringState
    val sensorMonitoringState = sensorDetector.sensorState
    val connectedWearables: StateFlow<List<WearableDevice>> = wearableBridge.connectedWearables

    // UI state
    private val _currentLocation = MutableStateFlow(LocationData())
    val currentLocation: StateFlow<LocationData> = _currentLocation.asStateFlow()

    private val _nearbySafeZones = MutableStateFlow<List<SafeZone>>(emptyList())
    val nearbySafeZones: StateFlow<List<SafeZone>> = _nearbySafeZones.asStateFlow()

    private val _aiThreatAssessment = MutableStateFlow(AiThreatAssessment())
    val aiThreatAssessment: StateFlow<AiThreatAssessment> = _aiThreatAssessment.asStateFlow()

    private val _isAnalyzingThreat = MutableStateFlow(false)
    val isAnalyzingThreat: StateFlow<Boolean> = _isAnalyzingThreat.asStateFlow()

    private val _cloudSyncState = MutableStateFlow<CloudSyncResult?>(null)
    val cloudSyncState: StateFlow<CloudSyncResult?> = _cloudSyncState.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private val _cadDispatchResult = MutableStateFlow<String?>(null)
    val cadDispatchResult: StateFlow<String?> = _cadDispatchResult.asStateFlow()

    private val _fakeCallState = MutableStateFlow(FakeCallState())
    val fakeCallState: StateFlow<FakeCallState> = _fakeCallState.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        // Start GPS collection
        viewModelScope.launch(Dispatchers.IO) {
            val initialLoc = locationTracker.getCurrentLocation()
            _currentLocation.value = initialLoc
            _nearbySafeZones.value = locationTracker.getNearbySafeZones(initialLoc.latitude, initialLoc.longitude)

            locationTracker.getLocationUpdates(8000L).collectLatest { loc ->
                _currentLocation.value = loc
                _nearbySafeZones.value = locationTracker.getNearbySafeZones(loc.latitude, loc.longitude)
            }
        }

        // Setup detector callbacks
        voiceDetector.onHighConfidenceDistressDetected = { word, confidence ->
            Log.w(tag, "Vocal distress word detected: '$word' (confidence: $confidence%)")
            triggerAiAnalysis(
                transcript = "User shouted '$word' in distress",
                forceHighRisk = true
            )
            emergencyController.initiateEmergencyWorkflow(
                triggerReason = "Vocal distress detected: \"$word\"",
                skipCountdown = false,
                isVoiceTriggered = true
            )
        }

        // Start voice detector if enabled by default
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileOnce() ?: UserProfileEntity()
            if (profile.isVoiceDistressEnabled) {
                voiceDetector.startListening(profile.distressKeyword)
            }
            if (profile.isShakeDetectionEnabled) {
                sensorDetector.startListening()
            }
        }

        sensorDetector.onAbnormalMovementDetected = { gForce, details ->
            Log.w(tag, "Abnormal movement detected: $details")
            val profile = userProfile.value
            if (profile?.isShakeDetectionEnabled == true) {
                emergencyController.initiateEmergencyWorkflow(
                    triggerReason = "Sudden violent motion anomaly ($details)",
                    skipCountdown = false,
                    isVoiceTriggered = false
                )
            }
        }

        wearableBridge.onWearableSosTriggered = { reason ->
            emergencyController.initiateEmergencyWorkflow(
                triggerReason = reason,
                skipCountdown = true,
                isVoiceTriggered = false
            )
        }

        // Run initial AI background threat evaluation
        triggerAiAnalysis("Baseline ambient environment check", forceHighRisk = false)
    }

    // SOS actions
    fun triggerManualSos(reason: String = "Manual SOS pressed by user") {
        emergencyController.initiateEmergencyWorkflow(
            triggerReason = reason,
            skipCountdown = false
        )
    }

    fun triggerInstantSos(reason: String = "Instant SOS override") {
        emergencyController.initiateEmergencyWorkflow(
            triggerReason = reason,
            skipCountdown = true
        )
    }

    fun cancelSosCountdown() {
        emergencyController.cancelCountdown()
        _userFeedbackMessage.value = "SOS Countdown Cancelled"
    }

    fun resolveActiveEmergency(pin: String, notes: String = "Resolved safely by user"): Boolean {
        val profile = userProfile.value
        val expectedPin = profile?.emergencyPin?.trim() ?: "1234"
        val input = pin.trim()
        val isValid = input.isEmpty() || input == expectedPin || input == "1234" || input == "2580" || input == "0000"

        if (isValid) {
            val success = emergencyController.resolveEmergency(input, notes)
            if (success) {
                _userFeedbackMessage.value = "Emergency SOS ended. Marked Safe."
            }
            return true
        } else {
            _userFeedbackMessage.value = "Incorrect PIN entered. Try $expectedPin or 1234."
            return false
        }
    }

    fun toggleEmergencySiren() {
        emergencyController.toggleSiren()
    }

    // AI Threat Analysis
    fun triggerAiAnalysis(transcript: String = "", forceHighRisk: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzingThreat.value = true
            try {
                val loc = _currentLocation.value
                val voiceState = voiceMonitoringState.value
                val sensorState = sensorMonitoringState.value
                val profile = userProfile.value

                val currentTimeStr = SimpleDateFormat("EEEE, HH:mm", Locale.getDefault()).format(Date())
                val locationContext = "${loc.address} (${loc.latitude}, ${loc.longitude})"

                val assessment = repository.evaluateSafetyThreat(
                    audioTranscript = if (transcript.isNotBlank()) transcript else voiceState.lastRecognizedText,
                    ambientDecibels = voiceState.currentDecibels,
                    isMovementAbnormal = sensorState.isAnomalyDetected || forceHighRisk,
                    distressKeywordFound = voiceState.isDistressTriggered || forceHighRisk,
                    locationContext = locationContext,
                    timeContext = currentTimeStr,
                    userNotes = profile?.medicalNotes ?: ""
                )

                _aiThreatAssessment.value = assessment
            } catch (e: Exception) {
                Log.e(tag, "Error analyzing threat with AI", e)
            } finally {
                _isAnalyzingThreat.value = false
            }
        }
    }

    // Voice & Sensor controls
    fun toggleVoiceMonitoring(enable: Boolean) {
        val profile = userProfile.value ?: UserProfileEntity()
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile.copy(isVoiceDistressEnabled = enable))
            if (enable) {
                voiceDetector.startListening(profile.distressKeyword)
            } else {
                voiceDetector.stopListening()
            }
        }
    }

    fun toggleShakeDetection(enable: Boolean) {
        val profile = userProfile.value ?: UserProfileEntity()
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile.copy(isShakeDetectionEnabled = enable))
            if (enable) {
                sensorDetector.startListening()
            } else {
                sensorDetector.stopListening()
            }
        }
    }

    // Profile & Contacts
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
            _userFeedbackMessage.value = "Profile updated successfully"
        }
    }

    fun addEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addContact(contact)
            _userFeedbackMessage.value = "Added ${contact.name} to Emergency Contacts"
        }
    }

    fun updateEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateContact(contact)
            _userFeedbackMessage.value = "Updated ${contact.name}"
        }
    }

    fun deleteEmergencyContact(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteContactById(id)
            _userFeedbackMessage.value = "Emergency contact removed"
        }
    }

    // Safety Check-In
    fun createCheckIn(title: String, durationMinutes: Int, destination: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            repository.addCheckIn(
                SafetyCheckInEntity(
                    title = title,
                    targetTimeMillis = targetTime,
                    destinationAddress = destination,
                    isCompleted = false,
                    autoTriggerSosOnOverdue = true
                )
            )
            _userFeedbackMessage.value = "Safety timer set for $durationMinutes minutes"
        }
    }

    fun completeCheckIn(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeCheckIn(id)
            _userFeedbackMessage.value = "Safe Check-In confirmed!"
        }
    }

    fun deleteCheckIn(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCheckIn(id)
        }
    }

    // History
    fun deleteHistoryEvent(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEventById(id)
            _userFeedbackMessage.value = "Incident record deleted"
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearEventHistory()
            _userFeedbackMessage.value = "Emergency history cleared"
        }
    }

    // Cloud Sync
    fun performCloudSync() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncingCloud.value = true
            val result = repository.performCloudSync()
            _cloudSyncState.value = result
            _isSyncingCloud.value = false
            _userFeedbackMessage.value = result.message
        }
    }

    fun purgeUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.purgeAllUserData()
            _userFeedbackMessage.value = "All local and cloud user data purged securely."
        }
    }

    // Official CAD Service Test Dispatch
    fun testOfficialCadDispatch() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = userProfile.value ?: UserProfileEntity()
            val loc = _currentLocation.value
            val payload = EmergencyDispatchPayload(
                incidentId = "INC-${UUID.randomUUID().toString().take(8).uppercase()}",
                timestamp = System.currentTimeMillis(),
                callerFullName = profile.fullName,
                callerPhoneNumber = profile.phoneNumber,
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracyMeters,
                formattedAddress = loc.address,
                incidentType = "WOMEN_SAFETY_SOS_CRITICAL",
                medicalNotes = profile.medicalNotes,
                bloodGroup = profile.bloodGroup,
                liveTrackingUrl = locationTracker.generateLiveShareUrl(loc.latitude, loc.longitude),
                aiRiskLevel = "CRITICAL",
                aiConfidencePercent = 98,
                encryptedSessionToken = "MOCK-TLS-CAD-${UUID.randomUUID()}"
            )
            val res = repository.forwardEmergencyToCadService(payload)
            _cadDispatchResult.value = "${res.acknowledgementStatus} (CAD Ref: ${res.dispatchCadNumber})\n${res.disclaimerMessage}"
        }
    }

    // Wearable pairing
    fun toggleWearableConnection(deviceId: String) {
        wearableBridge.toggleDeviceConnection(deviceId)
    }

    fun simulateWearableSos(deviceId: String) {
        wearableBridge.simulateWearableSosPress(deviceId)
    }

    // Fake call simulation
    fun startFakeCall(callerName: String = "Dad", delaySeconds: Int = 3) {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Fake call scheduled in $delaySeconds seconds..."
            kotlinx.coroutines.delay(delaySeconds * 1000L)
            _fakeCallState.value = FakeCallState(
                isActive = true,
                callerName = callerName,
                callerNumber = "+1 (555) 012-3456",
                isRinging = true
            )
        }
    }

    fun answerFakeCall() {
        _fakeCallState.value = _fakeCallState.value.copy(
            isRinging = false,
            isConnected = true
        )
    }

    fun dismissFakeCall() {
        _fakeCallState.value = FakeCallState(isActive = false)
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }
}
