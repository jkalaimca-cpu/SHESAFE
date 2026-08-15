package com.example.ui.screens.location

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SafeZone
import com.example.domain.model.SafeZoneType
import com.example.ui.components.LiveGpsRadarView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationScreen(
    viewModel: SafetyViewModel
) {
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val safeZones by viewModel.nearbySafeZones.collectAsState()
    val checkIns by viewModel.safetyCheckIns.collectAsState()

    var showCheckInSheet by remember { mutableStateOf(false) }
    var checkInTitle by remember { mutableStateOf("Heading home via Metro") }
    var checkInMinutes by remember { mutableStateOf(20) }
    var checkInDestination by remember { mutableStateOf("124 Pine St Apt 4B") }

    val liveTrackingLink = remember(currentLocation) {
        viewModel.locationTracker.generateLiveShareUrl(currentLocation.latitude, currentLocation.longitude)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HighDensityCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("location_screen_root")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GPS RADAR & SAFE ZONES",
                    color = HighDensityPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Live Location Sharing",
                    color = HighDensityTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(
                onClick = { showCheckInSheet = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HighDensityPrimaryContainer)
                    .testTag("open_checkin_timer_button")
            ) {
                Icon(imageVector = Icons.Default.Timer, contentDescription = "Safety Timer", tint = HighDensityPrimary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tactical Live Radar Canvas View
        LiveGpsRadarView(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .testTag("tactical_gps_radar"),
            safeZones = safeZones,
            isEmergencyActive = false
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Current Location Details Card
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CURRENT STREET ADDRESS",
                            color = HighDensityPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Accuracy: ±${currentLocation.accuracyMeters.toInt()}m",
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentLocation.address,
                    color = HighDensityTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Coordinates: ${"%.5f".format(currentLocation.latitude)}, ${"%.5f".format(currentLocation.longitude)}",
                    color = HighDensityTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons for Live Sharing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy link
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Live Tracking Link", liveTrackingLink))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("copy_live_link_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = HighDensityPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link", color = HighDensityPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share link
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Track my live safety location here: $liveTrackingLink")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Live GPS Location"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_live_link_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Live", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Verified 24/7 Nearby Safe Zones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEARBY 24/7 SAFE ZONES (${safeZones.size})",
                color = HighDensityTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Police & Medical",
                color = HighDensityPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        safeZones.forEach { zone ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensityCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconColor = when (zone.type) {
                            SafeZoneType.POLICE_STATION -> HighDensityPrimary
                            SafeZoneType.HOSPITAL_24X7 -> CrimsonPrimary
                            SafeZoneType.METRO_SECURITY_BOOTH -> SafetyWarningAmber
                            SafeZoneType.WOMEN_HELP_DESK -> SafetySafeGreen
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (zone.type) {
                                    SafeZoneType.POLICE_STATION -> Icons.Default.LocalPolice
                                    SafeZoneType.HOSPITAL_24X7 -> Icons.Default.LocationOn
                                    else -> Icons.Default.NearMe
                                },
                                contentDescription = zone.name,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = zone.name,
                                color = HighDensityTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${zone.distanceMeters}m away • ${zone.openStatus}",
                                color = HighDensityTextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = zone.address,
                                color = HighDensityTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row {
                        // Call
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${zone.contactPhone}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call Safe Zone", tint = SafetySafeGreen)
                        }

                        // Navigate
                        IconButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("geo:0,0?q=${zone.latitude},${zone.longitude}(${zone.name})")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(mapIntent)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Directions, contentDescription = "Directions", tint = HighDensityPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Safety Check-In Sheet
    if (showCheckInSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCheckInSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = HighDensitySurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Set Safety Check-In Timer",
                    color = HighDensityTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "If you don't check in before the timer expires, SafeHer will automatically trigger an emergency SOS alert to your contacts.",
                    color = HighDensityTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = checkInTitle,
                    onValueChange = { checkInTitle = it },
                    label = { Text("Trip / Activity Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = checkInDestination,
                    onValueChange = { checkInDestination = it },
                    label = { Text("Destination Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Duration:",
                    color = HighDensityTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        Button(
                            onClick = { checkInMinutes = mins },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkInMinutes == mins) HighDensityPrimary else HighDensitySurfaceVariant,
                                contentColor = if (checkInMinutes == mins) Color.White else HighDensityTextPrimary
                            )
                        ) {
                            Text("${mins}m", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.createCheckIn(checkInTitle, checkInMinutes, checkInDestination)
                        showCheckInSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_checkin_timer_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                ) {
                    Text("Start $checkInMinutes Min Safety Watch", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
