package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoSecondary

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    isDistressTriggered: Boolean = false
) {
    val barColor = if (isDistressTriggered) CrimsonPrimary else CyanAccent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayList = if (amplitudes.isEmpty()) List(16) { 0.15f } else amplitudes

        displayList.takeLast(16).forEach { amp ->
            val barHeightFactor = amp.coerceIn(0.1f, 1.0f)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(barHeightFactor)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                barColor,
                                if (isDistressTriggered) Color(0xFFFF8FA3) else IndigoSecondary
                            )
                        )
                    )
            )
        }
    }
}
