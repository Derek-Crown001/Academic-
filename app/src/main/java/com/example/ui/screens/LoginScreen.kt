package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.OwnerConsoleActivity
import com.example.data.model.SchoolRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: SchoolViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()

    var selectedRole by remember { mutableStateOf(SchoolRole.ADMIN) }
    var isAdminRegisterMode by remember { mutableStateOf(true) }

    // Multi-Tenant Inputs
    var schoolCodeInput by remember { mutableStateOf("") }
    var schoolNameInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var emailOrIdInput by remember { mutableStateOf("") }
    var studentIdInput by remember { mutableStateOf("") }
    var parentContactInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val roleThemeColor = when (selectedRole) {
        SchoolRole.APP_OWNER -> Color(0xFFD97706)
        SchoolRole.ADMIN -> Color(0xFF1E3A8A)
        SchoolRole.TEACHER -> Color(0xFF0F766E)
        SchoolRole.STUDENT -> PrimaryLight
        SchoolRole.PARENT -> Color(0xFF7C3AED)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cloud Sync Indicator & Toggle Bar
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCloudSyncEnabled) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCloudSyncEnabled) Color(0xFF10B981).copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCloudSyncEnabled) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                    contentDescription = "Cloud Status",
                                    tint = if (isCloudSyncEnabled) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isCloudSyncEnabled) "Firebase Online Sync: Active" else "Firebase Sync: Offline Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCloudSyncEnabled) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isCloudSyncEnabled) "Multi-device real-time school database" else "Data preserved locally on device",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isCloudSyncEnabled,
                                onCheckedChange = { viewModel.toggleCloudSync(it) },
                                modifier = Modifier.testTag("toggle_cloud_sync")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // App Header Branding
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(roleThemeColor, AcademicViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AcademiaTrack Portal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Multi-Tenant School Management & Online AI System",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Role Selector
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Select Portal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RoleTabButton(
                                    title = "Admin",
                                    icon = Icons.Rounded.AdminPanelSettings,
                                    isSelected = selectedRole == SchoolRole.ADMIN,
                                    activeColor = Color(0xFF1E3A8A),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedRole = SchoolRole.ADMIN
                                        viewModel.clearAuthError()
                                    }
                                )

                                RoleTabButton(
                                    title = "Teacher",
                                    icon = Icons.Rounded.CoPresent,
                                    isSelected = selectedRole == SchoolRole.TEACHER,
                                    activeColor = Color(0xFF0F766E),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedRole = SchoolRole.TEACHER
                                        viewModel.clearAuthError()
                                    }
                                )

                                RoleTabButton(
                                    title = "Student",
                                    icon = Icons.Rounded.School,
                                    isSelected = selectedRole == SchoolRole.STUDENT,
                                    activeColor = PrimaryLight,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedRole = SchoolRole.STUDENT
                                        viewModel.clearAuthError()
                                    }
                                )

                                RoleTabButton(
                                    title = "Parent",
                                    icon = Icons.Rounded.FamilyRestroom,
                                    isSelected = selectedRole == SchoolRole.PARENT,
                                    activeColor = Color(0xFF7C3AED),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedRole = SchoolRole.PARENT
                                        viewModel.clearAuthError()
                                    }
                                )
                            }
                        }
                    }
                }

                // Error Message Banner
                if (authError != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AcademicRose.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcademicRose.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = AcademicRose,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = authError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AcademicRose,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearAuthError() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Dismiss",
                                        tint = AcademicRose,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Authentication Form Card based on Role
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section Heading
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (selectedRole) {
                                        SchoolRole.ADMIN -> if (isAdminRegisterMode) "Register New School & Admin" else "School Administrator Login"
                                        SchoolRole.TEACHER -> "Teacher Portal Access"
                                        SchoolRole.STUDENT -> "Student Portal Access"
                                        SchoolRole.PARENT, SchoolRole.APP_OWNER -> "Parent / Guardian Access"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (selectedRole == SchoolRole.ADMIN) {
                                    TextButton(
                                        onClick = {
                                            isAdminRegisterMode = !isAdminRegisterMode
                                            viewModel.clearAuthError()
                                        }
                                    ) {
                                        Text(
                                            text = if (isAdminRegisterMode) "Sign In Existing" else "Register School",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = roleThemeColor
                                        )
                                    }
                                }
                            }

                            // Role Description Info
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = roleThemeColor.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (selectedRole) {
                                        SchoolRole.ADMIN -> if (isAdminRegisterMode) "Set up a clean, isolated database for your school. You will receive a School Passkey for your teachers and students." else "Sign in with your School Passkey and Admin credentials."
                                        SchoolRole.TEACHER -> "Enter your School Passkey provided by your Admin, followed by your staff email/ID and security PIN."
                                        SchoolRole.STUDENT -> "Enter your School Passkey, Student Admission Number, and your PIN."
                                        SchoolRole.PARENT, SchoolRole.APP_OWNER -> "Enter your School Passkey, your child's Admission Number, and your phone or PIN to monitor grades and reports."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            // School Passkey Field (Used by all roles)
                            OutlinedTextField(
                                value = schoolCodeInput,
                                onValueChange = { schoolCodeInput = it.uppercase() },
                                label = { Text("School Passkey / Code") },
                                placeholder = { Text("e.g. SCH-KINGS-01") },
                                leadingIcon = { Icon(Icons.Rounded.VpnKey, contentDescription = null, tint = roleThemeColor) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("school_passkey_input")
                            )

                            // Admin Register Specific Fields
                            if (selectedRole == SchoolRole.ADMIN && isAdminRegisterMode) {
                                OutlinedTextField(
                                    value = schoolNameInput,
                                    onValueChange = { schoolNameInput = it },
                                    label = { Text("School Name") },
                                    placeholder = { Text("e.g. Kingsway International College") },
                                    leadingIcon = { Icon(Icons.Rounded.Domain, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("school_name_input")
                                )

                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Administrator / Principal Full Name") },
                                    placeholder = { Text("e.g. Dr. C. Adebayo") },
                                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_name_input")
                                )
                            }

                            // Email or ID input for Admin/Teacher
                            if (selectedRole == SchoolRole.ADMIN || selectedRole == SchoolRole.TEACHER) {
                                OutlinedTextField(
                                    value = emailOrIdInput,
                                    onValueChange = { emailOrIdInput = it },
                                    label = { Text(if (selectedRole == SchoolRole.ADMIN) "Admin Email" else "Teacher Email or Staff ID") },
                                    placeholder = { Text(if (selectedRole == SchoolRole.ADMIN) "principal@school.edu" else "teacher@school.edu or TCH-01") },
                                    leadingIcon = { Icon(Icons.Rounded.AlternateEmail, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("email_or_id_input")
                                )
                            }

                            // Student / Parent specific ID input
                            if (selectedRole == SchoolRole.STUDENT || selectedRole == SchoolRole.PARENT) {
                                OutlinedTextField(
                                    value = studentIdInput,
                                    onValueChange = { studentIdInput = it },
                                    label = { Text(if (selectedRole == SchoolRole.STUDENT) "Student ID / Admission No" else "Child / Ward Admission No") },
                                    placeholder = { Text("e.g. STU-2025-042 or 2025-042") },
                                    leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("student_id_input")
                                )
                            }

                            if (selectedRole == SchoolRole.PARENT) {
                                OutlinedTextField(
                                    value = parentContactInput,
                                    onValueChange = { parentContactInput = it },
                                    label = { Text("Parent Phone or Email (Optional)") },
                                    placeholder = { Text("+234 800 000 0000 or parent@gmail.com") },
                                    leadingIcon = { Icon(Icons.Rounded.ContactPhone, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("parent_contact_input")
                                )
                            }

                            // Password / PIN Field
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = {
                                    Text(
                                        when (selectedRole) {
                                            SchoolRole.ADMIN -> "Admin Password"
                                            SchoolRole.TEACHER -> "Teacher Passcode / PIN (e.g. teach123)"
                                            SchoolRole.STUDENT -> "Student Passcode / PIN (e.g. 1234)"
                                            SchoolRole.PARENT, SchoolRole.APP_OWNER -> "Security PIN (e.g. 1234)"
                                        }
                                    )
                                },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = if (passwordVisible) "Hide" else "Show"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    executeLogin(
                                        role = selectedRole,
                                        isAdminRegister = isAdminRegisterMode,
                                        schoolName = schoolNameInput,
                                        schoolCode = schoolCodeInput,
                                        adminName = nameInput,
                                        emailOrId = emailOrIdInput,
                                        studentId = studentIdInput,
                                        parentContact = parentContactInput,
                                        password = passwordInput,
                                        viewModel = viewModel,
                                        onSuccess = onLoginSuccess
                                    )
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    executeLogin(
                                        role = selectedRole,
                                        isAdminRegister = isAdminRegisterMode,
                                        schoolName = schoolNameInput,
                                        schoolCode = schoolCodeInput,
                                        adminName = nameInput,
                                        emailOrId = emailOrIdInput,
                                        studentId = studentIdInput,
                                        parentContact = parentContactInput,
                                        password = passwordInput,
                                        viewModel = viewModel,
                                        onSuccess = onLoginSuccess
                                    )
                                },
                                enabled = !isAuthLoading && schoolCodeInput.isNotBlank() && passwordInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = roleThemeColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_submit_button")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = when (selectedRole) {
                                                SchoolRole.ADMIN -> if (isAdminRegisterMode) "Register School & Enter Admin Portal" else "Enter Admin Portal"
                                                SchoolRole.TEACHER -> "Enter Teacher Portal"
                                                SchoolRole.STUDENT -> "Enter Student Portal"
                                                SchoolRole.PARENT, SchoolRole.APP_OWNER -> "Enter Parent Portal"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
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
    }
}

private fun executeLogin(
    role: SchoolRole,
    isAdminRegister: Boolean,
    schoolName: String,
    schoolCode: String,
    adminName: String,
    emailOrId: String,
    studentId: String,
    parentContact: String,
    password: String,
    viewModel: SchoolViewModel,
    onSuccess: () -> Unit
) {
    when (role) {
        SchoolRole.APP_OWNER -> {
            val success = viewModel.selectPortal(SchoolRole.APP_OWNER, pin = password)
            if (success) {
                onSuccess()
            }
        }
        SchoolRole.ADMIN -> {
            if (isAdminRegister) {
                viewModel.registerNewSchoolAndAdmin(
                    schoolName = schoolName,
                    schoolCode = schoolCode,
                    adminName = adminName,
                    adminEmail = emailOrId,
                    adminPasscode = password,
                    onSuccess = onSuccess
                )
            } else {
                viewModel.signInAdmin(
                    schoolCode = schoolCode,
                    emailOrId = emailOrId,
                    passcode = password,
                    onSuccess = onSuccess
                )
            }
        }
        SchoolRole.TEACHER -> {
            viewModel.signInTeacher(
                schoolCode = schoolCode,
                emailOrId = emailOrId,
                passcode = password,
                onSuccess = onSuccess
            )
        }
        SchoolRole.STUDENT -> {
            viewModel.signInStudent(
                schoolCode = schoolCode,
                studentAdmissionId = studentId,
                passcode = password,
                onSuccess = onSuccess
            )
        }
        SchoolRole.PARENT -> {
            viewModel.signInParent(
                schoolCode = schoolCode,
                studentAdmissionId = studentId,
                parentContact = parentContact,
                passcode = password,
                onSuccess = onSuccess
            )
        }
    }
}

@Composable
fun RoleTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("role_tab_${title.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
