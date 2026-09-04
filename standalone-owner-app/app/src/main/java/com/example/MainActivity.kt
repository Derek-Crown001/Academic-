package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.ui.components.UserAvatar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalTab
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
    var showSplashScreen by remember { mutableStateOf(true) }

    AnimatedContent(
        targetState = showSplashScreen,
        transitionSpec = {
            fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) togetherWith
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
        },
        label = "splash_screen_transition"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(
                onSplashFinished = {
                    showSplashScreen = false
                }
            )
        } else {
            SchoolManagementMainContent(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolManagementMainContent(viewModel: SchoolViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
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

    // If not authenticated with Firebase Auth, present the full Login Navigation Flow
    if (!isAuthenticated) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                // Navigates directly into the respective role dashboard
            }
        )
        return
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentUser?.let { user ->
                viewModel.updateUserPhoto(user.id, it.toString())
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
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
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "AcademiaTrack",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${currentRole.name.lowercase().replaceFirstChar { it.uppercase() }} Portal",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    // Profile Photo Avatar
                    UserAvatar(
                        user = currentUser,
                        size = 30.dp,
                        onUploadClick = { photoPickerLauncher.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

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
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            RoleBadge(role = currentRole)
                            Text(
                                text = currentUser?.name?.split(" ")?.firstOrNull() ?: "User",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryHeaderColor,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = primaryHeaderColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Sign Out Action Button
                    IconButton(
                        onClick = { viewModel.logout(context) },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            RoleBasedNavigationBar(
                currentRole = currentRole,
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
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
                        text = "Access is partitioned by institutional role: Principal / Administrator, Teacher Faculty, Enrolled Student, and Parent / Guardian.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val demoRoles = listOf(
                        Triple(SchoolRole.ADMIN, "Admin Corner [PIN Secured]", "Dr. C. Adebayo (Principal / School Head)"),
                        Triple(SchoolRole.TEACHER, "Teacher Corner [PIN Secured]", "Mr. David Okon (Maths & Physics Faculty)"),
                        Triple(SchoolRole.STUDENT, "Student Portal", "Chidinma Nwosu (SS 2 Gold) - CBT & Grades"),
                        Triple(SchoolRole.PARENT, "Parent Portal", "Mrs. Ngozi Nwosu (Parent of Chidinma)")
                    )

                    demoRoles.forEach { (role, title, desc) ->
                        val isCurrent = currentRole == role
                        val roleColor = when (role) {
                            SchoolRole.ADMIN -> Color(0xFF1E3A8A)
                            SchoolRole.TEACHER -> Color(0xFF0F766E)
                            SchoolRole.STUDENT -> PrimaryLight
                            SchoolRole.PARENT -> Color(0xFF7C3AED)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) roleColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, roleColor) else null,
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
                                        .background(roleColor),
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sign Out Button from dialog
                    OutlinedButton(
                        onClick = {
                            showRoleSwitchDialog = false
                            viewModel.logout(context)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AcademicRose),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Out of Session", fontWeight = FontWeight.SemiBold)
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

/**
 * Dynamic Role-Based Bottom Navigation Bar.
 * Adapts navigation items, icons, labels, testTags, and active indicators based on the currently authenticated role.
 */
@Composable
fun RoleBasedNavigationBar(
    currentRole: SchoolRole,
    currentTab: PortalTab,
    onTabSelected: (PortalTab) -> Unit,
    modifier: Modifier = Modifier
) {
    data class NavItem(
        val tab: PortalTab,
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val testTag: String
    )

    val items = when (currentRole) {
        SchoolRole.ADMIN -> listOf(
            NavItem(PortalTab.DASHBOARD, "Dashboard", Icons.Rounded.Dashboard, "nav_admin_dashboard"),
            NavItem(PortalTab.DATA_VERIFICATION, "Audit", Icons.Rounded.VerifiedUser, "nav_admin_audit"),
            NavItem(PortalTab.ADMIN_MEMO, "Memos", Icons.Rounded.Description, "nav_admin_memos"),
            NavItem(PortalTab.CLASSES, "Classes", Icons.Rounded.MeetingRoom, "nav_admin_classes"),
            NavItem(PortalTab.REPORT_CARDS, "Reports", Icons.Rounded.Assessment, "nav_admin_reports")
        )
        SchoolRole.TEACHER -> listOf(
            NavItem(PortalTab.TEACHER_DASHBOARD, "Dashboard", Icons.Rounded.Dashboard, "nav_teacher_dashboard"),
            NavItem(PortalTab.CLASS_STUDENTS, "Students", Icons.Rounded.People, "nav_teacher_students"),
            NavItem(PortalTab.CA_GRADING, "Grading", Icons.Rounded.Grade, "nav_teacher_grading"),
            NavItem(PortalTab.CBT_CREATOR, "CBT Tests", Icons.Rounded.Quiz, "nav_teacher_cbt"),
            NavItem(PortalTab.CLASS_REGISTER, "Register", Icons.Rounded.FactCheck, "nav_teacher_register")
        )
        SchoolRole.STUDENT -> listOf(
            NavItem(PortalTab.STUDENT_CBT, "CBT Exams", Icons.Rounded.Quiz, "nav_student_cbt"),
            NavItem(PortalTab.STUDENT_ASSIGNMENTS, "Tasks", Icons.Rounded.Assignment, "nav_student_tasks"),
            NavItem(PortalTab.STUDENT_REPORT_CARD, "Report", Icons.Rounded.Assessment, "nav_student_report"),
            NavItem(PortalTab.STUDENT_AI_TUTOR, "AI Tutor", Icons.Rounded.AutoAwesome, "nav_student_ai"),
            NavItem(PortalTab.STUDENT_CLASS_CHAT, "Chat", Icons.Rounded.Chat, "nav_student_chat")
        )
        SchoolRole.PARENT -> listOf(
            NavItem(PortalTab.PARENT_CHILD_OVERVIEW, "Ward", Icons.Rounded.ChildCare, "nav_parent_ward"),
            NavItem(PortalTab.PARENT_REPORT_CARD, "Report", Icons.Rounded.Assessment, "nav_parent_report"),
            NavItem(PortalTab.PARENT_AI_ASSISTANT, "Advisor", Icons.Rounded.AutoAwesome, "nav_parent_ai"),
            NavItem(PortalTab.PARENT_ANNOUNCEMENTS, "Notices", Icons.Rounded.Notifications, "nav_parent_notices"),
            NavItem(PortalTab.PARENT_CONTACT, "Contact", Icons.Rounded.Call, "nav_parent_contact")
        )
    }

    val activeColor = when (currentRole) {
        SchoolRole.ADMIN -> Color(0xFF1E3A8A)
        SchoolRole.TEACHER -> Color(0xFF0F766E)
        SchoolRole.STUDENT -> PrimaryLight
        SchoolRole.PARENT -> Color(0xFF7C3AED)
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        items.forEach { item ->
            val selected = currentTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    indicatorColor = activeColor.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
