package com.example.ui.screens.settings

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPrivacyScreen(
    viewModel: SafetyViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val isSyncing by viewModel.isSyncingCloud.collectAsState()
    val cloudSyncState by viewModel.cloudSyncState.collectAsState()
    val cadDispatchResult by viewModel.cadDispatchResult.collectAsState()
    val wearables by viewModel.connectedWearables.collectAsState()

    var showEditProfileSheet by remember { mutableStateOf(false) }

    // Profile inputs
    var nameInput by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var phoneInput by remember(profile) { mutableStateOf(profile?.phoneNumber ?: "") }
    var emailInput by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var bloodGroupInput by remember(profile) { mutableStateOf(profile?.bloodGroup ?: "O+") }
    var medicalNotesInput by remember(profile) { mutableStateOf(profile?.medicalNotes ?: "") }
    var pinInput by remember(profile) { mutableStateOf(profile?.emergencyPin ?: "2580") }
    var keywordInput by remember(profile) { mutableStateOf(profile?.distressKeyword ?: "HELP") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HighDensityCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen_root")
    ) {
        // Top Header
        Column {
            Text(
                text = "SECURITY & PREFERENCES",
                color = HighDensityPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Profile & Safe Settings",
                color = HighDensityTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Card
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
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = profile?.fullName ?: "Elena Rostova",
                                color = HighDensityTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile?.phoneNumber ?: "+1 (555) 234-5678"} • Blood: ${profile?.bloodGroup ?: "O+"}",
                                color = HighDensityTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showEditProfileSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimaryContainer)
                    ) {
                        Text("Edit", color = HighDensityPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SOS Behavior & Preferences
        Text(
            text = "EMERGENCY SOS CONFIGURATION",
            color = HighDensityTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Countdown duration selector
                Text(
                    text = "SOS Countdown Buffer:",
                    color = HighDensityTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 3, 5, 10).forEach { sec ->
                        val isSelected = profile?.countdownDurationSeconds == sec
                        Button(
                            onClick = {
                                profile?.let { viewModel.updateProfile(it.copy(countdownDurationSeconds = sec)) }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) HighDensityPrimary else HighDensitySurfaceVariant
                            )
                        ) {
                            Text(
                                text = if (sec == 0) "Instant" else "${sec}s",
                                color = if (isSelected) Color.White else HighDensityTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Silent SOS Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Silent SOS Mode", color = HighDensityTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Dispatches SMS & GPS quietly without sounding the audible siren",
                            color = HighDensityTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = profile?.isSilentSosEnabled ?: false,
                        onCheckedChange = { checked ->
                            profile?.let { viewModel.updateProfile(it.copy(isSilentSosEnabled = checked)) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HighDensityPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Siren
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Audible Emergency Siren", color = HighDensityTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Plays loud high-pitch synthesizer alarm during active emergency",
                            color = HighDensityTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = profile?.isAutoSirenEnabled ?: true,
                        onCheckedChange = { checked ->
                            profile?.let { viewModel.updateProfile(it.copy(isAutoSirenEnabled = checked)) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HighDensityPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Encrypted Cloud Synchronization
        Text(
            text = "ZERO-KNOWLEDGE CLOUD BACKUP",
            color = HighDensityTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud",
                            tint = HighDensityPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("AES-GCM-256 Cloud Vault", color = HighDensityTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("End-to-end encrypted backup", color = HighDensityTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.performCloudSync() },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        modifier = Modifier.testTag("sync_cloud_now_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Sync Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (cloudSyncState != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✓ ${cloudSyncState?.message}",
                        color = SafetySafeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wearable Devices Integration Architecture (Stage 9)
        Text(
            text = "WEARABLE DEVICES INTEGRATION ARCHITECTURE",
            color = HighDensityTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Companion Smartwatch & Bluetooth SOS Fobs",
                    color = HighDensityTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Allows one-click hardware SOS triggering directly from paired Wear OS smartwatches or BLE discreet key fobs.",
                    color = HighDensityTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                wearables.forEach { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = device.name,
                                tint = if (device.isConnected) SafetySafeGreen else HighDensityTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(device.name, color = HighDensityTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Battery: ${device.batteryPercent}% • ${if (device.isConnected) "Connected" else "Disconnected"}", color = HighDensityTextSecondary, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.simulateWearableSos(device.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensitySurfaceVariant)
                        ) {
                            Text("Simulate SOS", color = HighDensityTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Official Emergency Services Integration Prototype (Stage 10)
        Text(
            text = "GOVERNMENT / POLICE CAD INTEGRATION SPEC",
            color = HighDensityTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HighDensityBorder.copy(alpha = 0.6f)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPolice,
                        contentDescription = "Police CAD",
                        tint = CrimsonPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official PSAP / CAD 911/112 Bridge",
                        color = HighDensityTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Prototype architecture for NextGen911 / NENA CAD direct dispatch. Live police dispatch is not triggered automatically without authorized credentials.",
                    color = HighDensityTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.testOfficialCadDispatch() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensitySurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Transmit Test CAD Payload", color = HighDensityTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (cadDispatchResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cadDispatchResult ?: "",
                        color = HighDensityPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Purge All Data
        Button(
            onClick = { viewModel.purgeUserData() },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Purge", tint = CrimsonPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Purge All Local & Cloud Data", color = CrimsonPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Edit Profile Modal Sheet
    if (showEditProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditProfileSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = HighDensityCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Edit User Safety Profile",
                    color = HighDensityTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedLabelColor = HighDensityPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Mobile Phone Number") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedLabelColor = HighDensityPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bloodGroupInput,
                    onValueChange = { bloodGroupInput = it },
                    label = { Text("Blood Group (e.g. O+, A-, B+)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedLabelColor = HighDensityPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = medicalNotesInput,
                    onValueChange = { medicalNotesInput = it },
                    label = { Text("Critical Medical Notes (Allergies, Asthma, etc.)") },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedLabelColor = HighDensityPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("4-Digit SOS Cancellation PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedLabelColor = HighDensityPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    label = { Text("Custom Voice Distress Keyword (e.g. HELP)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedLabelColor = HighDensityPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val current = profile ?: UserProfileEntity()
                        viewModel.updateProfile(
                            current.copy(
                                fullName = nameInput.trim(),
                                phoneNumber = phoneInput.trim(),
                                email = emailInput.trim(),
                                bloodGroup = bloodGroupInput.trim(),
                                medicalNotes = medicalNotesInput.trim(),
                                emergencyPin = if (pinInput.isNotBlank()) pinInput else "2580",
                                distressKeyword = if (keywordInput.isNotBlank()) keywordInput.uppercase() else "HELP"
                            )
                        )
                        showEditProfileSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                ) {
                    Text("Save Profile Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
