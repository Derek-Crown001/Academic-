package com.example.owner.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.owner.ui.components.OwnerStatPill
import com.example.owner.ui.components.OwnerThemeColors
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteAccessControlScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val allLicenseConfigs by viewModel.allLicenseConfigs.collectAsState()
    val selectedSchoolConfig by viewModel.selectedOwnerSchoolConfig.collectAsState()
    val allOwnerMemos by viewModel.allOwnerMemos.collectAsState()
    val allPaymentClaims by viewModel.allPaymentClaims.collectAsState()
    val allLicenseKeys by viewModel.allLicenseKeys.collectAsState()
    val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsState()
    val remoteStatuses by viewModel.remoteSchoolStatuses.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    var showSendMemoDialog by remember { mutableStateOf(false) }
    var showGenerateKeyDialog by remember { mutableStateOf(false) }
    var showFeatureLockDialog by remember { mutableStateOf(false) }
    var generatedKeyAlert by remember { mutableStateOf<String?>(null) }
    var showLockConfirmDialog by remember { mutableStateOf(false) }
    var lockReasonInput by remember { mutableStateOf("") }
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var maintenanceMsgInput by remember { mutableStateOf("Scheduled platform infrastructure maintenance in progress. All operations temporarily suspended.") }
    var maintenanceDurationInput by remember { mutableStateOf("In 2 hours") }

    val isTargetLocked = selectedSchoolConfig.isAppLocked
    val isTargetMaintenance = selectedSchoolConfig.isMaintenanceMode
    val targetSchoolCode = selectedSchoolConfig.schoolCode

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OwnerThemeColors.BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cloud Firestore Synchronization HUD
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OwnerThemeColors.SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.AmberPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(OwnerThemeColors.EmeraldSuccess)
                        )
                        Column {
                            Text(
                                text = "FIRESTORE CLOUD CONTROL",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = OwnerThemeColors.AmberLight
                            )
                            Text(
                                text = firestoreSyncStatus,
                                fontSize = 12.sp,
                                color = OwnerThemeColors.TextPrimary
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.syncSchoolToFirestore(selectedSchoolConfig) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CloudSync,
                            contentDescription = null,
                            tint = OwnerThemeColors.AmberLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync to Cloud", fontSize = 11.sp, color = OwnerThemeColors.AmberLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // School Selector Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TARGET TENANT SELECTOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerThemeColors.AmberLight
                    )
                    Text(
                        text = "${allLicenseConfigs.size} Schools Provisioned",
                        fontSize = 11.sp,
                        color = OwnerThemeColors.TextMuted
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allLicenseConfigs) { config ->
                        val isSelected = config.schoolCode == selectedSchoolConfig.schoolCode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OwnerThemeColors.AmberPrimary.copy(alpha = 0.2f) else OwnerThemeColors.CardDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) OwnerThemeColors.AmberLight else OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.clickable {
                                viewModel.switchOwnerSelectedSchool(config.schoolCode)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (config.isAppLocked) OwnerThemeColors.RoseDanger else OwnerThemeColors.EmeraldSuccess)
                                )
                                Text(
                                    text = config.schoolName.take(18) + if (config.schoolName.length > 18) "…" else "",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OwnerThemeColors.AmberLight else OwnerThemeColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Master Remote Kill-Switch & Arrears Paywall HUD
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isTargetLocked) OwnerThemeColors.RoseDanger else OwnerThemeColors.AmberPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isTargetLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.25f)
                                        else OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.25f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isTargetLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                    contentDescription = null,
                                    tint = if (isTargetLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Remote Master App Lock & Paywall",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OwnerThemeColors.TextPrimary
                                )
                                Text(
                                    text = "School: ${selectedSchoolConfig.schoolName} (${selectedSchoolConfig.schoolCode})",
                                    fontSize = 11.sp,
                                    color = OwnerThemeColors.TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isTargetLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.25f) else OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (isTargetLocked) "LOCKED" else "ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isTargetLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.EmeraldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isTargetLocked)
                            "⚠️ Current Lock Reason: \"${selectedSchoolConfig.lockReason}\""
                        else
                            "Target school is in good standing. ${selectedSchoolConfig.daysRemaining} days remaining on ${selectedSchoolConfig.subscriptionTier.name} tier.",
                        fontSize = 12.sp,
                        color = if (isTargetLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Toggle Lock Button
                        Button(
                            onClick = {
                                if (isTargetLocked) {
                                    viewModel.toggleSchoolAccountLock(
                                        schoolId = targetSchoolCode,
                                        isLocked = false,
                                        reason = "Subscription in good standing."
                                    )
                                } else {
                                    lockReasonInput = "Institutional subscription renewal required. Please contact platform management."
                                    showLockConfirmDialog = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTargetLocked) OwnerThemeColors.EmeraldSuccess else OwnerThemeColors.RoseDanger
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_master_lock_btn")
                        ) {
                            Icon(
                                if (isTargetLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTargetLocked) "Release Lock" else "Engage Lock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Push 1-Year Direct License Grant
                        Button(
                            onClick = {
                                viewModel.grantDirectAccess(
                                    schoolCode = targetSchoolCode,
                                    tier = SubscriptionTier.ANNUAL,
                                    durationDays = 365
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("push_1year_license_btn")
                        ) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Push 1-Yr License", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Remote Maintenance Mode HUD Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isTargetMaintenance) OwnerThemeColors.AmberPrimary else OwnerThemeColors.CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isTargetMaintenance) OwnerThemeColors.AmberPrimary.copy(alpha = 0.25f)
                                        else OwnerThemeColors.TextMuted.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Construction,
                                    contentDescription = null,
                                    tint = if (isTargetMaintenance) OwnerThemeColors.AmberLight else OwnerThemeColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Remote Instance Maintenance Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OwnerThemeColors.TextPrimary
                                )
                                Text(
                                    text = "Live Firestore Kill/Maintenance for ${selectedSchoolConfig.schoolName}",
                                    fontSize = 11.sp,
                                    color = OwnerThemeColors.TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isTargetMaintenance) OwnerThemeColors.AmberPrimary.copy(alpha = 0.25f) else OwnerThemeColors.SurfaceDark
                        ) {
                            Text(
                                text = if (isTargetMaintenance) "MAINTENANCE ON" else "STANDBY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isTargetMaintenance) OwnerThemeColors.AmberLight else OwnerThemeColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isTargetMaintenance)
                            "⚠️ Active Notice: \"${selectedSchoolConfig.maintenanceMessage}\" (Expected: ${selectedSchoolConfig.maintenanceExpectedEnd})"
                        else
                            "School instance is operating normally. Maintenance mode can be toggled remotely with real-time Firestore sync.",
                        fontSize = 12.sp,
                        color = if (isTargetMaintenance) OwnerThemeColors.AmberLight else OwnerThemeColors.TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isTargetMaintenance) {
                                    viewModel.toggleSchoolMaintenanceMode(
                                        schoolId = targetSchoolCode,
                                        isMaintenance = false
                                    )
                                } else {
                                    showMaintenanceDialog = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTargetMaintenance) OwnerThemeColors.EmeraldSuccess else OwnerThemeColors.AmberPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_maintenance_btn")
                        ) {
                            Icon(
                                if (isTargetMaintenance) Icons.Rounded.CheckCircle else Icons.Rounded.Engineering,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTargetMaintenance) "Disengage Maintenance (Cloud)" else "Engage Maintenance (Cloud)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isTargetMaintenance) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Granular Feature Lockdown Switchboard
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ToggleOn,
                                contentDescription = null,
                                tint = OwnerThemeColors.AmberLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Granular Feature Permissions Switchboard",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OwnerThemeColors.TextPrimary
                            )
                        }
                    }

                    Text(
                        text = "Remotely disable specific functional subsystems without locking the entire school app.",
                        fontSize = 11.sp,
                        color = OwnerThemeColors.TextSecondary
                    )

                    val featureList = listOf(
                        Triple("CBT Examination System", "CBT", selectedSchoolConfig.isCbtLocked),
                        Triple("AI Pedagogical Assistant (Gemini)", "AI", selectedSchoolConfig.isAiAssistantLocked),
                        Triple("Report Card Compilation & PDF Generator", "REPORT_CARD", selectedSchoolConfig.isReportCardLocked),
                        Triple("Faculty Clock-in & Attendance", "ATTENDANCE", selectedSchoolConfig.isTeacherAttendanceLocked),
                        Triple("Student Class Administration", "STUDENT_MGMT", selectedSchoolConfig.isStudentManagementLocked),
                        Triple("Multi-Channel Class Chat", "CHAT", selectedSchoolConfig.isChatRoomsLocked),
                        Triple("Parent Portal Access", "PARENT", selectedSchoolConfig.isParentPortalLocked)
                    )

                    featureList.forEach { (name, key, isLocked) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OwnerThemeColors.SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.5f) else OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.TextPrimary
                                    )
                                    Text(
                                        text = if (isLocked) "Feature is RESTRICTED remotely" else "Feature is ENABLED & ACTIVE",
                                        fontSize = 10.5.sp,
                                        color = if (isLocked) OwnerThemeColors.RoseDanger else OwnerThemeColors.EmeraldLight
                                    )
                                }

                                Switch(
                                    checked = !isLocked,
                                    onCheckedChange = { isEnabled ->
                                        viewModel.toggleFeatureLock(
                                            schoolCode = targetSchoolCode,
                                            featureKey = key,
                                            isLocked = !isEnabled
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OwnerThemeColors.EmeraldLight,
                                        checkedTrackColor = OwnerThemeColors.EmeraldSuccess,
                                        uncheckedThumbColor = OwnerThemeColors.RoseLight,
                                        uncheckedTrackColor = OwnerThemeColors.RoseDanger.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Operations Row: Generate License Key & Dispatch Invoicing Memo
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Key Generator Card Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OwnerThemeColors.CardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.AmberLight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showGenerateKeyDialog = true }
                        .testTag("open_key_generator_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(OwnerThemeColors.AmberPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Key, contentDescription = null, tint = OwnerThemeColors.AmberLight, modifier = Modifier.size(18.dp))
                        }
                        Text("Generate License Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OwnerThemeColors.TextPrimary)
                        Text("Issue 16-char crypto activation key for client", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)
                    }
                }

                // Send Official Memo Card Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OwnerThemeColors.CardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.SkyBlue.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSendMemoDialog = true }
                        .testTag("open_send_memo_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(OwnerThemeColors.SkyBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = OwnerThemeColors.SkyLight, modifier = Modifier.size(18.dp))
                        }
                        Text("Dispatch Memo / Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OwnerThemeColors.TextPrimary)
                        Text("Broadcast renewal invoice with bank remittance details", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)
                    }
                }
            }
        }

        // License Keys Vault Table
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.VpnKey, contentDescription = null, tint = OwnerThemeColors.AmberLight, modifier = Modifier.size(18.dp))
                            Text("License Key Vault", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = OwnerThemeColors.TextPrimary)
                        }
                        Text("${allLicenseKeys.size} Keys Generated", fontSize = 11.sp, color = OwnerThemeColors.TextMuted)
                    }

                    if (allLicenseKeys.isEmpty()) {
                        Text("No keys generated yet. Tap 'Generate License Key' above.", fontSize = 11.5.sp, color = OwnerThemeColors.TextSecondary)
                    } else {
                        allLicenseKeys.take(5).forEach { key ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OwnerThemeColors.SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = key.licenseKey,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = OwnerThemeColors.AmberLight
                                        )
                                        Text(
                                            text = "${key.tier.name} (${key.durationDays} Days) • ${if (key.isUsed) "Used by ${key.usedBySchoolCode}" else "Available / Unredeemed"}",
                                            fontSize = 10.5.sp,
                                            color = if (key.isUsed) OwnerThemeColors.TextMuted else OwnerThemeColors.EmeraldLight
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(key.licenseKey))
                                            viewModel.setFeedbackMessage("Copied ${key.licenseKey} to clipboard!")
                                        }
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = OwnerThemeColors.TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Payment Proof Claims Audit Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Payments, contentDescription = null, tint = OwnerThemeColors.EmeraldLight, modifier = Modifier.size(18.dp))
                            Text("Payment Claims Audit & Approvals", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = OwnerThemeColors.TextPrimary)
                        }
                        Text("${allPaymentClaims.size} Claims", fontSize = 11.sp, color = OwnerThemeColors.TextMuted)
                    }

                    if (allPaymentClaims.isEmpty()) {
                        Text("No payment claims pending audit.", fontSize = 11.5.sp, color = OwnerThemeColors.TextSecondary)
                    } else {
                        allPaymentClaims.forEach { claim ->
                            val isPending = claim.status == PaymentClaimStatus.PENDING
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OwnerThemeColors.SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPending) OwnerThemeColors.AmberLight.copy(alpha = 0.5f) else OwnerThemeColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(claim.schoolName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OwnerThemeColors.TextPrimary)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (claim.status) {
                                                PaymentClaimStatus.PENDING -> OwnerThemeColors.AmberPrimary.copy(alpha = 0.2f)
                                                PaymentClaimStatus.APPROVED -> OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.2f)
                                                PaymentClaimStatus.REJECTED -> OwnerThemeColors.RoseDanger.copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = claim.status.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (claim.status) {
                                                    PaymentClaimStatus.PENDING -> OwnerThemeColors.AmberLight
                                                    PaymentClaimStatus.APPROVED -> OwnerThemeColors.EmeraldLight
                                                    PaymentClaimStatus.REJECTED -> OwnerThemeColors.RoseLight
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text("Ref: ${claim.paymentReference} • Amount: ₦${String.format(Locale.US, "%,.2f", claim.amountPaid)}", fontSize = 11.5.sp, color = OwnerThemeColors.EmeraldLight)
                                    Text("Payer: ${claim.payerName} (${claim.payerPhone}) • Date: ${claim.paymentDate}", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)

                                    if (isPending) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.approvePaymentClaim(claim) },
                                                colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.EmeraldSuccess),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve & Unlock", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.rejectPaymentClaim(claim) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OwnerThemeColors.RoseLight),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.RoseDanger),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Rounded.Cancel, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reject Claim", fontSize = 11.5.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Generate Key Dialog
    if (showGenerateKeyDialog) {
        var selectedTier by remember { mutableStateOf(SubscriptionTier.ANNUAL) }
        var durationDaysInput by remember { mutableStateOf("365") }

        Dialog(onDismissRequest = { showGenerateKeyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.AmberPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Generate Cryptographic Key", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OwnerThemeColors.TextPrimary)

                    Text("Select License Tier:", fontSize = 12.sp, color = OwnerThemeColors.TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(SubscriptionTier.TERMLY, SubscriptionTier.ANNUAL, SubscriptionTier.LIFETIME).forEach { tier ->
                            val isSelected = selectedTier == tier
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) OwnerThemeColors.AmberPrimary else OwnerThemeColors.SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OwnerThemeColors.AmberLight else OwnerThemeColors.CardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTier = tier
                                        durationDaysInput = when (tier) {
                                            SubscriptionTier.TERMLY -> "120"
                                            SubscriptionTier.ANNUAL -> "365"
                                            SubscriptionTier.LIFETIME -> "3650"
                                            else -> "30"
                                        }
                                    }
                            ) {
                                Text(
                                    text = tier.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else OwnerThemeColors.TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = durationDaysInput,
                        onValueChange = { durationDaysInput = it },
                        label = { Text("Validity Duration (Days)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.AmberPrimary,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showGenerateKeyDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = OwnerThemeColors.TextSecondary)
                        }

                        Button(
                            onClick = {
                                val days = durationDaysInput.toIntOrNull() ?: 365
                                val key = viewModel.generateLicenseKey(selectedTier, days)
                                showGenerateKeyDialog = false
                                generatedKeyAlert = key
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Create Key", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Lock Confirmation Dialog
    if (showLockConfirmDialog) {
        Dialog(onDismissRequest = { showLockConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OwnerThemeColors.RoseDanger),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Confirm Remote Master Lock", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OwnerThemeColors.RoseLight)
                    Text("Locking '${selectedSchoolConfig.schoolName}' will activate the paywall and restrict all student, teacher, and admin operations.", fontSize = 12.sp, color = OwnerThemeColors.TextSecondary)

                    OutlinedTextField(
                        value = lockReasonInput,
                        onValueChange = { lockReasonInput = it },
                        label = { Text("Lock Reason Notice") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.RoseDanger,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showLockConfirmDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = OwnerThemeColors.TextSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.toggleSchoolAccountLock(
                                    schoolId = targetSchoolCode,
                                    isLocked = true,
                                    reason = lockReasonInput
                                )
                                showLockConfirmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.RoseDanger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Engage Lock (Cloud)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Maintenance Mode Dialog
    if (showMaintenanceDialog) {
        Dialog(onDismissRequest = { showMaintenanceDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OwnerThemeColors.AmberPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Engage Remote Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OwnerThemeColors.AmberLight)
                    Text("School app will display the maintenance overlay to all users for '${selectedSchoolConfig.schoolName}'.", fontSize = 12.sp, color = OwnerThemeColors.TextSecondary)

                    OutlinedTextField(
                        value = maintenanceMsgInput,
                        onValueChange = { maintenanceMsgInput = it },
                        label = { Text("Maintenance Advisory Message") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.AmberPrimary,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maintenanceDurationInput,
                        onValueChange = { maintenanceDurationInput = it },
                        label = { Text("Expected Completion Time") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.AmberPrimary,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showMaintenanceDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = OwnerThemeColors.TextSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.toggleSchoolMaintenanceMode(
                                    schoolId = targetSchoolCode,
                                    isMaintenance = true,
                                    message = maintenanceMsgInput,
                                    expectedEnd = maintenanceDurationInput
                                )
                                showMaintenanceDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Engage (Cloud)", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Send Memo Dialog
    if (showSendMemoDialog) {
        var memoTitle by remember { mutableStateOf("SUBSCRIPTION RENEWAL INVOICE") }
        var memoBody by remember { mutableStateOf("Your institutional subscription is due for renewal. Please remit payment to our master account to maintain uninterrupted school services.") }
        var memoAmount by remember { mutableStateOf("400000") }
        var memoDueDate by remember { mutableStateOf("Within 7 Days") }

        Dialog(onDismissRequest = { showSendMemoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.SkyBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Dispatch Official Memo / Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OwnerThemeColors.TextPrimary)

                    OutlinedTextField(
                        value = memoTitle,
                        onValueChange = { memoTitle = it },
                        label = { Text("Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.SkyBlue,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memoAmount,
                        onValueChange = { memoAmount = it },
                        label = { Text("Amount Due (₦)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.SkyBlue,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memoDueDate,
                        onValueChange = { memoDueDate = it },
                        label = { Text("Payment Due Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.SkyBlue,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memoBody,
                        onValueChange = { memoBody = it },
                        label = { Text("Notice Body") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.SkyBlue,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showSendMemoDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = OwnerThemeColors.TextSecondary)
                        }

                        Button(
                            onClick = {
                                val amount = memoAmount.toDoubleOrNull() ?: 0.0
                                viewModel.sendAppOwnerMemo(
                                    schoolCode = targetSchoolCode,
                                    title = memoTitle,
                                    memoBody = memoBody,
                                    amountDue = amount,
                                    dueDate = memoDueDate
                                )
                                showSendMemoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.SkyBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dispatch Memo", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
