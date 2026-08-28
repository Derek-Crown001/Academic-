package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.GradeBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminSchoolSettingsContent(
    schoolProfile: SchoolProfile,
    onSaveProfile: (SchoolProfile) -> Unit,
    onClearDemoLogs: () -> Unit = {}
) {
    var schoolCode by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolCode) }
    var schoolName by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolName) }
    var schoolMotto by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolMotto) }
    var address by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolAddress) }
    var academicSession by remember(schoolProfile) { mutableStateOf(schoolProfile.academicSession) }
    var currentTerm by remember(schoolProfile) { mutableStateOf(schoolProfile.currentTerm) }
    var principalName by remember(schoolProfile) { mutableStateOf(schoolProfile.principalName) }
    var contactPhone by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolPhone) }
    var contactEmail by remember(schoolProfile) { mutableStateOf(schoolProfile.schoolEmail) }
    var showClearLogsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Institution Profile & Branding",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Multipurpose school identity configuration",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Icon(
                            Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Customize your secondary school's official name, motto, unique School ID code, academic term, and principal signature. Stamped dynamically on all generated student report sheets and official PDF exports.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Multi-Tenant Isolation Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Rounded.Security,
                        contentDescription = "Security Rules",
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Firestore School Isolation Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F766E)
                        )
                        Text(
                            text = "Database rules enforce strict multi-tenancy under path /schools/${schoolCode.ifBlank { "YOUR-SCHOOL-CODE" }}/. Different schools cannot query or access each other's students, teachers, or exam records.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Letterhead Live Preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LIVE REPORT SHEET HEADER PREVIEW",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = schoolName.ifBlank { "KINGSWAY MODEL INTERNATIONAL COLLEGE" }.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = "ID: ${schoolCode.ifBlank { "SCH-KINGSWAY-01" }} • Motto: ${schoolMotto.ifBlank { "Excellence in Character, Leadership, and Academic Distinction" }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${address.ifBlank { "Victoria Island, Lagos" }} • Session: $academicSession ($currentTerm)",
                        fontSize = 10.5.sp,
                        color = PrimaryLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Principal: $principalName",
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Form Fields
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "School Information Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = schoolCode,
                        onValueChange = { schoolCode = it.uppercase() },
                        label = { Text("School ID Code (Tenant Key)") },
                        supportingText = { Text("Unique identifier for Firebase database partitioning") },
                        leadingIcon = { Icon(Icons.Rounded.VpnKey, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_school_code"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("School Name") },
                        leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_school_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = schoolMotto,
                        onValueChange = { schoolMotto = it },
                        label = { Text("School Motto") },
                        leadingIcon = { Icon(Icons.Rounded.FormatQuote, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_school_motto"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Campus Address") },
                        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = academicSession,
                            onValueChange = { academicSession = it },
                            label = { Text("Academic Session") },
                            leadingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = currentTerm,
                            onValueChange = { currentTerm = it },
                            label = { Text("Current Term") },
                            leadingIcon = { Icon(Icons.Rounded.Timelapse, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = principalName,
                        onValueChange = { principalName = it },
                        label = { Text("Principal / Head of School") },
                        leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Official Phone") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Official Email") },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val updated = schoolProfile.copy(
                                schoolCode = schoolCode.trim().ifBlank { "SCH-KINGSWAY-01" },
                                schoolName = schoolName.trim(),
                                schoolMotto = schoolMotto.trim(),
                                schoolAddress = address.trim(),
                                academicSession = academicSession.trim(),
                                currentTerm = currentTerm.trim(),
                                principalName = principalName.trim(),
                                schoolPhone = contactPhone.trim(),
                                schoolEmail = contactEmail.trim()
                            )
                            onSaveProfile(updated)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_school_profile_button")
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Apply School Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Clean Slate / Clear Demo Logs Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = "Clear Logs",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Clear All Demo Logs & Activity Records",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Removes dummy chats, test attendance logs, and sample exam trials for a clean operational start.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showClearLogsDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("clear_demo_logs_button")
                    ) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Demo Logs & History", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All Demo Logs?") },
            text = {
                Text("This will purge all demo chat messages, past clock-in records, and test submissions from both your local database and connected Firestore room logs. Your configured classes, subjects, and student roster will remain intact.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearDemoLogs()
                        showClearLogsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Clear All Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffAttendanceContent(
    attendances: List<TeacherAttendance>,
    teachers: List<SchoolUser>,
    onRegisterTeacher: (name: String, staffId: String, email: String, passcode: String, assignedClass: String, assignedSubjects: String, phone: String, qualification: String, gender: String) -> Unit,
    onDeleteTeacher: (SchoolUser) -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val displayDateStr = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date()) }

    var showRegisterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDutyFilter by remember { mutableStateOf("ALL") } // ALL, ON_DUTY, OFF_DUTY
    var teacherToDelete by remember { mutableStateOf<SchoolUser?>(null) }
    var selectedTeacherToViewPasskey by remember { mutableStateOf<SchoolUser?>(null) }

    val todayAttendances = attendances.filter { it.dateString == todayDateStr }
    val clockedInCount = todayAttendances.count { it.status == "CLOCKED_IN" }
    val clockedOutCount = todayAttendances.count { it.status == "CLOCKED_OUT" }

    val filteredTeachers = teachers.filter { teacher ->
        val attendance = todayAttendances.find { it.teacherId == teacher.id }
        val matchesSearch = searchQuery.isBlank() ||
                teacher.name.contains(searchQuery, ignoreCase = true) ||
                teacher.id.contains(searchQuery, ignoreCase = true) ||
                teacher.email.contains(searchQuery, ignoreCase = true) ||
                teacher.assignedSubjects.contains(searchQuery, ignoreCase = true) ||
                teacher.className.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedDutyFilter) {
            "ON_DUTY" -> attendance?.status == "CLOCKED_IN"
            "OFF_DUTY" -> attendance?.status != "CLOCKED_IN"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Faculty Directory & Attendance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = displayDateStr,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_register_teacher_button")
                        ) {
                            Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register Teacher", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }

                    // Stat row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$clockedInCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF047857))
                                Text(text = "Currently On Duty", fontSize = 11.sp, color = Color(0xFF047857))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF64748B).copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$clockedOutCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF334155))
                                Text(text = "Completed Shift", fontSize = 11.sp, color = Color(0xFF334155))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryLight.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${teachers.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryLight)
                                Text(text = "Total Faculty", fontSize = 11.sp, color = PrimaryLight)
                            }
                        }
                    }
                }
            }
        }

        // Search & Filter row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search faculty by name, ID, subject, or email") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("search_teachers_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedDutyFilter == "ALL",
                        onClick = { selectedDutyFilter = "ALL" },
                        label = { Text("All Faculty (${teachers.size})") }
                    )
                    FilterChip(
                        selected = selectedDutyFilter == "ON_DUTY",
                        onClick = { selectedDutyFilter = "ON_DUTY" },
                        label = { Text("On Duty ($clockedInCount)") }
                    )
                    FilterChip(
                        selected = selectedDutyFilter == "OFF_DUTY",
                        onClick = { selectedDutyFilter = "OFF_DUTY" },
                        label = { Text("Off Duty (${teachers.size - clockedInCount})") }
                    )
                }
            }
        }

        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registered Teachers & Passkeys (${filteredTeachers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        if (filteredTeachers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(40.dp), tint = PrimaryLight)
                        Text("No teachers found", fontWeight = FontWeight.Bold)
                        Text(
                            "Tap 'Register Teacher' above to add a new teacher with their secure login passkey.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTeachers, key = { it.id }) { teacher ->
                val attendance = attendances.find { it.teacherId == teacher.id && it.dateString == todayDateStr }
                    ?: attendances.find { it.teacherId == teacher.id }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (attendance?.status) {
                                                "CLOCKED_IN" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                "CLOCKED_OUT" -> Color(0xFF64748B).copy(alpha = 0.2f)
                                                else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (attendance?.status) {
                                            "CLOCKED_IN" -> Icons.Rounded.CheckCircle
                                            "CLOCKED_OUT" -> Icons.Rounded.Schedule
                                            else -> Icons.Rounded.PersonOff
                                        },
                                        contentDescription = null,
                                        tint = when (attendance?.status) {
                                            "CLOCKED_IN" -> Color(0xFF047857)
                                            "CLOCKED_OUT" -> Color(0xFF334155)
                                            else -> Color(0xFFD97706)
                                        }
                                    )
                                }

                                Column {
                                    Text(text = teacher.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "ID: ${teacher.id} • ${teacher.email}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (teacher.assignedSubjects.isNotBlank() || teacher.className.isNotBlank()) {
                                        Text(
                                            text = "Subject/Class: ${teacher.assignedSubjects.ifBlank { "Unassigned" }} ${if (teacher.className.isNotBlank()) "(${teacher.className})" else ""}",
                                            fontSize = 11.5.sp,
                                            color = PrimaryLight,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (attendance?.status) {
                                    "CLOCKED_IN" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    "CLOCKED_OUT" -> Color(0xFF64748B).copy(alpha = 0.15f)
                                    else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = when (attendance?.status) {
                                        "CLOCKED_IN" -> "ON DUTY"
                                        "CLOCKED_OUT" -> "ENDED SHIFT"
                                        else -> "OFF DUTY"
                                    },
                                    color = when (attendance?.status) {
                                        "CLOCKED_IN" -> Color(0xFF047857)
                                        "CLOCKED_OUT" -> Color(0xFF334155)
                                        else -> Color(0xFFB45309)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Passkey and Credentials Card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.VpnKey, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "Teacher Login Passkey: ",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = teacher.passcode.ifBlank { "teach123" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryLight
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { selectedTeacherToViewPasskey = teacher },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.Info, contentDescription = "View Details", tint = PrimaryLight, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { teacherToDelete = teacher },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove Teacher", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Register Teacher Dialog
    if (showRegisterDialog) {
        RegisterTeacherDialog(
            onDismiss = { showRegisterDialog = false },
            onConfirm = { name, staffId, email, passcode, assignedClass, assignedSubjects, phone, qualification, gender ->
                onRegisterTeacher(name, staffId, email, passcode, assignedClass, assignedSubjects, phone, qualification, gender)
                showRegisterDialog = false
            }
        )
    }

    // 2. View Details / Credentials Dialog
    selectedTeacherToViewPasskey?.let { teacher ->
        AlertDialog(
            onDismissRequest = { selectedTeacherToViewPasskey = null },
            icon = { Icon(Icons.Rounded.Badge, contentDescription = null, tint = PrimaryLight) },
            title = { Text("Teacher Portal Login Credentials") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Teacher Name: ${teacher.name}", fontWeight = FontWeight.Bold)
                    Text(text = "Staff ID: ${teacher.id}")
                    Text(text = "Login Email: ${teacher.email}")
                    Text(text = "Assigned Class: ${teacher.className.ifBlank { "None" }}")
                    Text(text = "Subjects: ${teacher.assignedSubjects.ifBlank { "None" }}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Surface(
                        color = PrimaryLight.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Unique Login Passkey (PIN):", fontSize = 11.5.sp, color = PrimaryLight, fontWeight = FontWeight.Bold)
                            Text(
                                text = teacher.passcode.ifBlank { "teach123" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryLight
                            )
                            Text(
                                text = "Teacher uses this Passkey along with the School Passkey to log in to the Teacher Portal.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedTeacherToViewPasskey = null }) {
                    Text("Done")
                }
            }
        )
    }

    // 3. Delete Confirmation Dialog
    teacherToDelete?.let { teacher ->
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("Remove Teacher '${teacher.name}'?") },
            text = {
                Text("Are you sure you want to remove ${teacher.name} (Staff ID: ${teacher.id}) from the faculty directory? They will no longer be able to log in to the Teacher Portal.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTeacher(teacher)
                        teacherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Teacher")
                }
            },
            dismissButton = {
                TextButton(onClick = { teacherToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTeacherDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        staffId: String,
        email: String,
        passcode: String,
        assignedClass: String,
        assignedSubjects: String,
        phone: String,
        qualification: String,
        gender: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("TCH-2025-0${(10..99).random()}") }
    var email by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("teach${(100..999).random()}") }
    var assignedClass by remember { mutableStateOf("SS 2 Gold") }
    var assignedSubjects by remember { mutableStateOf("Mathematics, Further Math") }
    var phone by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("B.Sc (Ed) / PGDE") }
    var gender by remember { mutableStateOf("Male") }
    var showPasskey by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = PrimaryLight)
                Text("Register Teacher & Set Passkey", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Create a teacher profile with a unique login passkey for secure portal access.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (email.isBlank() && it.isNotBlank()) {
                                email = "${it.trim().lowercase().replace(" ", ".")}@school.edu.ng"
                            }
                        },
                        label = { Text("Teacher Full Name (e.g. Dr. Jane Okonkwo)") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("register_teacher_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = staffId,
                        onValueChange = { staffId = it },
                        label = { Text("Staff ID / Number") },
                        leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("register_teacher_id_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Login Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("register_teacher_email_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { passcode = it },
                        label = { Text("Teacher Login Passkey / PIN") },
                        supportingText = { Text("Unique passkey the teacher will use to sign in") },
                        leadingIcon = { Icon(Icons.Rounded.VpnKey, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passcode = "teach${(100..999).random()}" }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Generate New Passkey")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("register_teacher_passcode_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = assignedSubjects,
                        onValueChange = { assignedSubjects = it },
                        label = { Text("Assigned Subjects (e.g. Chemistry, Physics)") },
                        leadingIcon = { Icon(Icons.Rounded.Subject, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = assignedClass,
                        onValueChange = { assignedClass = it },
                        label = { Text("Assigned Class Arm (e.g. SS 2 Gold)") },
                        leadingIcon = { Icon(Icons.Rounded.MeetingRoom, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (Optional)") },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = qualification,
                        onValueChange = { qualification = it },
                        label = { Text("Highest Qualification (e.g. B.Ed, M.Sc)") },
                        leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Gender:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Male", "Female").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && passcode.isNotBlank()) {
                        onConfirm(name, staffId, email, passcode, assignedClass, assignedSubjects, phone, qualification, gender)
                    }
                },
                modifier = Modifier.testTag("submit_register_teacher_button")
            ) {
                Text("Register Teacher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminReportCardsApprovalContent(
    reportCards: List<ReportCard>,
    onApproveReportCard: (Long, Boolean) -> Unit,
    onBulkApproveClass: (String) -> Unit,
    onExportPdf: (ReportCard) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filteredCards = when (selectedFilter) {
        "PENDING" -> reportCards.filter { it.isPublishedByTeacher && !it.isApprovedByAdmin }
        "APPROVED" -> reportCards.filter { it.isApprovedByAdmin }
        "DRAFT" -> reportCards.filter { !it.isPublishedByTeacher }
        else -> reportCards
    }

    val pendingCount = reportCards.count { it.isPublishedByTeacher && !it.isApprovedByAdmin }
    val approvedCount = reportCards.count { it.isApprovedByAdmin }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Report Sheet Seal & Approval Queue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Class Teacher Publish → Admin Approval → Parent Release",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (pendingCount > 0) {
                            Button(
                                onClick = { onBulkApproveClass("SS 2 Gold") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve All Pending")
                            }
                        }
                    }

                    // Stat Badges
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${reportCards.size})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "PENDING",
                            onClick = { selectedFilter = "PENDING" },
                            label = { Text("Awaiting Seal ($pendingCount)") }
                        )
                        FilterChip(
                            selected = selectedFilter == "APPROVED",
                            onClick = { selectedFilter = "APPROVED" },
                            label = { Text("Approved ($approvedCount)") }
                        )
                    }
                }
            }
        }

        items(filteredCards) { card ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = card.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "${card.className} • ${card.term} • ${card.session}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Approval Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                card.isApprovedByAdmin -> Color(0xFF10B981).copy(alpha = 0.15f)
                                card.isPublishedByTeacher -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when {
                                    card.isApprovedByAdmin -> "OFFICIALLY APPROVED"
                                    card.isPublishedByTeacher -> "PUBLISHED BY TEACHER"
                                    else -> "TEACHER DRAFT"
                                },
                                color = when {
                                    card.isApprovedByAdmin -> Color(0xFF047857)
                                    card.isPublishedByTeacher -> Color(0xFF1D4ED8)
                                    else -> Color(0xFFB45309)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Academic Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Score: ${card.totalScore.toInt()}/${card.maxPossibleScore.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Average: ${String.format(Locale.US, "%.1f", card.averageScore)}%",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLight,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Rank: ${card.classPosition}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Attendance: ${card.attendancePresent}/${card.attendanceTotal}",
                            fontSize = 12.sp
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onExportPdf(card) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", fontSize = 12.sp)
                        }

                        if (!card.isApprovedByAdmin) {
                            Button(
                                onClick = { onApproveReportCard(card.id, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Seal & Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onApproveReportCard(card.id, false) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Rounded.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Revoke Approval", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
