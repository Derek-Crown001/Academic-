package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AnnouncementCard
import com.example.ui.components.GradeBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalTab
import com.example.ui.viewmodel.SchoolViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allCbtExams by viewModel.allCbtExams.collectAsState()
    val allReportCards by viewModel.allReportCards.collectAsState()
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val allTeacherAttendance by viewModel.allTeacherAttendance.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showBroadcastAnnouncementDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Admin Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = when (currentTab) {
                PortalTab.DASHBOARD -> 0
                PortalTab.CLASSES -> 1
                PortalTab.STAFF_ATTENDANCE -> 2
                PortalTab.REPORT_CARDS -> 3
                PortalTab.ADMIN_AI_ASSISTANT -> 4
                PortalTab.SCHOOL_SETTINGS -> 5
                PortalTab.SUBJECTS -> 6
                PortalTab.ANNOUNCEMENTS -> 7
                PortalTab.STAFF_CHAT, PortalTab.CLASS_CHAT_MODERATION -> 8
                else -> 0
            },
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryLight
        ) {
            Tab(
                selected = currentTab == PortalTab.DASHBOARD,
                onClick = { viewModel.selectTab(PortalTab.DASHBOARD) },
                text = { Text("Overview", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Dashboard, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.CLASSES,
                onClick = { viewModel.selectTab(PortalTab.CLASSES) },
                text = { Text("Classes & Arms", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.MeetingRoom, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STAFF_ATTENDANCE,
                onClick = { viewModel.selectTab(PortalTab.STAFF_ATTENDANCE) },
                text = { Text("Staff Attendance", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Schedule, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.REPORT_CARDS,
                onClick = { viewModel.selectTab(PortalTab.REPORT_CARDS) },
                text = { Text("Seal & Approve", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Assessment, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.ADMIN_AI_ASSISTANT,
                onClick = { viewModel.selectTab(PortalTab.ADMIN_AI_ASSISTANT) },
                text = { Text("AI Assistant", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.SCHOOL_SETTINGS,
                onClick = { viewModel.selectTab(PortalTab.SCHOOL_SETTINGS) },
                text = { Text("School Settings", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Settings, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.SUBJECTS,
                onClick = { viewModel.selectTab(PortalTab.SUBJECTS) },
                text = { Text("Curriculum", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Subject, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.ANNOUNCEMENTS,
                onClick = { viewModel.selectTab(PortalTab.ANNOUNCEMENTS) },
                text = { Text("Broadcasts", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Campaign, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STAFF_CHAT || currentTab == PortalTab.CLASS_CHAT_MODERATION,
                onClick = {
                    viewModel.setChatChannel("STAFF_GENERAL")
                    viewModel.selectTab(PortalTab.STAFF_CHAT)
                },
                text = { Text("Chat & Moderation", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Forum, contentDescription = null) }
            )
        }

        when (currentTab) {
            PortalTab.DASHBOARD -> {
                AdminDashboardContent(
                    studentsCount = allStudents.size,
                    teachersCount = allTeachers.size,
                    classesCount = allClasses.size,
                    subjectsCount = allSubjects.size,
                    cbtCount = allCbtExams.size,
                    reportCards = allReportCards,
                    cloudSyncStatus = cloudSyncStatus,
                    onSyncToCloud = { viewModel.syncAllDataToCloud() },
                    onNavigateToTab = { viewModel.selectTab(it) },
                    onBroadcastClick = { showBroadcastAnnouncementDialog = true }
                )
            }
            PortalTab.CLASSES -> {
                AdminClassManagementContent(
                    classes = allClasses,
                    teachers = allTeachers,
                    students = allStudents,
                    onAddClass = { name, level, arm, teacherId, teacherName, capacity, room ->
                        viewModel.addClass(name, level, arm, teacherId, teacherName, capacity, room)
                    },
                    onUpdateClass = { updatedClass ->
                        viewModel.updateClass(updatedClass)
                    },
                    onDeleteClass = { cls ->
                        viewModel.deleteClass(cls)
                    }
                )
            }
            PortalTab.STAFF_ATTENDANCE -> {
                AdminStaffAttendanceContent(
                    attendances = allTeacherAttendance,
                    teachers = allTeachers
                )
            }
            PortalTab.REPORT_CARDS -> {
                AdminReportCardsApprovalContent(
                    reportCards = allReportCards,
                    onApproveReportCard = { id, approved -> viewModel.approveReportCard(id, approved) },
                    onBulkApproveClass = { className -> viewModel.bulkApproveClassReportCards(className) },
                    onExportPdf = { rc -> viewModel.exportReportCardPdf(context = context, reportCard = rc) }
                )
            }
            PortalTab.ADMIN_AI_ASSISTANT -> {
                RoleAiAssistantScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    currentRole = SchoolRole.ADMIN
                )
            }
            PortalTab.SCHOOL_SETTINGS -> {
                AdminSchoolSettingsContent(
                    schoolProfile = schoolProfile,
                    onSaveProfile = { updated -> viewModel.updateSchoolProfile(updated) },
                    onClearDemoLogs = { viewModel.clearAllActivityLogsAndHistory() }
                )
            }
            PortalTab.SUBJECTS -> {
                AdminSubjectsContent(
                    subjects = allSubjects,
                    teachers = allTeachers,
                    onAddSubjectClick = { showAddSubjectDialog = true }
                )
            }
            PortalTab.ANNOUNCEMENTS -> {
                AdminAnnouncementsContent(
                    announcements = allAnnouncements,
                    onNewAnnouncement = { showBroadcastAnnouncementDialog = true }
                )
            }
            PortalTab.STAFF_CHAT, PortalTab.CLASS_CHAT_MODERATION -> {
                ChatRoomScreen(viewModel = viewModel)
            }
            else -> {}
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            teachers = allTeachers,
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { name, code, level, teacherName, teacherId, colorHex ->
                viewModel.addSubject(name, code, level, teacherName, teacherId, colorHex)
                showAddSubjectDialog = false
            }
        )
    }

    if (showBroadcastAnnouncementDialog) {
        BroadcastAnnouncementDialog(
            onDismiss = { showBroadcastAnnouncementDialog = false },
            onConfirm = { title, content, audience, category, isUrgent ->
                viewModel.broadcastAnnouncement(title, content, audience, category, isUrgent)
                showBroadcastAnnouncementDialog = false
            }
        )
    }
}

@Composable
fun AdminDashboardContent(
    studentsCount: Int,
    teachersCount: Int,
    classesCount: Int,
    subjectsCount: Int,
    cbtCount: Int,
    reportCards: List<ReportCard>,
    cloudSyncStatus: com.example.service.firestore.CloudSyncStatus,
    onSyncToCloud: () -> Unit,
    onNavigateToTab: (PortalTab) -> Unit,
    onBroadcastClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "School Administration Corner",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = AcademicEmerald
                        )
                    }

                    Text(
                        text = "Real-time academic management, CBT oversight, subject teacher coordination, and report card approvals.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBroadcastClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E3A8A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_broadcast_button")
                        ) {
                            Icon(Icons.Rounded.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Broadcast Notice", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = onSyncToCloud,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_cloud_sync_button")
                        ) {
                            Icon(
                                if (cloudSyncStatus.state == com.example.service.firestore.CloudSyncState.SYNCING)
                                    Icons.Rounded.Sync
                                else
                                    Icons.Rounded.CloudSync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Cloud", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Firebase Cloud Firestore Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                when (cloudSyncStatus.state) {
                                    com.example.service.firestore.CloudSyncState.SYNCING -> PrimaryLight.copy(alpha = 0.12f)
                                    com.example.service.firestore.CloudSyncState.SUCCESS -> AcademicEmerald.copy(alpha = 0.12f)
                                    com.example.service.firestore.CloudSyncState.ERROR -> AcademicAmber.copy(alpha = 0.12f)
                                    else -> PrimaryLight.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (cloudSyncStatus.state) {
                                com.example.service.firestore.CloudSyncState.SYNCING -> Icons.Rounded.Sync
                                com.example.service.firestore.CloudSyncState.SUCCESS -> Icons.Rounded.CloudDone
                                com.example.service.firestore.CloudSyncState.ERROR -> Icons.Rounded.CloudOff
                                else -> Icons.Rounded.CloudQueue
                            },
                            contentDescription = null,
                            tint = when (cloudSyncStatus.state) {
                                com.example.service.firestore.CloudSyncState.SYNCING -> PrimaryLight
                                com.example.service.firestore.CloudSyncState.SUCCESS -> AcademicEmerald
                                com.example.service.firestore.CloudSyncState.ERROR -> AcademicAmber
                                else -> PrimaryLight
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Firebase Cloud Firestore",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (cloudSyncStatus.state) {
                                    com.example.service.firestore.CloudSyncState.SYNCING -> PrimaryLight.copy(alpha = 0.15f)
                                    com.example.service.firestore.CloudSyncState.SUCCESS -> AcademicEmerald.copy(alpha = 0.15f)
                                    com.example.service.firestore.CloudSyncState.ERROR -> AcademicAmber.copy(alpha = 0.15f)
                                    else -> PrimaryLight.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = when (cloudSyncStatus.state) {
                                        com.example.service.firestore.CloudSyncState.SYNCING -> "SYNCING"
                                        com.example.service.firestore.CloudSyncState.SUCCESS -> "LIVE SYNC"
                                        com.example.service.firestore.CloudSyncState.ERROR -> "OFFLINE READY"
                                        else -> "ONLINE"
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (cloudSyncStatus.state) {
                                        com.example.service.firestore.CloudSyncState.SYNCING -> PrimaryLight
                                        com.example.service.firestore.CloudSyncState.SUCCESS -> AcademicEmerald
                                        com.example.service.firestore.CloudSyncState.ERROR -> AcademicAmber
                                        else -> PrimaryLight
                                    }
                                )
                            }
                        }
                        Text(
                            text = cloudSyncStatus.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    IconButton(
                        onClick = onSyncToCloud,
                        modifier = Modifier.testTag("refresh_cloud_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Sync Cloud",
                            tint = PrimaryLight
                        )
                    }
                }
            }
        }

        // Stats Grid
        item {
            Text(
                text = "Key Academic Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(title = "Total Students", value = "$studentsCount", icon = Icons.Rounded.School, color = PrimaryLight, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Teaching Staff", value = "$teachersCount", icon = Icons.Rounded.CoPresent, color = AcademicViolet, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Class Arms",
                    value = "$classesCount",
                    icon = Icons.Rounded.MeetingRoom,
                    color = SecondaryLight,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(PortalTab.CLASSES) }
                )
                AdminStatCard(
                    title = "CBT Assessments",
                    value = "$cbtCount",
                    icon = Icons.Rounded.Quiz,
                    color = AcademicAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Report Card Approvals Summary
        item {
            val approvedCount = reportCards.count { it.isApprovedByAdmin }
            val pendingCount = reportCards.size - approvedCount

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(PortalTab.REPORT_CARDS) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AcademicEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FactCheck,
                            contentDescription = null,
                            tint = AcademicEmerald,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Terminal Report Cards Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$approvedCount Approved & Published • $pendingCount Pending Review",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminSubjectsContent(
    subjects: List<SchoolSubject>,
    teachers: List<SchoolUser>,
    onAddSubjectClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subjects & Assigned Teachers (${subjects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddSubjectClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_subject_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Subject")
                }
            }
        }

        items(subjects) { subject ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject.code.take(3),
                            fontWeight = FontWeight.Black,
                            color = PrimaryLight,
                            fontSize = 13.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Level: ${subject.classLevel} • ${subject.periodsPerWeek} periods/wk",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Teacher: ${subject.teacherName.ifBlank { "Unassigned" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryLight
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = subject.code,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportCardsContent(
    reportCards: List<ReportCard>,
    onToggleApprove: (Long, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Terminal Report Sheet Review & Approvals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(reportCards) { rc ->
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
                            Text(text = rc.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "${rc.className} • ${rc.admissionNo} • ${rc.term}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (rc.isApprovedByAdmin) AcademicEmerald.copy(alpha = 0.15f) else AcademicAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (rc.isApprovedByAdmin) "APPROVED & PUBLISHED" else "PENDING REVIEW",
                                color = if (rc.isApprovedByAdmin) AcademicEmerald else AcademicAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Average: ${String.format(Locale.US, "%.1f%%", rc.averageScore)}",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLight,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Position: ${rc.classPosition}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Teacher Remark: \"${rc.classTeacherRemark}\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onToggleApprove(rc.id, !rc.isApprovedByAdmin) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rc.isApprovedByAdmin) AcademicRose else AcademicEmerald
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (rc.isApprovedByAdmin) "Revoke Approval" else "Approve & Publish to Student/Parent")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnnouncementsContent(
    announcements: List<SchoolAnnouncement>,
    onNewAnnouncement: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "School Broadcasts (${announcements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onNewAnnouncement,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Notice")
                }
            }
        }

        items(announcements) { announcement ->
            AnnouncementCard(announcement = announcement)
        }
    }
}

// --- Dialogs ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    teachers: List<SchoolUser>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, level: String, teacherName: String, teacherId: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("SS 2") }
    var selectedTeacher by remember { mutableStateOf(teachers.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subject & Assign Teacher", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name (e.g. Further Mathematics)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Subject Code (e.g. FTH 201)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("Class Level (e.g. SS 2)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assign Subject Teacher:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                teachers.forEach { teacher ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTeacher = teacher }
                    ) {
                        RadioButton(
                            selected = selectedTeacher?.id == teacher.id,
                            onClick = { selectedTeacher = teacher }
                        )
                        Text(text = teacher.name, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onConfirm(
                            name,
                            code,
                            level,
                            selectedTeacher?.name ?: "Unassigned",
                            selectedTeacher?.id ?: "",
                            "#2563EB"
                        )
                    }
                }
            ) {
                Text("Add Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BroadcastAnnouncementDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, audience: String, category: String, isUrgent: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("ALL") }
    var category by remember { mutableStateOf("ACADEMIC") }
    var isUrgent by remember { mutableStateOf(false) }

    val audiences = listOf("ALL", "STUDENT", "TEACHER", "PARENT")
    val categories = listOf("ACADEMIC", "EXAM", "FEES", "EVENT", "GENERAL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast School Notice", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Notice Content") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Target Audience:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    audiences.forEach { aud ->
                        FilterChip(
                            selected = audience == aud,
                            onClick = { audience = aud },
                            label = { Text(aud, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                    Text("Mark as Urgent Notice", fontSize = 13.sp, color = AcademicRose)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, audience, category, isUrgent)
                    }
                }
            ) {
                Text("Publish Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
