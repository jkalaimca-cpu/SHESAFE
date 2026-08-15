package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.CrimsonPrimaryLight

@Composable
fun SosEmergencyButton(
    modifier: Modifier = Modifier,
    size: Dp = 192.dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit = onClick
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 1.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1_scale"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1_alpha"
    )

    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2_scale"
    )
    val rippleAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size * 1.45f)
            .testTag("sos_button_container")
    ) {
        // Outer concentric halo 1
        Box(
            modifier = Modifier
                .size(size)
                .scale(rippleScale1)
                .clip(CircleShape)
                .background(CrimsonPrimary.copy(alpha = rippleAlpha1))
        )

        // Mid concentric halo 2
        Box(
            modifier = Modifier
                .size(size)
                .scale(rippleScale2)
                .clip(CircleShape)
                .background(CrimsonPrimary.copy(alpha = rippleAlpha2))
        )

        // Main Core High Density SOS Button
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(28.dp, shape = CircleShape, spotColor = CrimsonPrimary, ambientColor = CrimsonPrimaryDark)
                .clip(CircleShape)
                .border(8.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .testTag("sos_main_button"),
            shape = CircleShape,
            color = CrimsonPrimary
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CrimsonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SOS",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TAP & HOLD",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
