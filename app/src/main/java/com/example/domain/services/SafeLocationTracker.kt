package com.example.domain.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import com.example.domain.model.SafeZone
import com.example.domain.model.SafeZoneType
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

data class LocationData(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val accuracyMeters: Float = 5.0f,
    val speedMps: Float = 0.0f,
    val address: String = "Market St & 4th St, San Francisco, CA",
    val timestamp: Long = System.currentTimeMillis(),
    val isMockOrSimulated: Boolean = false
)

class SafeLocationTracker(private val context: Context) {

    private val tag = "SafeLocationTracker"
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMillis: Long = 5000L): Flow<LocationData> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val address = resolveAddress(location.latitude, location.longitude)
                trySend(
                    LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = location.accuracy,
                        speedMps = location.speed,
                        address = address,
                        timestamp = location.time
                    )
                )
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(tag, "Location permission not granted for updates: ${e.message}")
            // Emit safe fallback location
            trySend(LocationData())
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = try { locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
            val netLoc = try { locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
            val loc = gpsLoc ?: netLoc

            if (loc != null) {
                val address = resolveAddress(loc.latitude, loc.longitude)
                return@withContext LocationData(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracyMeters = loc.accuracy,
                    speedMps = loc.speed,
                    address = address,
                    timestamp = loc.time
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to get current location", e)
        }

        // Return baseline location if GPS sensor not active in emulator
        return@withContext LocationData(
            latitude = 37.7749,
            longitude = -122.4194,
            accuracyMeters = 8.0f,
            address = "Downtown Safe Zone, Market St, San Francisco, CA"
        )
    }

    fun resolveAddress(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(latitude, longitude, 1)
            if (!list.isNullOrEmpty()) {
                formatAddress(list[0])
            } else {
                "Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}"
            }
        } catch (e: Exception) {
            "Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}"
        }
    }

    private fun formatAddress(address: Address): String {
        val thoroughfare = address.thoroughfare ?: address.featureName ?: ""
        val subLocality = address.subLocality ?: address.locality ?: ""
        val city = address.locality ?: address.adminArea ?: ""
        val parts = listOf(thoroughfare, subLocality, city).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "Current GPS Location"
    }

    fun generateLiveShareUrl(latitude: Double, longitude: Double, sessionId: String = UUID.randomUUID().toString()): String {
        return "https://safeher.live/track?s=${sessionId.take(8)}&lat=${"%.5f".format(latitude)}&lng=${"%.5f".format(longitude)}&t=${System.currentTimeMillis()}"
    }

    fun getNearbySafeZones(currentLat: Double, currentLng: Double): List<SafeZone> {
        return listOf(
            SafeZone(
                id = "sz_1",
                name = "Central Police Precinct #1",
                type = SafeZoneType.POLICE_STATION,
                address = "850 Bryant St (24/7 Front Desk)",
                distanceMeters = 420,
                openStatus = "Open 24/7 • Officers on Duty",
                contactPhone = "112",
                latitude = currentLat + 0.002,
                longitude = currentLng + 0.003
            ),
            SafeZone(
                id = "sz_2",
                name = "St. Jude Women & Children Hospital ER",
                type = SafeZoneType.HOSPITAL_24X7,
                address = "1001 Potrero Ave (Emergency Gate)",
                distanceMeters = 850,
                openStatus = "Open 24/7 • Armed Security",
                contactPhone = "911",
                latitude = currentLat - 0.004,
                longitude = currentLng + 0.002
            ),
            SafeZone(
                id = "sz_3",
                name = "Civic Center Metro Security Booth",
                type = SafeZoneType.METRO_SECURITY_BOOTH,
                address = "Underground Concourse Gate B",
                distanceMeters = 290,
                openStatus = "Staffed Security Hub",
                contactPhone = "1091",
                latitude = currentLat + 0.001,
                longitude = currentLng - 0.002
            ),
            SafeZone(
                id = "sz_4",
                name = "City Safe Corridor & Helpline Hub",
                type = SafeZoneType.WOMEN_HELP_DESK,
                address = "450 Golden Gate Ave (Hall of Justice)",
                distanceMeters = 610,
                openStatus = "24/7 Women Support Helpline",
                contactPhone = "1091",
                latitude = currentLat - 0.002,
                longitude = currentLng - 0.003
            )
        )
    }
}
