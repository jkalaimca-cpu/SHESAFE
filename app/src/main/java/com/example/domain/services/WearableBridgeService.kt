package com.example.domain.services

import android.content.Context
import android.util.Log
import com.example.domain.model.WearableDevice
import com.example.domain.model.WearableType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ARCHITECTURAL SPECIFICATION FOR WEARABLE INTEGRATION:
 * Supports Wear OS Companion apps, BLE Smart Rings, Smart Safety Bands & discrete Bluetooth SOS fobs.
 */
class WearableBridgeService(private val context: Context) {

    private val tag = "WearableBridgeService"

    private val _connectedWearables = MutableStateFlow<List<WearableDevice>>(
        listOf(
            WearableDevice(
                id = "wear_1",
                name = "Galaxy Watch / Pixel Watch (Wear OS)",
                deviceType = WearableType.SMARTWATCH_WEAR_OS,
                isConnected = true,
                batteryPercent = 88,
                macAddress = "D4:F5:13:9A:82:11"
            ),
            WearableDevice(
                id = "wear_2",
                name = "Aura Smart Safety Ring",
                deviceType = WearableType.SAFETY_SMART_RING,
                isConnected = false,
                batteryPercent = 64,
                macAddress = "E1:33:AA:99:40:55"
            ),
            WearableDevice(
                id = "wear_3",
                name = "Discreet Keychain SOS Button (BLE)",
                deviceType = WearableType.BLUETOOTH_SOS_BUTTON,
                isConnected = true,
                batteryPercent = 95,
                macAddress = "C8:90:12:34:56:78"
            )
        )
    )
    val connectedWearables: StateFlow<List<WearableDevice>> = _connectedWearables.asStateFlow()

    var onWearableSosTriggered: ((String) -> Unit)? = null

    fun toggleDeviceConnection(deviceId: String) {
        _connectedWearables.value = _connectedWearables.value.map { device ->
            if (device.id == deviceId) {
                device.copy(isConnected = !device.isConnected, lastSyncTimestamp = System.currentTimeMillis())
            } else device
        }
    }

    fun simulateWearableSosPress(deviceId: String) {
        val device = _connectedWearables.value.firstOrNull { it.id == deviceId }
        val name = device?.name ?: "Wearable Device"
        Log.i(tag, "Remote hardware SOS button triggered from $name")
        onWearableSosTriggered?.invoke("Triggered remotely via $name SOS hardware click")
    }
}
