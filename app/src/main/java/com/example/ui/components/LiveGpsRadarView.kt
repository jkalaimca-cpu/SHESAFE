package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.domain.model.SafeZone
import com.example.domain.model.SafeZoneType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SafetySafeGreen
import com.example.ui.theme.TwilightDarkCanvas
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveGpsRadarView(
    modifier: Modifier = Modifier,
    safeZones: List<SafeZone> = emptyList(),
    isEmergencyActive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val primaryRadarColor = if (isEmergencyActive) Color(0xFFE11D48) else CyanAccent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TwilightDarkCanvas)
            .border(1.dp, primaryRadarColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = (size.minDimension / 2) - 10f

            // Concentric range rings
            val ringCount = 3
            for (i in 1..ringCount) {
                val radius = (maxRadius / ringCount) * i
                drawCircle(
                    color = primaryRadarColor.copy(alpha = 0.18f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Crosshairs
            drawLine(
                color = primaryRadarColor.copy(alpha = 0.2f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = primaryRadarColor.copy(alpha = 0.2f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )

            // Sweeping radar beam line
            val rad = Math.toRadians(sweepAngle.toDouble())
            val sweepEnd = Offset(
                x = center.x + (maxRadius * cos(rad)).toFloat(),
                y = center.y + (maxRadius * sin(rad)).toFloat()
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(primaryRadarColor.copy(alpha = 0.8f), Color.Transparent),
                    start = center,
                    end = sweepEnd
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.5f
            )

            // Safe Zone blips (Police, Hospital, Metro)
            safeZones.forEachIndexed { index, zone ->
                val angle = (index * 85 + 40) * (Math.PI / 180)
                val distFactor = (0.35f + (index * 0.18f)).coerceIn(0.2f, 0.85f)
                val blipOffset = Offset(
                    x = center.x + (maxRadius * distFactor * cos(angle)).toFloat(),
                    y = center.y + (maxRadius * distFactor * sin(angle)).toFloat()
                )

                val blipColor = when (zone.type) {
                    SafeZoneType.POLICE_STATION -> Color(0xFF38BDF8)
                    SafeZoneType.HOSPITAL_24X7 -> Color(0xFFF43F5E)
                    SafeZoneType.METRO_SECURITY_BOOTH -> Color(0xFFFBBF24)
                    SafeZoneType.WOMEN_HELP_DESK -> SafetySafeGreen
                }

                // Outer blip ring
                drawCircle(
                    color = blipColor.copy(alpha = 0.4f),
                    radius = 8f,
                    center = blipOffset
                )
                // Inner solid blip
                drawCircle(
                    color = blipColor,
                    radius = 4f,
                    center = blipOffset
                )
            }

            // User center location beacon
            drawCircle(
                color = primaryRadarColor.copy(alpha = 0.3f),
                radius = 16f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = center
            )
            drawCircle(
                color = primaryRadarColor,
                radius = 4f,
                center = center
            )
        }
    }
}
