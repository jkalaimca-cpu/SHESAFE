package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.EmergencyEventDao
import com.example.data.local.dao.SafetyCheckInDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.EmergencyEventEntity
import com.example.data.local.entity.SafetyCheckInEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        EmergencyContactEntity::class,
        EmergencyEventEntity::class,
        SafetyCheckInEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun emergencyEventDao(): EmergencyEventDao
    abstract fun safetyCheckInDao(): SafetyCheckInDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safeher_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userProfileDao = database.userProfileDao()
            val contactDao = database.emergencyContactDao()
            val eventDao = database.emergencyEventDao()
            val checkInDao = database.safetyCheckInDao()

            // 1. Initial User Profile
            userProfileDao.insertOrUpdate(
                UserProfileEntity(
                    id = 1,
                    fullName = "Elena Rostova",
                    phoneNumber = "+1 (555) 019-2834",
                    email = "elena.safety@example.com",
                    bloodGroup = "A+",
                    medicalNotes = "No critical medical allergies. Contact sister first.",
                    emergencyPin = "2580",
                    distressKeyword = "HELP",
                    countdownDurationSeconds = 5,
                    isSilentSosEnabled = false,
                    isVoiceDistressEnabled = true,
                    isShakeDetectionEnabled = true,
                    isAutoSirenEnabled = true,
                    isCloudSyncEnabled = true,
                    isRegistered = true
                )
            )

            // 2. Initial Trusted Contacts
            contactDao.insertAll(
                listOf(
                    EmergencyContactEntity(
                        name = "Maya Rostova (Sister)",
                        phoneNumber = "+1 (555) 432-8910",
                        relationship = "Sister",
                        isPrimary = true,
                        canReceiveSms = true,
                        canReceiveLiveGps = true,
                        avatarColorHex = "#E11D48"
                    ),
                    EmergencyContactEntity(
                        name = "David Chen (Partner)",
                        phoneNumber = "+1 (555) 789-0123",
                        relationship = "Partner",
                        isPrimary = false,
                        canReceiveSms = true,
                        canReceiveLiveGps = true,
                        avatarColorHex = "#6366F1"
                    ),
                    EmergencyContactEntity(
                        name = "Aria Martinez (Friend)",
                        phoneNumber = "+1 (555) 654-3210",
                        relationship = "Friend",
                        isPrimary = false,
                        canReceiveSms = true,
                        canReceiveLiveGps = true,
                        avatarColorHex = "#0EA5E9"
                    )
                )
            )

            // 3. Initial Demo Safety Check-in
            val futureTime = System.currentTimeMillis() + (45 * 60 * 1000) // 45 min from now
            checkInDao.insertCheckIn(
                SafetyCheckInEntity(
                    title = "Late Night Commute (Metro Line 4)",
                    targetTimeMillis = futureTime,
                    destinationAddress = "Downtown Central Station",
                    isCompleted = false,
                    autoTriggerSosOnOverdue = true
                )
            )

            // 4. Sample Historic Safety Drill / Log
            val pastTime = System.currentTimeMillis() - (86400000L * 2) // 2 days ago
            eventDao.insertEvent(
                EmergencyEventEntity(
                    timestamp = pastTime,
                    eventType = "MANUAL_SOS",
                    status = "RESOLVED",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    address = "Market St & 4th St, San Francisco, CA",
                    riskLevel = "HIGH",
                    aiConfidencePercent = 94,
                    contactsAlertedCount = 3,
                    isSirenTriggered = true,
                    trackingSessionUrl = "https://safeher.live/track?session=demo-safe-892",
                    cancellationPinUsed = true,
                    durationSeconds = 142,
                    resolutionNotes = "Emergency safety check completed. User confirmed safe via PIN."
                )
            )
        }
    }
}
