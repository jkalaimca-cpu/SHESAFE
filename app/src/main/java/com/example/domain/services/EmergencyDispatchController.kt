package com.example.domain.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.EmergencyEventEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.SafetyRepository
import com.example.domain.model.EmergencyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class EmergencyDispatchController(
    private val context: Context,
    private val repository: SafetyRepository,
    private val locationTracker: SafeLocationTracker,
    private val scope: CoroutineScope
) {
    private val tag = "EmergencyController"
    private val notificationChannelId = "safeher_emergency_alerts_channel"

    private val _emergencyState = MutableStateFlow<EmergencyState>(EmergencyState.Idle)
    val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    private var countdownJob: Job? = null
    private var sirenJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var isSirenMuted: Boolean = false

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    init {
        createNotificationChannel()
    }

    fun initiateEmergencyWorkflow(
        triggerReason: String,
        skipCountdown: Boolean = false,
        isVoiceTriggered: Boolean = false
    ) {
        if (_emergencyState.value is EmergencyState.Active) {
            Log.w(tag, "Emergency already actively running")
            return
        }

        countdownJob?.cancel()

        scope.launch(Dispatchers.Main) {
            val profile = repository.getUserProfileOnce() ?: UserProfileEntity()
            val countdownSec = if (skipCountdown) 0 else profile.countdownDurationSeconds

            if (countdownSec > 0) {
                _emergencyState.value = EmergencyState.Countdown(
                    remainingSeconds = countdownSec,
                    totalSeconds = countdownSec,
                    triggerReason = triggerReason
                )

                countdownJob = scope.launch(Dispatchers.Default) {
                    for (i in countdownSec downTo 1) {
                        if (!isActive) break
                        _emergencyState.value = EmergencyState.Countdown(
                            remainingSeconds = i,
                            totalSeconds = countdownSec,
                            triggerReason = triggerReason
                        )
                        vibrateBriefly(100)
                        delay(1000)
                    }
                    if (isActive) {
                        dispatchActiveSos(triggerReason, isVoiceTriggered, profile)
                    }
                }
            } else {
                dispatchActiveSos(triggerReason, isVoiceTriggered, profile)
            }
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _emergencyState.value = EmergencyState.Idle
        vibrateBriefly(50)
    }

    private suspend fun dispatchActiveSos(
        triggerReason: String,
        isVoiceTriggered: Boolean,
        profile: UserProfileEntity
    ) {
        val location = locationTracker.getCurrentLocation()
        val contacts = repository.getAllContactsOnce()
        val trackingUrl = locationTracker.generateLiveShareUrl(location.latitude, location.longitude)
        val eventType = if (isVoiceTriggered) "AI_VOICE_DISTRESS" else if (triggerReason.contains("Shake", ignoreCase = true)) "SHAKE_ABNORMAL_MOVEMENT" else "MANUAL_SOS"

        // 1. Record in DB
        val event = EmergencyEventEntity(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            status = "ACTIVE",
            latitude = location.latitude,
            longitude = location.longitude,
            address = location.address,
            riskLevel = "CRITICAL",
            aiConfidencePercent = if (isVoiceTriggered) 95 else 99,
            contactsAlertedCount = contacts.size,
            isSirenTriggered = profile.isAutoSirenEnabled && !profile.isSilentSosEnabled,
            trackingSessionUrl = trackingUrl,
            cancellationPinUsed = false,
            durationSeconds = 0,
            resolutionNotes = "Active SOS initiated. Reason: $triggerReason"
        )
        val eventId = repository.recordEmergencyEvent(event)

        // 2. Dispatch SMS and Alerts
        dispatchSmsToContacts(profile, contacts, location, trackingUrl)

        // 3. Post High-Priority Notification
        showEmergencyNotification(profile.fullName, location.address, trackingUrl)

        // 4. Start Siren & Vibration unless silent SOS is enabled
        if (profile.isAutoSirenEnabled && !profile.isSilentSosEnabled) {
            startEmergencySiren()
        }
        startSosVibrationPattern()

        // 5. Update State
        _emergencyState.value = EmergencyState.Active(
            eventId = eventId,
            triggerReason = triggerReason,
            startTimeMillis = System.currentTimeMillis(),
            latitude = location.latitude,
            longitude = location.longitude,
            address = location.address,
            contactsAlerted = contacts.size,
            sirenPlaying = profile.isAutoSirenEnabled && !profile.isSilentSosEnabled,
            trackingUrl = trackingUrl
        )
    }

    private fun dispatchSmsToContacts(
        profile: UserProfileEntity,
        contacts: List<EmergencyContactEntity>,
        location: LocationData,
        trackingUrl: String
    ) {
        val message = "🚨 EMERGENCY SOS from ${profile.fullName}!\n" +
                "I need urgent help! My current location:\n" +
                "${location.address}\n" +
                "Live Tracking: $trackingUrl\n" +
                "Google Maps: https://maps.google.com/?q=${location.latitude},${location.longitude}\n" +
                "Sent via SafeHer Women Safety System."

        for (contact in contacts) {
            if (contact.canReceiveSms && contact.phoneNumber.isNotBlank()) {
                try {
                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null)
                    Log.i(tag, "SMS successfully dispatched to ${contact.name} (${contact.phoneNumber})")
                } catch (e: Exception) {
                    Log.e(tag, "Failed to send background SMS to ${contact.phoneNumber}: ${e.message}")
                }
            }
        }
    }

    fun toggleSiren() {
        val current = _emergencyState.value
        if (current is EmergencyState.Active) {
            if (current.sirenPlaying) {
                stopSiren()
                _emergencyState.value = current.copy(sirenPlaying = false)
            } else {
                startEmergencySiren()
                _emergencyState.value = current.copy(sirenPlaying = true)
            }
        }
    }

    fun resolveEmergency(enteredPin: String, notes: String = "Resolved by user safely"): Boolean {
        val current = _emergencyState.value
        if (current !is EmergencyState.Active) return false

        stopSiren()
        try {
            vibrator.cancel()
        } catch (e: Exception) {
            Log.w(tag, "Error cancelling vibrator: ${e.message}")
        }
        dismissEmergencyNotification()
        val durationSeconds = ((System.currentTimeMillis() - current.startTimeMillis) / 1000).coerceAtLeast(1)

        _emergencyState.value = EmergencyState.Idle

        scope.launch(Dispatchers.IO) {
            try {
                repository.resolveEmergencyEvent(
                    eventId = current.eventId,
                    status = "RESOLVED",
                    durationSeconds = durationSeconds,
                    notes = notes
                )
            } catch (e: Exception) {
                Log.e(tag, "Error persisting emergency resolution: ${e.message}")
            }
        }
        return true
    }

    private fun startEmergencySiren() {
        stopSiren()
        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val buffer = ShortArray(bufferSize)
                var phase = 0.0

                while (isActive) {
                    // Modulate frequency between 700Hz and 1400Hz for high-pitch safety alarm
                    for (f in 700..1400 step 15) {
                        if (!isActive) break
                        for (i in buffer.indices) {
                            phase += 2.0 * Math.PI * f / sampleRate
                            buffer[i] = (sin(phase) * 30000).toInt().toShort()
                        }
                        audioTrack?.write(buffer, 0, buffer.size)
                    }
                    for (f in 1400 downTo 700 step 15) {
                        if (!isActive) break
                        for (i in buffer.indices) {
                            phase += 2.0 * Math.PI * f / sampleRate
                            buffer[i] = (sin(phase) * 30000).toInt().toShort()
                        }
                        audioTrack?.write(buffer, 0, buffer.size)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error playing siren audio synthesizer", e)
            }
        }
    }

    private fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.w(tag, "Error releasing audio track", e)
        }
        audioTrack = null
    }

    private fun startSosVibrationPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // SOS in Morse code: ... --- ...
            val timings = longArrayOf(0, 200, 100, 200, 100, 200, 300, 500, 100, 500, 100, 500, 300, 200, 100, 200, 100, 200, 800)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 300, 200, 300, 200, 600, 200, 600), 0)
        }
    }

    private fun vibrateBriefly(millis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(millis)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "SafeHer Critical Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority emergency alerts and live GPS tracking status"
                enableVibration(true)
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showEmergencyNotification(userName: String, address: String, trackingUrl: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle("🚨 EMERGENCY MODE ACTIVE - SafeHer")
            .setContentText("Broadcasting live GPS location & alerting trusted contacts.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("EMERGENCY SOS ACTIVE\nLocation: $address\nLive link dispatched. Tap to open emergency controller.")
            )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(999, notification)
    }

    private fun dismissEmergencyNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(999)
    }
}
