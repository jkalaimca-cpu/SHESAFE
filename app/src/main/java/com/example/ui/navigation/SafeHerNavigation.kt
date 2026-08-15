package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.EmergencyState
import com.example.ui.components.ActiveEmergencyScreen
import com.example.ui.components.FakeCallSimulationDialog
import com.example.ui.components.SosCountdownOverlay
import com.example.ui.screens.contacts.ContactsScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.location.LiveLocationScreen
import com.example.ui.screens.profile.ProfileSetupScreen
import com.example.ui.screens.settings.SettingsPrivacyScreen
import com.example.ui.screens.threat.AiThreatScreen
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.viewmodel.SafetyViewModel

sealed class SafeHerScreen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : SafeHerScreen("home", "SOS", Icons.Filled.Shield, Icons.Outlined.Home)
    object AiThreat : SafeHerScreen("ai_threat", "AI Radar", Icons.Filled.Radar, Icons.Outlined.Radar)
    object Location : SafeHerScreen("location", "Location", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
    object Contacts : SafeHerScreen("contacts", "Contacts", Icons.Filled.Contacts, Icons.Outlined.Contacts)
    object History : SafeHerScreen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : SafeHerScreen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Onboarding : SafeHerScreen("onboarding", "Setup", Icons.Filled.Security, Icons.Filled.Security)
}

@Composable
fun SafeHerMainApp(viewModel: SafetyViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SafeHerScreen.Home.route

    val emergencyState by viewModel.emergencyState.collectAsState()
    val fakeCallState by viewModel.fakeCallState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val userMessage by viewModel.userFeedbackMessage.collectAsState()
    val contacts by viewModel.emergencyContacts.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Multi-Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle result
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    val bottomNavItems = listOf(
        SafeHerScreen.Home,
        SafeHerScreen.AiThreat,
        SafeHerScreen.Location,
        SafeHerScreen.Contacts,
        SafeHerScreen.History,
        SafeHerScreen.Settings
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute != SafeHerScreen.Onboarding.route && emergencyState !is EmergencyState.Active) {
                NavigationBar(
                    containerColor = HighDensitySurface,
                    contentColor = HighDensityTextPrimary,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) HighDensityPrimary else HighDensityTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HighDensityPrimary,
                                unselectedIconColor = HighDensityTextSecondary,
                                selectedTextColor = HighDensityPrimary,
                                unselectedTextColor = HighDensityTextSecondary,
                                indicatorColor = HighDensityPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        containerColor = HighDensityCanvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = SafeHerScreen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(SafeHerScreen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToAiThreat = { navController.navigate(SafeHerScreen.AiThreat.route) },
                        onNavigateToLocation = { navController.navigate(SafeHerScreen.Location.route) },
                        onNavigateToContacts = { navController.navigate(SafeHerScreen.Contacts.route) },
                        onNavigateToHistory = { navController.navigate(SafeHerScreen.History.route) },
                        onNavigateToSettings = { navController.navigate(SafeHerScreen.Settings.route) }
                    )
                }

                composable(SafeHerScreen.AiThreat.route) {
                    AiThreatScreen(viewModel = viewModel)
                }

                composable(SafeHerScreen.Location.route) {
                    LiveLocationScreen(viewModel = viewModel)
                }

                composable(SafeHerScreen.Contacts.route) {
                    ContactsScreen(viewModel = viewModel)
                }

                composable(SafeHerScreen.History.route) {
                    HistoryScreen(viewModel = viewModel)
                }

                composable(SafeHerScreen.Settings.route) {
                    SettingsPrivacyScreen(viewModel = viewModel)
                }

                composable(SafeHerScreen.Onboarding.route) {
                    ProfileSetupScreen(
                        viewModel = viewModel,
                        onSetupComplete = {
                            navController.navigate(SafeHerScreen.Home.route) {
                                popUpTo(SafeHerScreen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Overlay 1: SOS Countdown Buffer
            val currentEmergState = emergencyState
            if (currentEmergState is EmergencyState.Countdown) {
                SosCountdownOverlay(
                    remainingSeconds = currentEmergState.remainingSeconds,
                    totalSeconds = currentEmergState.totalSeconds,
                    triggerReason = currentEmergState.triggerReason,
                    onCancel = { viewModel.cancelSosCountdown() },
                    onSendNow = { viewModel.triggerInstantSos("Manual SOS countdown override") }
                )
            }

            // Overlay 2: Active Emergency Mode Fullscreen
            if (currentEmergState is EmergencyState.Active) {
                val primaryPhone = contacts.firstOrNull { it.isPrimary }?.phoneNumber ?: "112"
                val expectedPin = userProfile?.emergencyPin ?: "1234"
                ActiveEmergencyScreen(
                    state = currentEmergState,
                    primaryPhone = primaryPhone,
                    expectedPin = expectedPin,
                    onToggleSiren = { viewModel.toggleEmergencySiren() },
                    onResolveEmergency = { pin -> viewModel.resolveActiveEmergency(pin) }
                )
            }

            // Overlay 3: Fake Call Discreet Exit Simulation
            if (fakeCallState.isActive) {
                FakeCallSimulationDialog(
                    state = fakeCallState,
                    onAnswer = { viewModel.answerFakeCall() },
                    onDecline = { viewModel.dismissFakeCall() }
                )
            }
        }
    }
}
