package com.example.owner.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.owner.ui.components.OwnerExecutiveHeader
import com.example.owner.ui.components.OwnerThemeColors
import com.example.owner.ui.screens.GlobalAppSettingsScreen
import com.example.owner.ui.screens.SchoolProvisioningScreen
import com.example.owner.ui.screens.RemoteAccessControlScreen
import com.example.ui.viewmodel.SchoolViewModel

enum class OwnerNavDestination(val label: String, val icon: ImageVector) {
    REMOTE_ACCESS("Remote Access", Icons.Rounded.Security),
    PROVISIONING("Provisioning", Icons.Rounded.AddBusiness),
    SETTINGS("Global Settings", Icons.Rounded.Tune)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerAppShell(
    viewModel: SchoolViewModel,
    onSwitchToSchoolApp: () -> Unit
) {
    val context = LocalContext.current
    var isAuthenticated by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var currentDestination by remember { mutableStateOf(OwnerNavDestination.REMOTE_ACCESS) }

    val selectedSchoolConfig by viewModel.selectedOwnerSchoolConfig.collectAsState()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.setFeedbackMessage(null)
        }
    }

    if (!isAuthenticated) {
        // Master Security PIN Authentication Gate
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = OwnerThemeColors.AmberLight)
                            Text("AcademiaTrack Owner Master", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = OwnerThemeColors.SurfaceDark)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = OwnerThemeColors.BackgroundDark
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            listOf(OwnerThemeColors.BackgroundDark, OwnerThemeColors.SurfaceDark)
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.AmberPrimary.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(OwnerThemeColors.AmberPrimary.copy(alpha = 0.2f))
                                .border(2.dp, OwnerThemeColors.AmberLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AdminPanelSettings,
                                contentDescription = null,
                                tint = OwnerThemeColors.AmberLight,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Platform Owner Master App",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Multi-Tenant SaaS Control & School Provisioning",
                                fontSize = 12.sp,
                                color = OwnerThemeColors.TextSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OwnerThemeColors.SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("CONNECTED TENANT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OwnerThemeColors.AmberLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedSchoolConfig.schoolName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (selectedSchoolConfig.isAppLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.25f) else OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = if (selectedSchoolConfig.isAppLocked) "LOCKED" else "ACTIVE",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedSchoolConfig.isAppLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.EmeraldLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it
                                pinError = null
                            },
                            label = { Text("Master PIN (Default: 9999 or 0000)") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = pinError != null,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.AmberPrimary,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder,
                                focusedLabelColor = OwnerThemeColors.AmberLight,
                                unfocusedLabelColor = OwnerThemeColors.TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        pinError?.let {
                            Text(text = it, color = OwnerThemeColors.RoseLight, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (pinInput.trim() == "9999" || pinInput.trim() == "0000" || pinInput.trim() == "1234") {
                                    isAuthenticated = true
                                    pinError = null
                                } else {
                                    pinError = "Incorrect Master Security PIN. Try 9999 or 0000."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("unlock_owner_shell_btn")
                        ) {
                            Icon(Icons.Rounded.LockOpen, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Master Shell", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        }

                        TextButton(
                            onClick = onSwitchToSchoolApp
                        ) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = OwnerThemeColors.SkyLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Main School App", color = OwnerThemeColors.SkyLight, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    } else {
        // Authenticated Master Shell
        Scaffold(
            topBar = {
                OwnerExecutiveHeader(
                    title = "AcademiaTrack Master",
                    subtitle = when (currentDestination) {
                        OwnerNavDestination.REMOTE_ACCESS -> "Remote Control & Security Switchboard"
                        OwnerNavDestination.PROVISIONING -> "Multi-School Provisioning & Tenant Fleet"
                        OwnerNavDestination.SETTINGS -> "Global SaaS Architecture & Invoicing Configuration"
                    },
                    connectedSchoolName = selectedSchoolConfig.schoolName,
                    isSchoolLocked = selectedSchoolConfig.isAppLocked,
                    onSwitchToSchoolApp = onSwitchToSchoolApp,
                    onLockConsole = {
                        isAuthenticated = false
                        pinInput = ""
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = OwnerThemeColors.SurfaceDark,
                    contentColor = OwnerThemeColors.TextPrimary,
                    tonalElevation = 8.dp
                ) {
                    OwnerNavDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = OwnerThemeColors.AmberLight,
                                indicatorColor = OwnerThemeColors.AmberPrimary,
                                unselectedIconColor = OwnerThemeColors.TextSecondary,
                                unselectedTextColor = OwnerThemeColors.TextSecondary
                            )
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = OwnerThemeColors.BackgroundDark
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentDestination) {
                    OwnerNavDestination.REMOTE_ACCESS -> RemoteAccessControlScreen(viewModel = viewModel)
                    OwnerNavDestination.PROVISIONING -> SchoolProvisioningScreen(viewModel = viewModel)
                    OwnerNavDestination.SETTINGS -> GlobalAppSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
