package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.SchoolRole
import com.example.data.model.SchoolUser
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

    val allUsers by viewModel.allUsers.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()

    var selectedRole by remember { mutableStateOf(SchoolRole.STUDENT) }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Form inputs
    var emailInput by remember { mutableStateOf("chidinma.nwosu@kingsway.edu") }
    var passwordInput by remember { mutableStateOf("1234") }
    var fullNameInput by remember { mutableStateOf("") }
    var classNameInput by remember { mutableStateOf("SS 2 Gold") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Dialogs
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    val roleThemeColor = when (selectedRole) {
        SchoolRole.ADMIN -> Color(0xFF1E3A8A)
        SchoolRole.TEACHER -> Color(0xFF0F766E)
        SchoolRole.STUDENT -> PrimaryLight
        SchoolRole.PARENT -> Color(0xFF7C3AED)
    }

    // Auto-populate sample email based on role selection for convenience
    LaunchedEffect(selectedRole) {
        if (!isSignUpMode) {
            when (selectedRole) {
                SchoolRole.ADMIN -> {
                    emailInput = "admin@kingswayacademy.edu"
                    passwordInput = "admin123"
                }
                SchoolRole.TEACHER -> {
                    emailInput = "david.okon@kingswayacademy.edu"
                    passwordInput = "teach123"
                }
                SchoolRole.STUDENT -> {
                    emailInput = "chidinma.nwosu@kingsway.edu"
                    passwordInput = "1234"
                }
                SchoolRole.PARENT -> {
                    emailInput = "ngozi.nwosu@gmail.com"
                    passwordInput = "1234"
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // App Hero / Branding Banner
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
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
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AcademiaTrack",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Secondary School Management & Academic Portal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Role Selector Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Your Academic Role",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                        }
                    }
                }
            }

            // Role Description Badge
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = roleThemeColor.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, roleThemeColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedRole) {
                                SchoolRole.ADMIN -> Icons.Rounded.Security
                                SchoolRole.TEACHER -> Icons.Rounded.Badge
                                SchoolRole.STUDENT -> Icons.Rounded.AutoAwesome
                                SchoolRole.PARENT -> Icons.Rounded.Visibility
                            },
                            contentDescription = null,
                            tint = roleThemeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when (selectedRole) {
                                SchoolRole.ADMIN -> "Full administrative control: broad registers, fees, classes, faculty time-clock & broad settings."
                                SchoolRole.TEACHER -> "Faculty Corner: continuous assessment grading, CBT exam builder & broad registers."
                                SchoolRole.STUDENT -> "Student Portal: take CBT exams, track report cards, GPA & assignments."
                                SchoolRole.PARENT -> "Guardian Portal: monitor ward's academic standing, attendance & report cards."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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

            // Primary Authentication Form Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isSignUpMode) "Create ${selectedRole.name.lowercase().replaceFirstChar { it.uppercase() }} Account" else "Sign In with Firebase Auth",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = fullNameInput,
                                onValueChange = { fullNameInput = it },
                                label = { Text("Full Legal Name") },
                                placeholder = { Text("e.g. Samuel Adeleke") },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = FocusDirection.Down.let { ImeAction.Next }),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )

                            if (selectedRole == SchoolRole.STUDENT) {
                                OutlinedTextField(
                                    value = classNameInput,
                                    onValueChange = { classNameInput = it },
                                    label = { Text("Class / Arm") },
                                    placeholder = { Text("e.g. SS 2 Gold") },
                                    leadingIcon = { Icon(Icons.Rounded.MeetingRoom, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_class_input")
                                )
                            }
                        }

                        // Email / Registration ID Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(if (selectedRole == SchoolRole.STUDENT) "Student Email / Reg No" else "Official School Email") },
                            placeholder = { Text("user@kingsway.edu") },
                            leadingIcon = { Icon(Icons.Rounded.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        // Password / PIN Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(if (selectedRole == SchoolRole.ADMIN || selectedRole == SchoolRole.TEACHER) "Security Passcode / PIN" else "Password") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (selectedRole == SchoolRole.ADMIN || selectedRole == SchoolRole.TEACHER) KeyboardType.Password else KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                if (isSignUpMode) {
                                    viewModel.signUpWithFirebase(
                                        name = fullNameInput.ifBlank { "User ${emailInput.take(5)}" },
                                        email = emailInput,
                                        passcode = passwordInput,
                                        role = selectedRole,
                                        className = if (selectedRole == SchoolRole.STUDENT) classNameInput else null,
                                        onSuccess = onLoginSuccess
                                    )
                                } else {
                                    viewModel.signInWithFirebase(
                                        email = emailInput,
                                        passcode = passwordInput,
                                        targetRole = selectedRole,
                                        onSuccess = onLoginSuccess
                                    )
                                }
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (!isSignUpMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        resetEmailInput = emailInput
                                        showForgotPasswordDialog = true
                                    }
                                ) {
                                    Text("Forgot Password?", fontSize = 12.sp, color = roleThemeColor)
                                }
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (isSignUpMode) {
                                    viewModel.signUpWithFirebase(
                                        name = fullNameInput.ifBlank { "User ${emailInput.take(5)}" },
                                        email = emailInput,
                                        passcode = passwordInput,
                                        role = selectedRole,
                                        className = if (selectedRole == SchoolRole.STUDENT) classNameInput else null,
                                        onSuccess = onLoginSuccess
                                    )
                                } else {
                                    viewModel.signInWithFirebase(
                                        email = emailInput,
                                        passcode = passwordInput,
                                        targetRole = selectedRole,
                                        onSuccess = onLoginSuccess
                                    )
                                }
                            },
                            enabled = !isAuthLoading && emailInput.isNotBlank() && passwordInput.isNotBlank(),
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
                                        text = if (isSignUpMode) "Create Account" else "Access Portal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Google Sign-In Option
                        OutlinedButton(
                            onClick = {
                                viewModel.signInWithGoogle(context, onSuccess = onLoginSuccess)
                            },
                            enabled = !isAuthLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_signin_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = PrimaryLight
                                )
                                Text(
                                    text = "Continue with Google Sign-In",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        // Mode toggle (Sign In vs Sign Up)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSignUpMode) "Already have an account?" else "New user?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    isSignUpMode = !isSignUpMode
                                    viewModel.clearAuthError()
                                }
                            ) {
                                Text(
                                    text = if (isSignUpMode) "Sign In" else "Register Profile",
                                    fontWeight = FontWeight.Bold,
                                    color = roleThemeColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick One-Tap Role Demo Access Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = AcademicAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Instant One-Tap Demo Access",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Test each partitioned role instantly with preconfigured accounts:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick demo accounts
                        val demoAccounts = listOf(
                            Triple(SchoolRole.STUDENT, "Chidinma Nwosu (SS 2 Gold)", "STU-2025-042"),
                            Triple(SchoolRole.PARENT, "Mrs. Ngozi Nwosu (Parent of Chidinma)", "PAR-2025-001"),
                            Triple(SchoolRole.TEACHER, "Mr. David Okon (Physics & Maths)", "TCH-2024-001"),
                            Triple(SchoolRole.ADMIN, "Dr. C. Adebayo (Principal / Admin)", "ADM-2024-001")
                        )

                        demoAccounts.forEach { (role, title, id) ->
                            val userMatch = allUsers.find { it.id == id } ?: allUsers.find { it.role == role }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (role) {
                                        SchoolRole.ADMIN -> Color(0xFF1E3A8A).copy(alpha = 0.2f)
                                        SchoolRole.TEACHER -> Color(0xFF0F766E).copy(alpha = 0.2f)
                                        SchoolRole.STUDENT -> PrimaryLight.copy(alpha = 0.2f)
                                        SchoolRole.PARENT -> Color(0xFF7C3AED).copy(alpha = 0.2f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.quickLoginAsRole(role, userMatch, onSuccess = onLoginSuccess)
                                    }
                                    .testTag("quick_login_${role.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (role) {
                                                    SchoolRole.ADMIN -> Color(0xFF1E3A8A)
                                                    SchoolRole.TEACHER -> Color(0xFF0F766E)
                                                    SchoolRole.STUDENT -> PrimaryLight
                                                    SchoolRole.PARENT -> Color(0xFF7C3AED)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (role) {
                                                SchoolRole.ADMIN -> Icons.Rounded.AdminPanelSettings
                                                SchoolRole.TEACHER -> Icons.Rounded.CoPresent
                                                SchoolRole.STUDENT -> Icons.Rounded.School
                                                SchoolRole.PARENT -> Icons.Rounded.FamilyRestroom
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text(text = "Role: ${role.name} • Tap to enter", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Login",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Password Reset Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your registered email address to receive password reset instructions from Firebase Auth:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("Account Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordReset(resetEmailInput)
                        showForgotPasswordDialog = false
                    },
                    enabled = resetEmailInput.isNotBlank()
                ) {
                    Text("Send Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
