package com.example.ui.screens.threat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RiskLevel
import com.example.ui.components.SafetyRiskBadge
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityCard
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensitySurfaceVariant
import com.example.ui.theme.HighDensityTextMuted
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.SafetySafeGreen
import com.example.ui.theme.SafetyWarningAmber
import com.example.ui.viewmodel.SafetyViewModel

@Composable
fun AiThreatScreen(
    viewModel: SafetyViewModel
) {
    val threatAssessment by viewModel.aiThreatAssessment.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingThreat.collectAsState()
    val voiceState by viewModel.voiceMonitoringState.collectAsState()
    val sensorState by viewModel.sensorMonitoringState.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HighDensityCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("ai_threat_screen_root")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI THREAT DETECTION",
                    color = HighDensityPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Safety Radar & AI Monitor",
                    color = HighDensityTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(
                onClick = { viewModel.triggerAiAnalysis("Manual user threat scan trigger") },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HighDensityPrimaryContainer)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = HighDensityPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Scan", tint = HighDensityPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Primary AI Risk Evaluation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SafetyRiskBadge(
                        riskLevel = threatAssessment.riskLevel,
                        confidencePercent = threatAssessment.confidencePercent
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI",
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (threatAssessment.isEvaluatedByGemini) "Gemini 3.5 Flash" else "Local Fusion Engine",
                            color = HighDensityTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = threatAssessment.summary,
                    color = HighDensityTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Explainable Indicators
                Text(
                    text = "EXPLAINABLE SAFETY INDICATORS",
                    color = HighDensityTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                threatAssessment.explainableSafetyIndicators.forEach { ind ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = ind.name, color = HighDensityTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = ind.statusText, color = HighDensityTextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ind.threatScorePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                ind.threatScorePercent > 70 -> CrimsonPrimary
                                ind.threatScorePercent > 35 -> SafetyWarningAmber
                                else -> SafetySafeGreen
                            },
                            trackColor = HighDensitySurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // False Positive Prevention Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HighDensitySurfaceVariant.copy(alpha = 0.6f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "False alarm safety",
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = threatAssessment.falsePositivePreventionReasoning,
                            color = HighDensityTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Signal 1: Voice Distress Keyword Recognition
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (voiceState.isListening) SafetySafeGreen.copy(alpha = 0.15f) else HighDensitySurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (voiceState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Mic",
                                tint = if (voiceState.isListening) SafetySafeGreen else HighDensityTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Voice Distress Recognition",
                                color = HighDensityTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (voiceState.isListening) "Listening for \"${profile?.distressKeyword ?: "HELP"}\"" else "Disabled by user",
                                color = HighDensityTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = voiceState.isListening,
                        onCheckedChange = { viewModel.toggleVoiceMonitoring(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SafetySafeGreen
                        )
                    )
                }

                if (voiceState.isListening) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Real-time Acoustic Waveform (${voiceState.currentDecibels} dB):",
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    WaveformVisualizer(
                        amplitudes = voiceState.waveformAmplitudes,
                        isDistressTriggered = voiceState.isDistressTriggered
                    )

                    if (voiceState.lastRecognizedText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last Heard: \"${voiceState.lastRecognizedText}\"",
                            color = HighDensityTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Signal 2: Motion Anomaly & Violent Shake Detector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (sensorState.isEnabled) HighDensityPrimaryContainer else HighDensitySurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Accelerometer",
                                tint = if (sensorState.isEnabled) HighDensityPrimary else HighDensityTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Abnormal Motion Detector",
                                color = HighDensityTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (sensorState.isEnabled) "G-Force: ${"%.2f".format(sensorState.currentAccelerationG)} G • Armed" else "Disabled",
                                color = HighDensityTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = sensorState.isEnabled,
                        onCheckedChange = { viewModel.toggleShakeDetection(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HighDensityPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Lab Testing Controls (For QA & Verification)
        Text(
            text = "SAFETY VERIFICATION LAB & SIMULATION",
            color = HighDensityTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Test automatic SOS trigger with spoken or simulated distress keywords:",
            color = HighDensityTextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Distress word quick chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("HELP ME", "EMERGENCY", "SAVE ME").forEach { word ->
                OutlinedButton(
                    onClick = { viewModel.voiceDetector.triggerSimulatedDistressPhrase(word) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(word, color = CrimsonPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("DISTRESS", "CALL POLICE", "BACHAO").forEach { word ->
                OutlinedButton(
                    onClick = { viewModel.voiceDetector.triggerSimulatedDistressPhrase(word) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(word, color = CrimsonPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Test Scream
            OutlinedButton(
                onClick = { viewModel.voiceDetector.triggerSimulatedDistressPhrase("SCREAM") },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Hearing, contentDescription = "Scream", tint = CrimsonPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Scream (88dB)", color = CrimsonPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Test Violent Shake
            OutlinedButton(
                onClick = { viewModel.sensorDetector.simulateShakeAnomaly() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("simulate_shake_anomaly_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed", tint = HighDensityPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Shake Anomaly", color = HighDensityPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Run full threat analysis button
        Button(
            onClick = { viewModel.triggerAiAnalysis("Full multi-sensor threat radar diagnostic") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("run_ai_threat_diagnostic_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run AI Diagnostic with Gemini", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
