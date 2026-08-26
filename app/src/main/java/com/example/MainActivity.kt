package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolRole
import com.example.data.model.SchoolUser
import com.example.ui.components.RoleBadge
import com.example.ui.components.SecurityPinDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcademiaTrackTheme {
                SchoolManagementApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolManagementApp(viewModel: SchoolViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val cbtRunnerState by viewModel.cbtRunnerState.collectAsState()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()
    val securityError by viewModel.securityError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var pendingRoleForPin by remember { mutableStateOf<SchoolRole?>(null) }
    var pendingUserForPin by remember { mutableStateOf<SchoolUser?>(null) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.setFeedbackMessage(null)
        }
    }

    // If an interactive CBT Exam is actively running, present fullscreen CBT Runner
    if (cbtRunnerState.isRunning || (cbtRunnerState.isSubmitted && cbtRunnerState.submissionResult != null)) {
        CbtExamRunnerScreen(viewModel = viewModel)
        return
    }

    val primaryHeaderColor = when (currentRole) {
        SchoolRole.ADMIN -> Color(0xFF1E3A8A)
        SchoolRole.TEACHER -> Color(0xFF0F766E)
        SchoolRole.STUDENT -> PrimaryLight
        SchoolRole.PARENT -> Color(0xFF7C3AED)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryHeaderColor, AcademicViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "AcademiaTrack",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Secondary School Portal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Role Switcher Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = primaryHeaderColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryHeaderColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clickable { showRoleSwitchDialog = true }
                            .testTag("switch_role_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            RoleBadge(role = currentRole)
                            Text(
                                text = currentUser?.name?.split(" ")?.firstOrNull() ?: "User",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryHeaderColor,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = primaryHeaderColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentRole) {
                SchoolRole.ADMIN -> AdminPortalScreen(viewModel = viewModel)
                SchoolRole.TEACHER -> TeacherPortalScreen(viewModel = viewModel)
                SchoolRole.STUDENT -> StudentPortalScreen(viewModel = viewModel)
                SchoolRole.PARENT -> ParentPortalScreen(viewModel = viewModel)
            }
        }
    }

    // Role Switch Dialog
    if (showRoleSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitchDialog = false },
            title = {
                Text(
                    text = "Select Portal / Role",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Access is strictly partitioned. Teachers and Admins have secured management corners, while Students and Parents have distinct academic portals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val demoRoles = listOf(
                        Triple(SchoolRole.STUDENT, "Student Portal", "Chidinma Nwosu (SS 2 Gold) - CBT & Grades"),
                        Triple(SchoolRole.PARENT, "Parent Portal", "Mrs. Ngozi Nwosu (Parent of Chidinma)"),
                        Triple(SchoolRole.TEACHER, "Teacher Corner [PIN Secured]", "Mr. David Okon (Maths & Physics)"),
                        Triple(SchoolRole.ADMIN, "Admin Corner [PIN Secured]", "Dr. C. Adebayo (Principal / Admin)")
                    )

                    demoRoles.forEach { (role, title, desc) ->
                        val isCurrent = currentRole == role
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryLight) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showRoleSwitchDialog = false
                                    if (role == SchoolRole.ADMIN || role == SchoolRole.TEACHER) {
                                        pendingRoleForPin = role
                                        pendingUserForPin = allUsers.find { it.role == role }
                                    } else {
                                        viewModel.selectPortal(role)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    Text(text = desc, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                if (role == SchoolRole.ADMIN || role == SchoolRole.TEACHER) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Secured",
                                        tint = AcademicRose,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleSwitchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Security PIN Verification Dialog for Admin / Teacher
    pendingRoleForPin?.let { role ->
        SecurityPinDialog(
            targetRole = role,
            errorMessage = securityError,
            onDismiss = {
                pendingRoleForPin = null
                pendingUserForPin = null
                viewModel.clearSecurityError()
            },
            onConfirm = { pin ->
                val success = viewModel.selectPortal(role, user = pendingUserForPin, pin = pin)
                if (success) {
                    pendingRoleForPin = null
                    pendingUserForPin = null
                }
            }
        )
    }
}
