package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.OwnerConsoleActivity
import com.example.R
import com.example.data.model.*
import com.example.ui.components.OwnerApkDownloadDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * App Owner Master Control, Multi-School Switcher & Remote Subscription Management Portal.
 * Enables the App Owner to:
 * 1. View all client schools registered into the system in real time.
 * 2. Filter schools by Subscription Due, Expired/Locked, and Active.
 * 3. Switch between schools to remotely control permissions, features, and master locks.
 * 4. Issue targeted renewal invoices and broadcast official memos.
 * 5. Generate 16-character license activation keys.
 * 6. Manually onboard new client schools.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOwnerConsoleScreen(viewModel: SchoolViewModel) {
    val allLicenseConfigs by viewModel.allLicenseConfigs.collectAsState()
    val selectedSchoolConfig by viewModel.selectedOwnerSchoolConfig.collectAsState()
    val selectedSchoolCode by viewModel.selectedOwnerSchoolCode.collectAsState()
    val allOwnerMemos by viewModel.allOwnerMemos.collectAsState()
    val allPaymentClaims by viewModel.allPaymentClaims.collectAsState()
    val allLicenseKeys by viewModel.allLicenseKeys.collectAsState()

    val clipboard = LocalClipboardManager.current
    var selectedSection by remember { mutableStateOf(0) }
    val sectionTitles = listOf("Remote Control", "Invoices & Memos", "License Keys", "Payment Claims", "Owner Settings", "Console Artifact")

    // Filter state for School Directory
    var selectedSchoolFilter by remember { mutableStateOf("ALL") } // ALL, DUE, ACTIVE, LOCKED

    // Dialogs
    var showSendMemoDialog by remember { mutableStateOf(false) }
    var showCustomLockReasonDialog by remember { mutableStateOf(false) }
    var lockReasonDraft by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.lockReason) }
    var showAddSchoolDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDownloadApkDialog by remember { mutableStateOf(false) }

    var generatedKey by remember { mutableStateOf<String?>(null) }
    var selectedTierForGen by remember { mutableStateOf(SubscriptionTier.ANNUAL) }

    // Settings fields
    var bankNameInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerBankName) }
    var accountNumberInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerAccountNumber) }
    var accountNameInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerAccountName) }
    var phoneInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerContactPhone) }
    var emailInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.registeredOwnerEmail) }
    var annualFeeInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.subscriptionFeePerYear.toInt().toString()) }
    var termlyFeeInput by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.subscriptionFeePerTerm.toInt().toString()) }

    // Multi-School metrics
    val totalSchoolsCount = allLicenseConfigs.size
    val dueCount = allLicenseConfigs.count { it.isDueSoon || (it.isOverdue && !it.isAppLocked) }
    val lockedCount = allLicenseConfigs.count { it.isAppLocked }
    val activeCount = allLicenseConfigs.count { !it.isAppLocked && !it.isOverdue }

    val filteredSchools = remember(allLicenseConfigs, selectedSchoolFilter) {
        allLicenseConfigs.filter { school ->
            when (selectedSchoolFilter) {
                "DUE" -> school.isDueSoon || (school.isOverdue && !school.isAppLocked)
                "ACTIVE" -> !school.isAppLocked && !school.isOverdue
                "LOCKED" -> school.isAppLocked
                else -> true
            }
        }
    }

    val isTargetLocked = selectedSchoolConfig.isAppLocked
    val daysRemaining = selectedSchoolConfig.daysRemaining
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Standalone Master SaaS Remote Control Bar
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "STANDALONE MASTER CONTROLLER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFBBF24),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Target Client: ${selectedSchoolConfig.schoolName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Live Target Status Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isTargetLocked) Color(0xFFDC2626).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isTargetLocked) Color(0xFFEF4444) else Color(0xFF34D399)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isTargetLocked) Color(0xFFEF4444) else Color(0xFF34D399))
                                    )
                                    Text(
                                        text = if (isTargetLocked) "CLIENT LOCKED" else "CLIENT ACTIVE",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isTargetLocked) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Control the main school client remotely in real time. Actions applied here take effect immediately across all student, teacher, and admin portals.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )

                        // Quick Control Action Buttons Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Switch to School Client App View
                            Button(
                                onClick = {
                                    viewModel.selectPortal(SchoolRole.ADMIN)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("switch_to_school_client_btn")
                            ) {
                                Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Switch to School App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Quick Toggle Lock Button
                            Button(
                                onClick = {
                                    viewModel.toggleMasterAppLock(
                                        schoolCode = selectedSchoolCode,
                                        isLocked = !isTargetLocked,
                                        customReason = if (!isTargetLocked) "Institutional subscription renewal required. Please contact platform management." else "Subscription is in good standing."
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTargetLocked) Color(0xFF10B981) else Color(0xFFDC2626)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("quick_toggle_remote_lock")
                            ) {
                                Icon(
                                    if (isTargetLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isTargetLocked) "Remote Unlock School" else "Remote Kill-Switch Lock",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Push 1-Year License Button
                            Button(
                                onClick = {
                                    viewModel.grantDirectAccess(
                                        schoolCode = selectedSchoolCode,
                                        tier = SubscriptionTier.ANNUAL,
                                        durationDays = 365
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("push_master_license_btn")
                            ) {
                                Icon(Icons.Rounded.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Push 1-Yr License", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Launch Standalone Owner Activity
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(context, OwnerConsoleActivity::class.java)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.setFeedbackMessage("Standalone Activity Launched")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("launch_standalone_activity_btn")
                            ) {
                                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Standalone Window", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Download Owner APK from GitHub
                            Button(
                                onClick = { showDownloadApkDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("owner_screen_download_apk_btn")
                            ) {
                                Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download APK (GitHub)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Executive Header Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AdminPanelSettings,
                                        contentDescription = "Master Owner Shield",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "APP OWNER MASTER CONSOLE",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.2.sp
                                    )
                                    Text(
                                        text = "Multi-School Management & Licensing",
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddSchoolDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New School", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Metrics Grid Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OwnerMetricBadge(
                                label = "Total Schools",
                                value = "$totalSchoolsCount",
                                containerColor = Color(0xFF1E293B),
                                textColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            OwnerMetricBadge(
                                label = "Active",
                                value = "$activeCount",
                                containerColor = Color(0xFF064E3B).copy(alpha = 0.6f),
                                textColor = Color(0xFF34D399),
                                modifier = Modifier.weight(1f)
                            )
                            OwnerMetricBadge(
                                label = "Due / Expiring",
                                value = "$dueCount",
                                containerColor = Color(0xFF78350F).copy(alpha = 0.6f),
                                textColor = Color(0xFFFBBF24),
                                modifier = Modifier.weight(1f)
                            )
                            OwnerMetricBadge(
                                label = "Locked",
                                value = "$lockedCount",
                                containerColor = Color(0xFF7F1D1D).copy(alpha = 0.6f),
                                textColor = Color(0xFFF87171),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section: School Switcher & Registered Schools Directory
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
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
                                    Icons.Rounded.Apartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Registered Schools Directory",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Tap school to control",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Filter Chips Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipItem(
                                label = "All ($totalSchoolsCount)",
                                selected = selectedSchoolFilter == "ALL",
                                onClick = { selectedSchoolFilter = "ALL" }
                            )
                            FilterChipItem(
                                label = "Due Soon ($dueCount)",
                                selected = selectedSchoolFilter == "DUE",
                                onClick = { selectedSchoolFilter = "DUE" },
                                highlightColor = Color(0xFFD97706)
                            )
                            FilterChipItem(
                                label = "Active ($activeCount)",
                                selected = selectedSchoolFilter == "ACTIVE",
                                onClick = { selectedSchoolFilter = "ACTIVE" },
                                highlightColor = Color(0xFF059669)
                            )
                            FilterChipItem(
                                label = "Locked ($lockedCount)",
                                selected = selectedSchoolFilter == "LOCKED",
                                onClick = { selectedSchoolFilter = "LOCKED" },
                                highlightColor = Color(0xFFDC2626)
                            )
                        }

                        // Horizontal School Switcher Cards Carousel
                        if (filteredSchools.isEmpty()) {
                            Text(
                                text = "No schools matching filter found.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(filteredSchools) { school ->
                                    val isSelected = school.schoolCode == selectedSchoolCode
                                    SchoolSelectorCard(
                                        school = school,
                                        isSelected = isSelected,
                                        onClick = {
                                            viewModel.switchOwnerSelectedSchool(school.schoolCode)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Currently Selected School Control Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTargetLocked) Color(0xFF7F1D1D) else Color(0xFF064E3B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "CONTROLLING SCHOOL:",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White.copy(alpha = 0.75f),
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = selectedSchoolConfig.schoolName,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Passkey: ${selectedSchoolConfig.schoolCode} • ${selectedSchoolConfig.schoolCity} • Admin: ${selectedSchoolConfig.principalName}",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTargetLocked) Color(0xFFEF4444) else if (selectedSchoolConfig.isDueSoon) Color(0xFFF59E0B) else Color(0xFF10B981)
                            ) {
                                Text(
                                    text = selectedSchoolConfig.statusBadgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.15f))

                        // Quick Action Buttons Row for Active School
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isTargetLocked) {
                                        viewModel.toggleMasterAppLock(selectedSchoolConfig.schoolCode, false)
                                    } else {
                                        showCustomLockReasonDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTargetLocked) Color(0xFF10B981) else Color(0xFFDC2626)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (isTargetLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTargetLocked) "Unlock App" else "Master Lock",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { showSendMemoDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send Invoice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    viewModel.grantDirectAccess(selectedSchoolConfig.schoolCode, SubscriptionTier.ANNUAL, 365)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 Yr Renew", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Navigation Tabs Bar
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedSection,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    sectionTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSection == index,
                            onClick = { selectedSection = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // TAB 0: Remote Control & Granular Feature Locks
            if (selectedSection == 0) {
                item {
                    // Master Lock Switch Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTargetLocked) AcademicRose.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isTargetLocked) AcademicRose else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTargetLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                        contentDescription = null,
                                        tint = if (isTargetLocked) AcademicRose else Color(0xFF059669),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Master App Remote Lock",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isTargetLocked) "School is currently locked out of the app" else "School has full access to the platform",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = isTargetLocked,
                                    onCheckedChange = { locked ->
                                        if (locked) {
                                            showCustomLockReasonDialog = true
                                        } else {
                                            viewModel.toggleMasterAppLock(selectedSchoolConfig.schoolCode, false)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AcademicRose,
                                        checkedTrackColor = AcademicRose.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            if (isTargetLocked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AcademicRose.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Info, contentDescription = null, tint = AcademicRose, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Lock Reason Shown to School: \"${selectedSchoolConfig.lockReason}\"",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Granular Feature Locks for ${selectedSchoolConfig.schoolName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val featureLocks = listOf(
                    Triple("CBT", "CBT Examination System", selectedSchoolConfig.isCbtLocked),
                    Triple("AI", "Gemini AI Pedagogical Assistant", selectedSchoolConfig.isAiAssistantLocked),
                    Triple("REPORT_CARD", "Gradebook & Report Card PDF Generator", selectedSchoolConfig.isReportCardLocked),
                    Triple("ATTENDANCE", "Faculty Staff Clock-in Register", selectedSchoolConfig.isTeacherAttendanceLocked),
                    Triple("STUDENT_MGMT", "Student Class Management", selectedSchoolConfig.isStudentManagementLocked),
                    Triple("CHAT", "Class & Staff Multi-Channel Chat", selectedSchoolConfig.isChatRoomsLocked),
                    Triple("PARENT", "Parent Academic & Fee Portal", selectedSchoolConfig.isParentPortalLocked)
                )

                items(featureLocks) { (key, name, isLocked) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isLocked) AcademicRose else Color(0xFF059669),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(text = name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (isLocked) "Feature is Restricted" else "Feature is Active",
                                        fontSize = 11.sp,
                                        color = if (isLocked) AcademicRose else Color(0xFF059669)
                                    )
                                }
                            }

                            Switch(
                                checked = isLocked,
                                onCheckedChange = { locked ->
                                    viewModel.toggleFeatureLock(selectedSchoolConfig.schoolCode, key, locked)
                                }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "1-Click Direct Access Extension",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.grantDirectAccess(selectedSchoolConfig.schoolCode, SubscriptionTier.TRIAL, 14) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+14 Days Trial", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.grantDirectAccess(selectedSchoolConfig.schoolCode, SubscriptionTier.TERMLY, 120) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+1 Term (120D)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.grantDirectAccess(selectedSchoolConfig.schoolCode, SubscriptionTier.ANNUAL, 365) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+1 Year (365D)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.grantDirectAccess(selectedSchoolConfig.schoolCode, SubscriptionTier.LIFETIME, 3650) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Lifetime Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Danger zone: Delete school
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AcademicRose),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove '${selectedSchoolConfig.schoolName}' from Registry", fontSize = 12.sp)
                    }
                }
            }

            // TAB 1: Invoices & Memos Hub
            if (selectedSection == 1) {
                item {
                    Button(
                        onClick = { showSendMemoDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Targeted Invoice to ${selectedSchoolConfig.schoolName}", fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Text(
                        text = "Broadcasted Invoices & Memos (${allOwnerMemos.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (allOwnerMemos.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No memos or invoices broadcasted yet. Tap the button above to issue a renewal notice or invoice to any school.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(allOwnerMemos) { memo ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (memo.isPaid) Color(0xFF059669).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (memo.isPaid) "PAID / CLEARED" else "OUTSTANDING",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (memo.isPaid) Color(0xFF059669) else Color(0xFFD97706),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = memo.schoolCode,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(memo.createdAtMillis)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(text = memo.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                                Text(text = memo.memoBody, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                if (memo.amountDue > 0) {
                                    Text(
                                        text = "Amount Due: ${memo.currency}${String.format(Locale.US, "%,.2f", memo.amountDue)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { viewModel.markOwnerMemoPaid(memo.id, !memo.isPaid) }
                                    ) {
                                        Text(if (memo.isPaid) "Mark Unpaid" else "Mark Paid")
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteOwnerMemo(memo) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = AcademicRose, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: 16-Digit License Key Generator & Vault
            if (selectedSection == 2) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Generate License Activation Key", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Generate cryptographic 16-character keys to give school owners after payment verification.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(SubscriptionTier.TERMLY, SubscriptionTier.ANNUAL, SubscriptionTier.LIFETIME).forEach { tier ->
                                    val isSelected = selectedTierForGen == tier
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedTierForGen = tier }
                                    ) {
                                        Text(
                                            text = tier.name,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val days = when (selectedTierForGen) {
                                        SubscriptionTier.TERMLY -> 120
                                        SubscriptionTier.ANNUAL -> 365
                                        SubscriptionTier.LIFETIME -> 3650
                                        else -> 365
                                    }
                                    generatedKey = viewModel.generateLicenseKey(selectedTierForGen, days)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate ${selectedTierForGen.name} Key", fontWeight = FontWeight.Bold)
                            }

                            generatedKey?.let { key ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF059669).copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Latest Generated Key:", fontSize = 11.sp, color = Color(0xFF059669))
                                            Text(key, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                                        }

                                        IconButton(
                                            onClick = {
                                                clipboard.setText(AnnotatedString(key))
                                                viewModel.setFeedbackMessage("Key '$key' copied to clipboard!")
                                            }
                                        ) {
                                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = Color(0xFF059669))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Generated Keys Registry (${allLicenseKeys.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(allLicenseKeys) { keyObj ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = keyObj.licenseKey, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${keyObj.tier.name} • ${keyObj.durationDays} Days • ${if (keyObj.isUsed) "Redeemed by ${keyObj.usedBySchoolCode}" else "Available"}",
                                    fontSize = 11.5.sp,
                                    color = if (keyObj.isUsed) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF059669),
                                    fontWeight = if (keyObj.isUsed) FontWeight.Normal else FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(keyObj.licenseKey))
                                    viewModel.setFeedbackMessage("Copied ${keyObj.licenseKey}!")
                                }
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // TAB 3: Payment Claims
            if (selectedSection == 3) {
                item {
                    Text(
                        text = "Submitted School Payment Claims (${allPaymentClaims.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (allPaymentClaims.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No payment claims submitted yet by school admins.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(allPaymentClaims) { claim ->
                        val isPending = claim.status == PaymentClaimStatus.PENDING
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPending) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (claim.status) {
                                            PaymentClaimStatus.PENDING -> Color(0xFFD97706).copy(alpha = 0.15f)
                                            PaymentClaimStatus.APPROVED -> Color(0xFF059669).copy(alpha = 0.15f)
                                            PaymentClaimStatus.REJECTED -> AcademicRose.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = claim.status.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (claim.status) {
                                                PaymentClaimStatus.PENDING -> Color(0xFFD97706)
                                                PaymentClaimStatus.APPROVED -> Color(0xFF059669)
                                                PaymentClaimStatus.REJECTED -> AcademicRose
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = claim.paymentDate,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "Amount: ${selectedSchoolConfig.currencySymbol}${String.format(Locale.US, "%,.2f", claim.amountPaid)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "Bank Reference: ${claim.paymentReference}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Payer: ${claim.payerName} (${claim.payerPhone}) • School: ${claim.schoolName} (${claim.schoolCode})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (claim.notes.isNotBlank()) {
                                    Text(
                                        text = "Notes: ${claim.notes}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isPending) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.rejectPaymentClaim(claim) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { viewModel.approvePaymentClaim(claim, 365) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve & Grant 1-Yr Access", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: Owner Bank & Pricing Settings
            if (selectedSection == 4) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "App Owner Bank & Subscription Settings", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = bankNameInput,
                                onValueChange = { bankNameInput = it },
                                label = { Text("Bank Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = accountNumberInput,
                                onValueChange = { accountNumberInput = it },
                                label = { Text("Account Number") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = accountNameInput,
                                onValueChange = { accountNameInput = it },
                                label = { Text("Account Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = annualFeeInput,
                                onValueChange = { annualFeeInput = it },
                                label = { Text("Annual License Fee (${selectedSchoolConfig.currencySymbol})") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = termlyFeeInput,
                                onValueChange = { termlyFeeInput = it },
                                label = { Text("Termly License Fee (${selectedSchoolConfig.currencySymbol})") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("App Owner Phone Number") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("App Owner Email") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val updated = selectedSchoolConfig.copy(
                                        ownerBankName = bankNameInput.trim(),
                                        ownerAccountNumber = accountNumberInput.trim(),
                                        ownerAccountName = accountNameInput.trim(),
                                        ownerContactPhone = phoneInput.trim(),
                                        registeredOwnerEmail = emailInput.trim(),
                                        subscriptionFeePerYear = annualFeeInput.toDoubleOrNull() ?: selectedSchoolConfig.subscriptionFeePerYear,
                                        subscriptionFeePerTerm = termlyFeeInput.toDoubleOrNull() ?: selectedSchoolConfig.subscriptionFeePerTerm,
                                        lastSyncTimestampMillis = System.currentTimeMillis()
                                    )
                                    viewModel.updateLicenseConfig(updated)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save App Owner Settings", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // TAB 5: Platform Owner Console UI Artifact & Architecture Inspector
            if (selectedSection == 5) {
                item {
                    var showFullscreenImage by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
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
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.DashboardCustomize,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "SaaS Owner App Artifact",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp
                                            )
                                            Text(
                                                "High-Fidelity Master Console Interface",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showFullscreenImage = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(Icons.Rounded.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Enlarge", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Interactive Image Frame
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF0F172A))
                                        .clickable { showFullscreenImage = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.owner_console_artifact_1788169964429),
                                        contentDescription = "Platform Owner App Artifact UI Mockup",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Subtle Overlay Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Text("Tap to Inspect (1080p UI)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Text(
                                    text = "This architectural artifact represents the multi-tenant SaaS control surface deployed for institutional oversight, cross-school remote licensing, cryptographic key distribution, and central revenue auditing.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Architectural Feature Matrix
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Master Platform Capabilities",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                val features = listOf(
                                    Pair("Remote Kill-Switch & Paywall", "Instant lock & unlock with custom arrears notices across all client installations."),
                                    Pair("16-Digit Cryptographic Vault", "Generates and validates ACAD-ANNL-XXXX-XXXX license tokens."),
                                    Pair("Dual Payment Rails", "Automated bank transfer receipt audit and direct online gateway verification."),
                                    Pair("Unified Directive Dispatch", "Emergency circular broadcasts directly injected into school Admin & Teacher headers."),
                                    Pair("Multi-Tenant SQLite Sync", "Offline-first resilience coupled with automatic cloud reconciliation.")
                                )

                                features.forEach { (title, desc) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Column {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Fullscreen Image Dialog
                    if (showFullscreenImage) {
                        Dialog(onDismissRequest = { showFullscreenImage = false }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Owner App Artifact View",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        IconButton(
                                            onClick = { showFullscreenImage = false },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Close",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.owner_console_artifact_1788169964429),
                                            contentDescription = "Full Owner App UI Artifact",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Register New School by App Owner
    if (showAddSchoolDialog) {
        var newSchoolName by remember { mutableStateOf("") }
        var newSchoolCode by remember { mutableStateOf("") }
        var newPrincipalName by remember { mutableStateOf("") }
        var newCity by remember { mutableStateOf("Lagos") }
        var newTier by remember { mutableStateOf(SubscriptionTier.ANNUAL) }
        var newDurationDays by remember { mutableStateOf("365") }

        AlertDialog(
            onDismissRequest = { showAddSchoolDialog = false },
            title = {
                Text("Register New Client School", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newSchoolName,
                        onValueChange = {
                            newSchoolName = it
                            if (newSchoolCode.isBlank() && it.isNotBlank()) {
                                val clean = it.filter { c -> c.isLetterOrDigit() }.take(8).uppercase()
                                newSchoolCode = "SCH-$clean-01"
                            }
                        },
                        label = { Text("School Name") },
                        placeholder = { Text("e.g. Atlantic Hall High School") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newSchoolCode,
                        onValueChange = { newSchoolCode = it },
                        label = { Text("School Passkey / ID") },
                        placeholder = { Text("e.g. SCH-ATLANTIC-01") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPrincipalName,
                        onValueChange = { newPrincipalName = it },
                        label = { Text("Principal / Administrator Name") },
                        placeholder = { Text("e.g. Dr. O. Williams") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("City / State") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDurationDays,
                        onValueChange = { newDurationDays = it },
                        label = { Text("Initial Duration (Days)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = newDurationDays.toIntOrNull() ?: 365
                        viewModel.addNewSchoolByOwner(
                            schoolName = newSchoolName,
                            schoolCode = newSchoolCode,
                            principalName = newPrincipalName,
                            city = newCity,
                            tier = newTier,
                            durationDays = days
                        )
                        showAddSchoolDialog = false
                    },
                    enabled = newSchoolName.isNotBlank() && newSchoolCode.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Register School", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSchoolDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Send Targeted Invoice to Selected School
    if (showSendMemoDialog) {
        var memoTitleInput by remember { mutableStateOf("Platform Subscription Renewal - ${selectedSchoolConfig.schoolName}") }
        var memoBodyInput by remember { mutableStateOf("Dear School Administration, your termly platform license is due. Please make payment to the account details provided below to maintain full system access.") }
        var memoAmountInput by remember { mutableStateOf(selectedSchoolConfig.subscriptionFeePerYear.toInt().toString()) }
        var memoDueDateInput by remember { mutableStateOf("7 Days from Notice") }
        var memoTypeInput by remember { mutableStateOf(OwnerMemoType.PAYMENT_INVOICE) }

        AlertDialog(
            onDismissRequest = { showSendMemoDialog = false },
            title = {
                Text("Send Official Invoice / Memo", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Target: ${selectedSchoolConfig.schoolName} (${selectedSchoolConfig.schoolCode})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = memoTitleInput,
                        onValueChange = { memoTitleInput = it },
                        label = { Text("Memo Title / Subject") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memoBodyInput,
                        onValueChange = { memoBodyInput = it },
                        label = { Text("Memo Body / Instructions") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memoAmountInput,
                        onValueChange = { memoAmountInput = it },
                        label = { Text("Amount Due (${selectedSchoolConfig.currencySymbol})") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memoDueDateInput,
                        onValueChange = { memoDueDateInput = it },
                        label = { Text("Payment Due Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = memoAmountInput.toDoubleOrNull() ?: 0.0
                        viewModel.sendAppOwnerMemo(
                            schoolCode = selectedSchoolConfig.schoolCode,
                            title = memoTitleInput,
                            memoBody = memoBodyInput,
                            memoType = memoTypeInput,
                            amountDue = amount,
                            dueDate = memoDueDateInput,
                            paymentBank = selectedSchoolConfig.ownerBankName,
                            accountNumber = selectedSchoolConfig.ownerAccountNumber,
                            accountName = selectedSchoolConfig.ownerAccountName
                        )
                        showSendMemoDialog = false
                    },
                    enabled = memoTitleInput.isNotBlank() && memoBodyInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Broadcast Memo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendMemoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Customize Lock Reason
    if (showCustomLockReasonDialog) {
        AlertDialog(
            onDismissRequest = { showCustomLockReasonDialog = false },
            icon = {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = AcademicRose, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Engage Master Remote Lock", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "This will immediately lock '${selectedSchoolConfig.schoolName}' out of the app. Specify the message to display on their lock screen:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = lockReasonDraft,
                        onValueChange = { lockReasonDraft = it },
                        label = { Text("Lock Screen Notice") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleMasterAppLock(selectedSchoolConfig.schoolCode, true, lockReasonDraft)
                        showCustomLockReasonDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicRose),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Lock School App Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomLockReasonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Confirm Delete School
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text("Delete School from Registry?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to remove '${selectedSchoolConfig.schoolName}' (${selectedSchoolConfig.schoolCode}) from the App Owner Console? This will remove their licensing configuration.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchoolByOwner(selectedSchoolConfig.schoolCode)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicRose),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete School", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: GitHub Releases APK Distribution Dialog
    if (showDownloadApkDialog) {
        OwnerApkDownloadDialog(
            onDismiss = { showDownloadApkDialog = false },
            onShowToast = { msg ->
                viewModel.setFeedbackMessage(msg)
            }
        )
    }
}

@Composable
private fun OwnerMetricBadge(
    label: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) highlightColor else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SchoolSelectorCard(
    school: AppOwnerLicenseConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isOverdue = school.isOverdue
    val isDueSoon = school.isDueSoon
    val isLocked = school.isAppLocked

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else if (isLocked) AcademicRose.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .width(230.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLocked) Color(0xFFEF4444) else if (isDueSoon) Color(0xFFF59E0B) else Color(0xFF10B981)
                ) {
                    Text(
                        text = school.statusBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = school.schoolName,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${school.schoolCode} • ${school.schoolCity}",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Text(
                text = "Expires: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(school.subscriptionExpiryDateMillis))}",
                fontSize = 10.5.sp,
                color = if (isLocked || isOverdue) AcademicRose else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isLocked || isOverdue) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
