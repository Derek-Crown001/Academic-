package com.example.owner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppOwnerLicenseConfig
import com.example.data.model.SubscriptionTier
import com.example.owner.ui.components.OwnerStatPill
import com.example.owner.ui.components.OwnerThemeColors
import com.example.ui.viewmodel.SchoolViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolProvisioningScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val allLicenseConfigs by viewModel.allLicenseConfigs.collectAsState()
    val selectedSchoolConfig by viewModel.selectedOwnerSchoolConfig.collectAsState()

    var filterTab by remember { mutableStateOf("ALL") } // ALL, ACTIVE, DUE, LOCKED
    var showProvisionDialog by remember { mutableStateOf(false) }
    var schoolToDelete by remember { mutableStateOf<AppOwnerLicenseConfig?>(null) }

    val filteredSchools = remember(allLicenseConfigs, filterTab) {
        allLicenseConfigs.filter { config ->
            when (filterTab) {
                "ACTIVE" -> !config.isAppLocked && !config.isOverdue
                "DUE" -> config.isDueSoon || (config.daysRemaining <= 14 && !config.isAppLocked)
                "LOCKED" -> config.isAppLocked || config.isOverdue
                else -> true
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OwnerThemeColors.BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Overview Banner & Provision New Tenant CTA
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.AmberPrimary.copy(alpha = 0.6f)),
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
                        Column {
                            Text(
                                text = "Tenant Fleet & Provisioning",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = OwnerThemeColors.TextPrimary
                            )
                            Text(
                                text = "Multi-School Onboarding & Database Segregation",
                                fontSize = 11.5.sp,
                                color = OwnerThemeColors.TextSecondary
                            )
                        }

                        Button(
                            onClick = { showProvisionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_provision_school_btn")
                        ) {
                            Icon(Icons.Rounded.AddBusiness, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Provision School", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                    }

                    // Fleet Stat Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OwnerStatPill(
                            icon = Icons.Rounded.School,
                            label = "Total Tenants",
                            value = "${allLicenseConfigs.size}",
                            accentColor = OwnerThemeColors.SkyLight,
                            modifier = Modifier.weight(1f)
                        )
                        OwnerStatPill(
                            icon = Icons.Rounded.CheckCircle,
                            label = "Active Subs",
                            value = "${allLicenseConfigs.count { !it.isAppLocked && !it.isOverdue }}",
                            accentColor = OwnerThemeColors.EmeraldLight,
                            modifier = Modifier.weight(1f)
                        )
                        OwnerStatPill(
                            icon = Icons.Rounded.Lock,
                            label = "Locked/Arrears",
                            value = "${allLicenseConfigs.count { it.isAppLocked || it.isOverdue }}",
                            accentColor = OwnerThemeColors.RoseLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Filter Tab Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All (${allLicenseConfigs.size})",
                    "ACTIVE" to "Active (${allLicenseConfigs.count { !it.isAppLocked && !it.isOverdue }})",
                    "DUE" to "Due Soon (${allLicenseConfigs.count { it.isDueSoon }})",
                    "LOCKED" to "Locked (${allLicenseConfigs.count { it.isAppLocked || it.isOverdue }})"
                ).forEach { (key, label) ->
                    val isSelected = filterTab == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OwnerThemeColors.AmberPrimary else OwnerThemeColors.CardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) OwnerThemeColors.AmberLight else OwnerThemeColors.CardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { filterTab = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else OwnerThemeColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Schools List
        if (filteredSchools.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OwnerThemeColors.CardDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.SearchOff, contentDescription = null, tint = OwnerThemeColors.TextMuted, modifier = Modifier.size(40.dp))
                        Text("No school tenants match your filter criteria.", color = OwnerThemeColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredSchools) { school ->
                val isSelected = school.schoolCode == selectedSchoolConfig.schoolCode

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = OwnerThemeColors.CardDark),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) OwnerThemeColors.AmberLight
                        else if (school.isAppLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.5f)
                        else OwnerThemeColors.CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.switchOwnerSelectedSchool(school.schoolCode) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // School Name & Status Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (school.isAppLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.2f) else OwnerThemeColors.SkyBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.AccountBalance,
                                        contentDescription = null,
                                        tint = if (school.isAppLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.SkyLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = school.schoolName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = OwnerThemeColors.TextPrimary
                                    )
                                    Text(
                                        text = "Code: ${school.schoolCode} • ${school.schoolCity}",
                                        fontSize = 11.sp,
                                        color = OwnerThemeColors.TextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (school.isAppLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.25f)
                                else if (school.isDueSoon) OwnerThemeColors.AmberPrimary.copy(alpha = 0.25f)
                                else OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = school.statusBadgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (school.isAppLocked) OwnerThemeColors.RoseLight
                                    else if (school.isDueSoon) OwnerThemeColors.AmberLight
                                    else OwnerThemeColors.EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Principal: ${school.principalName}", fontSize = 11.sp, color = OwnerThemeColors.TextSecondary)
                            Text("Tier: ${school.subscriptionTier.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OwnerThemeColors.AmberLight)
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Quick Lock/Unlock
                            Button(
                                onClick = {
                                    viewModel.toggleMasterAppLock(
                                        schoolCode = school.schoolCode,
                                        isLocked = !school.isAppLocked,
                                        customReason = if (!school.isAppLocked) "School system has been restricted by the platform owner." else "Subscription is in good standing."
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (school.isAppLocked) OwnerThemeColors.EmeraldSuccess else OwnerThemeColors.RoseDanger
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (school.isAppLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (school.isAppLocked) "Unlock" else "Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Grant 1 Year
                            Button(
                                onClick = {
                                    viewModel.grantDirectAccess(
                                        schoolCode = school.schoolCode,
                                        tier = SubscriptionTier.ANNUAL,
                                        durationDays = 365
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 Year", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            // Delete Tenant Button
                            IconButton(
                                onClick = { schoolToDelete = school },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = OwnerThemeColors.RoseDanger, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Provision New School Dialog Wizard
    if (showProvisionDialog) {
        var schoolNameInput by remember { mutableStateOf("") }
        var schoolCodeInput by remember { mutableStateOf("") }
        var principalNameInput by remember { mutableStateOf("") }
        var cityInput by remember { mutableStateOf("Lagos") }
        var selectedTier by remember { mutableStateOf(SubscriptionTier.ANNUAL) }
        var annualFeeInput by remember { mutableStateOf("400000") }
        var termlyFeeInput by remember { mutableStateOf("150000") }

        Dialog(onDismissRequest = { showProvisionDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OwnerThemeColors.AmberPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Provision New School Tenant", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OwnerThemeColors.TextPrimary)
                        Text("Register a new institution with dedicated SaaS license parameters.", fontSize = 11.5.sp, color = OwnerThemeColors.TextSecondary)
                    }

                    item {
                        OutlinedTextField(
                            value = schoolNameInput,
                            onValueChange = {
                                schoolNameInput = it
                                if (schoolCodeInput.isBlank() && it.isNotBlank()) {
                                    val acronym = it.split(" ").filter { word -> word.isNotBlank() }.map { word -> word.first().uppercase() }.joinToString("")
                                    schoolCodeInput = "SCH-$acronym-01"
                                }
                            },
                            label = { Text("School Name") },
                            placeholder = { Text("e.g. St. Jude's International College") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.AmberPrimary,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = schoolCodeInput,
                            onValueChange = { schoolCodeInput = it.uppercase() },
                            label = { Text("Unique School Code") },
                            placeholder = { Text("e.g. SCH-STJUDE-01") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.AmberPrimary,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = principalNameInput,
                            onValueChange = { principalNameInput = it },
                            label = { Text("Head of School / Principal Name") },
                            placeholder = { Text("e.g. Mrs. Grace Eze") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.AmberPrimary,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("City / State") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OwnerThemeColors.AmberPrimary,
                                unfocusedBorderColor = OwnerThemeColors.CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("Initial Subscription Plan:", fontSize = 12.sp, color = OwnerThemeColors.TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(SubscriptionTier.TRIAL, SubscriptionTier.TERMLY, SubscriptionTier.ANNUAL).forEach { tier ->
                                val isSelected = selectedTier == tier
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) OwnerThemeColors.AmberPrimary else OwnerThemeColors.SurfaceDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OwnerThemeColors.AmberLight else OwnerThemeColors.CardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTier = tier }
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
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { showProvisionDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = OwnerThemeColors.TextSecondary)
                            }

                            Button(
                                onClick = {
                                    if (schoolNameInput.isNotBlank() && schoolCodeInput.isNotBlank()) {
                                        val durationDays = when (selectedTier) {
                                            SubscriptionTier.TRIAL -> 30
                                            SubscriptionTier.TERMLY -> 120
                                            SubscriptionTier.ANNUAL -> 365
                                            SubscriptionTier.LIFETIME -> 3650
                                            else -> 365
                                        }
                                        viewModel.addNewSchoolByOwner(
                                            schoolName = schoolNameInput,
                                            schoolCode = schoolCodeInput,
                                            principalName = principalNameInput,
                                            city = cityInput,
                                            tier = selectedTier,
                                            durationDays = durationDays,
                                            feeYear = annualFeeInput.toDoubleOrNull() ?: 400000.0,
                                            feeTerm = termlyFeeInput.toDoubleOrNull() ?: 150000.0
                                        )
                                        showProvisionDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.AmberPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Provision", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    schoolToDelete?.let { target ->
        Dialog(onDismissRequest = { schoolToDelete = null }) {
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
                    Text("Delete School Tenant?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OwnerThemeColors.RoseLight)
                    Text("Are you sure you want to remove '${target.schoolName}' (${target.schoolCode}) from the owner registry? This action is irreversible.", fontSize = 12.sp, color = OwnerThemeColors.TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { schoolToDelete = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = OwnerThemeColors.TextSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.deleteSchoolByOwner(target.schoolCode)
                                schoolToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.RoseDanger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete Tenant", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
