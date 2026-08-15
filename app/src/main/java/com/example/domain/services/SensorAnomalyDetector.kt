package com.example.domain.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

data class SensorMonitoringState(
    val isEnabled: Boolean = false,
    val currentAccelerationG: Float = 1.0f,
    val shakeCount: Int = 0,
    val isAnomalyDetected: Boolean = false,
    val lastAnomalyTimestamp: Long = 0L
)

class SensorAnomalyDetector(private val context: Context) : SensorEventListener {

    private val tag = "SensorAnomalyDetector"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _sensorState = MutableStateFlow(SensorMonitoringState())
    val sensorState: StateFlow<SensorMonitoringState> = _sensorState.asStateFlow()

    private var shakeThresholdG = 2.7f // G-force threshold for violent shake
    private var lastShakeTimestamp: Long = 0
    private var shakeStreak = 0

    var onAbnormalMovementDetected: ((Float, String) -> Unit)? = null

    fun startListening(thresholdG: Float = 2.7f) {
        shakeThresholdG = thresholdG
        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            _sensorState.value = _sensorState.value.copy(isEnabled = true, isAnomalyDetected = false)
        } else {
            Log.w(tag, "Accelerometer not available on this device")
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
        _sensorState.value = _sensorState.value.copy(isEnabled = false, isAnomalyDetected = false)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
        val now = System.currentTimeMillis()

        _sensorState.value = _sensorState.value.copy(currentAccelerationG = gForce)

        if (gForce > shakeThresholdG) {
            if (now - lastShakeTimestamp < 800) {
                shakeStreak++
                if (shakeStreak >= 3) {
                    _sensorState.value = _sensorState.value.copy(
                        isAnomalyDetected = true,
                        shakeCount = shakeStreak,
                        lastAnomalyTimestamp = now
                    )
                    onAbnormalMovementDetected?.invoke(gForce, "Rapid violent shake sequence ($shakeStreak spikes)")
                    shakeStreak = 0
                }
            } else {
                shakeStreak = 1
            }
            lastShakeTimestamp = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun simulateShakeAnomaly() {
        _sensorState.value = _sensorState.value.copy(
            isAnomalyDetected = true,
            shakeCount = 3,
            currentAccelerationG = 3.4f,
            lastAnomalyTimestamp = System.currentTimeMillis()
        )
        onAbnormalMovementDetected?.invoke(3.4f, "Simulated rapid impact/struggle movement")
    }
}
