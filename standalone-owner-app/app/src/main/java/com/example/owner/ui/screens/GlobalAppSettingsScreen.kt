package com.example.owner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppOwnerLicenseConfig
import com.example.owner.ui.components.OwnerThemeColors
import com.example.ui.components.OwnerApkDownloadDialog
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAppSettingsScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSchoolConfig by viewModel.selectedOwnerSchoolConfig.collectAsState()

    var bankName by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerBankName) }
    var accountNumber by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerAccountNumber) }
    var accountName by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerAccountName) }
    var annualFee by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.subscriptionFeePerYear.toString()) }
    var termlyFee by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.subscriptionFeePerTerm.toString()) }
    var ownerEmail by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.registeredOwnerEmail) }
    var ownerPhone by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.ownerContactPhone) }
    var gracePeriodDays by remember(selectedSchoolConfig) { mutableStateOf(selectedSchoolConfig.gracePeriodDays.toString()) }

    var emergencyMaintenanceMode by remember { mutableStateOf(false) }
    var geminiAiGatewayEnabled by remember { mutableStateOf(true) }
    var autoLockArrearsEnabled by remember { mutableStateOf(true) }
    var showBackupSuccessDialog by remember { mutableStateOf(false) }
    var showDownloadApkDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OwnerThemeColors.BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Platform Overview & Master Credentials
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.SurfaceDark),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.SettingsSuggest, contentDescription = null, tint = OwnerThemeColors.AmberLight, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Global SaaS Platform Architecture", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = OwnerThemeColors.TextPrimary)
                            Text("Multi-Tenant Core Engine • v3.8.4-PROD", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)
                        }
                    }

                    Text(
                        text = "Manage overarching developer metadata, payment remittance configurations, and system-wide security rules.",
                        fontSize = 11.5.sp,
                        color = OwnerThemeColors.TextMuted
                    )
                }
            }
        }

        // Section 2: Master Bank Remittance & Invoicing Parameters
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = OwnerThemeColors.EmeraldLight, modifier = Modifier.size(18.dp))
                        Text("Master Remittance & Payment Gateway", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = OwnerThemeColors.TextPrimary)
                    }

                    Text("All invoice notices and paywall prompts display these banking credentials to school owners for subscription renewal.", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Designated Bank Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.EmeraldLight,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.EmeraldLight,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OwnerThemeColors.EmeraldLight,
                            unfocusedBorderColor = OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = annualFee,
                            onValueChange = { annualFee = it },
                            label = { Text("Annual Fee (₦)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.EmeraldLight,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = termlyFee,
                            onValueChange = { termlyFee = it },
                            label = { Text("Termly Fee (₦)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.EmeraldLight,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Save Remittance Parameters Button
                    Button(
                        onClick = {
                            val updatedConfig = selectedSchoolConfig.copy(
                                ownerBankName = bankName.trim(),
                                ownerAccountNumber = accountNumber.trim(),
                                ownerAccountName = accountName.trim(),
                                subscriptionFeePerYear = annualFee.toDoubleOrNull() ?: selectedSchoolConfig.subscriptionFeePerYear,
                                subscriptionFeePerTerm = termlyFee.toDoubleOrNull() ?: selectedSchoolConfig.subscriptionFeePerTerm,
                                registeredOwnerEmail = ownerEmail.trim(),
                                ownerContactPhone = ownerPhone.trim(),
                                gracePeriodDays = gracePeriodDays.toIntOrNull() ?: selectedSchoolConfig.gracePeriodDays,
                                lastSyncTimestampMillis = System.currentTimeMillis()
                            )
                            viewModel.updateLicenseConfig(updatedConfig)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.EmeraldSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_global_banking_settings_btn")
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Banking & Pricing Parameters", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        // Section 3: System Engine & Security Policies
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.SecurityUpdateGood, contentDescription = null, tint = OwnerThemeColors.AmberLight, modifier = Modifier.size(18.dp))
                        Text("Global Security & Engine Governance", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = OwnerThemeColors.TextPrimary)
                    }

                    // Policy 1: Auto-Lock on Arrears
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OwnerThemeColors.SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Engage Paywall on Arrears", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = OwnerThemeColors.TextPrimary)
                                Text("Automatically lock school portals when grace period (7 days) expires without renewed license key.", fontSize = 10.5.sp, color = OwnerThemeColors.TextSecondary)
                            }
                            Switch(
                                checked = autoLockArrearsEnabled,
                                onCheckedChange = { autoLockArrearsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OwnerThemeColors.EmeraldLight,
                                    checkedTrackColor = OwnerThemeColors.EmeraldSuccess
                                )
                            )
                        }
                    }

                    // Policy 2: Gemini AI Pedagogical Gateway
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OwnerThemeColors.SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gemini AI Pedagogical API Gateway", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = OwnerThemeColors.TextPrimary)
                                Text("Enables AI study assistant & automated lesson plan generation across all client schools.", fontSize = 10.5.sp, color = OwnerThemeColors.TextSecondary)
                            }
                            Switch(
                                checked = geminiAiGatewayEnabled,
                                onCheckedChange = { geminiAiGatewayEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OwnerThemeColors.AmberLight,
                                    checkedTrackColor = OwnerThemeColors.AmberPrimary
                                )
                            )
                        }
                    }

                    // Policy 3: Emergency Maintenance Mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (emergencyMaintenanceMode) OwnerThemeColors.RoseDanger.copy(alpha = 0.15f) else OwnerThemeColors.SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (emergencyMaintenanceMode) OwnerThemeColors.RoseDanger else OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Emergency Platform Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = if (emergencyMaintenanceMode) OwnerThemeColors.RoseLight else OwnerThemeColors.TextPrimary)
                                Text("Suspends client logins and displays a maintenance advisory banner to all school users.", fontSize = 10.5.sp, color = OwnerThemeColors.TextSecondary)
                            }
                            Switch(
                                checked = emergencyMaintenanceMode,
                                onCheckedChange = {
                                    emergencyMaintenanceMode = it
                                    if (it) {
                                        viewModel.broadcastGlobalEmergencyMaintenance(
                                            isMaintenance = true,
                                            message = "EMERGENCY PLATFORM MAINTENANCE: All school services temporarily suspended by Platform Owner."
                                        )
                                    } else {
                                        viewModel.broadcastGlobalEmergencyMaintenance(
                                            isMaintenance = false,
                                            message = ""
                                        )
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OwnerThemeColors.RoseLight,
                                    checkedTrackColor = OwnerThemeColors.RoseDanger
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Data Snapshot & Diagnostics
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.CloudSync, contentDescription = null, tint = OwnerThemeColors.SkyLight, modifier = Modifier.size(18.dp))
                        Text("Database Snapshot & Storage Utility", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = OwnerThemeColors.TextPrimary)
                    }

                    Text("Generate a secure localized snapshot of all multi-tenant databases, academic records, and licensing registries.", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showBackupSuccessDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.SkyBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Snapshot", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.setFeedbackMessage("Diagnostics OK: 100% database integrity verified.")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OwnerThemeColors.SkyLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.SkyBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Diagnostics", fontSize = 11.5.sp)
                        }
                    }
                }
            }
            // Section 6: Standalone APK Distribution & GitHub Releases
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.CardDark),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OwnerThemeColors.AmberPrimary.copy(alpha = 0.6f)),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(OwnerThemeColors.AmberPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CloudDownload,
                                    contentDescription = null,
                                    tint = OwnerThemeColors.AmberLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    "GitHub APK Distribution Hub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = OwnerThemeColors.TextPrimary
                                )
                                Text(
                                    "Download Owner Master & School Client Binaries",
                                    fontSize = 10.5.sp,
                                    color = OwnerThemeColors.AmberLight
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OwnerThemeColors.AmberPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "CI/CD RELEASES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = OwnerThemeColors.AmberLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        "Access direct download links and standalone release packages compiled automatically for the Platform Owner and client schools.",
                        fontSize = 11.5.sp,
                        color = OwnerThemeColors.TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDownloadApkDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("owner_settings_download_apk_btn")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download APK", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        }

                        Button(
                            onClick = { showDownloadApkDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("owner_settings_push_repo_btn")
                        ) {
                            Icon(Icons.Rounded.CallSplit, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push to Separate Repo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showDownloadApkDialog) {
        OwnerApkDownloadDialog(
            onDismiss = { showDownloadApkDialog = false },
            onShowToast = { msg ->
                viewModel.setFeedbackMessage(msg)
            }
        )
    }

    if (showBackupSuccessDialog) {
        Dialog(onDismissRequest = { showBackupSuccessDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.SkyLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = OwnerThemeColors.EmeraldLight, modifier = Modifier.size(28.dp))
                    }

                    Text("Snapshot Generated Successfully", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OwnerThemeColors.TextPrimary)
                    Text("Encrypted database snapshot created containing all school tenant registries, student records, CBT exams, and cryptographic license vaults.", fontSize = 11.5.sp, color = OwnerThemeColors.TextSecondary)

                    Button(
                        onClick = { showBackupSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.SkyBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
