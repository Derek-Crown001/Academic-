package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.GradeBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalTab
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPortalScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val allCbtExams by viewModel.allCbtExams.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val allAssignmentSubmissions by viewModel.allAssignmentSubmissions.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val currentTeacherAttendance by viewModel.currentTeacherAttendance.collectAsState()
    val allReportCards by viewModel.allReportCards.collectAsState()

    var showCreateCbtDialog by remember { mutableStateOf(false) }
    var showCreateAssignmentDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var selectedGradeStudent by remember { mutableStateOf<StudentGrade?>(null) }
    var selectedExamForQuestionBank by remember { mutableStateOf<CbtExam?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Teacher Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = when (currentTab) {
                PortalTab.TEACHER_DASHBOARD -> 0
                PortalTab.CLASS_STUDENTS -> 1
                PortalTab.CA_GRADING -> 2
                PortalTab.CLASS_REGISTER -> 3
                PortalTab.SUBJECT_MANAGER -> 4
                PortalTab.CBT_CREATOR -> 5
                PortalTab.ASSIGNMENT_MANAGER -> 6
                PortalTab.TEACHER_AI_ASSISTANT -> 7
                PortalTab.STAFF_CHAT -> 8
                else -> 0
            },
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryLight
        ) {
            Tab(
                selected = currentTab == PortalTab.TEACHER_DASHBOARD,
                onClick = { viewModel.selectTab(PortalTab.TEACHER_DASHBOARD) },
                text = { Text("My Desk", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.CoPresent, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.CLASS_STUDENTS,
                onClick = { viewModel.selectTab(PortalTab.CLASS_STUDENTS) },
                text = { Text("Students", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.People, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.CA_GRADING,
                onClick = { viewModel.selectTab(PortalTab.CA_GRADING) },
                text = { Text("CA & Reports", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.FactCheck, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.CLASS_REGISTER,
                onClick = { viewModel.selectTab(PortalTab.CLASS_REGISTER) },
                text = { Text("Mark Register", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.HowToReg, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.SUBJECT_MANAGER,
                onClick = { viewModel.selectTab(PortalTab.SUBJECT_MANAGER) },
                text = { Text("Add Subjects", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.LibraryBooks, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.CBT_CREATOR,
                onClick = { viewModel.selectTab(PortalTab.CBT_CREATOR) },
                text = { Text("CBT Tests", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Quiz, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.ASSIGNMENT_MANAGER,
                onClick = { viewModel.selectTab(PortalTab.ASSIGNMENT_MANAGER) },
                text = { Text("Homework", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Assignment, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.TEACHER_AI_ASSISTANT,
                onClick = { viewModel.selectTab(PortalTab.TEACHER_AI_ASSISTANT) },
                text = { Text("AI Assistant", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STAFF_CHAT,
                onClick = {
                    viewModel.setChatChannel("STAFF_GENERAL")
                    viewModel.selectTab(PortalTab.STAFF_CHAT)
                },
                text = { Text("Staff Room", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Forum, contentDescription = null) }
            )
        }

        when (currentTab) {
            PortalTab.TEACHER_DASHBOARD -> {
                TeacherDashboardContent(
                    teacher = currentUser,
                    teacherAttendance = currentTeacherAttendance,
                    subjects = allSubjects.filter { it.teacherId == currentUser?.id || it.teacherName.contains(currentUser?.name ?: "xyz") },
                    cbtExams = allCbtExams,
                    assignments = allAssignments,
                    onClockIn = { viewModel.clockInCurrentTeacher() },
                    onClockOut = { viewModel.clockOutCurrentTeacher() },
                    onNavigateToTab = { viewModel.selectTab(it) }
                )
            }
            PortalTab.CLASS_STUDENTS -> {
                TeacherStudentManagementSection(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    allStudents = allStudents,
                    allClasses = allClasses
                )
            }
            PortalTab.CA_GRADING -> {
                TeacherCaGradingContent(
                    grades = allGrades,
                    reportCards = allReportCards,
                    onEditGrade = { grade -> selectedGradeStudent = grade },
                    onPublishClass = { className -> viewModel.publishClassReportCards(className) }
                )
            }
            PortalTab.CLASS_REGISTER -> {
                TeacherClassRegisterContent(
                    viewModel = viewModel,
                    students = allStudents,
                    classes = allClasses
                )
            }
            PortalTab.SUBJECT_MANAGER -> {
                TeacherSubjectManagementContent(
                    subjects = allSubjects,
                    teachers = allTeachers,
                    onAddSubjectClick = { showAddSubjectDialog = true },
                    onDeleteSubject = { subject -> viewModel.deleteSubject(subject) }
                )
            }
            PortalTab.CBT_CREATOR -> {
                TeacherCbtContent(
                    cbtExams = allCbtExams,
                    onCreateCbtClick = { showCreateCbtDialog = true },
                    onStartExamPreview = { viewModel.startCbtExam(it) },
                    onManageQuestionBank = { exam -> selectedExamForQuestionBank = exam }
                )
            }
            PortalTab.ASSIGNMENT_MANAGER -> {
                TeacherAssignmentsContent(
                    assignments = allAssignments,
                    submissions = allAssignmentSubmissions,
                    onCreateAssignmentClick = { showCreateAssignmentDialog = true },
                    onGradeSubmission = { sub, score, feedback ->
                        viewModel.gradeAssignmentSubmission(sub, score, feedback)
                    }
                )
            }
            PortalTab.TEACHER_AI_ASSISTANT -> {
                RoleAiAssistantScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    currentRole = SchoolRole.TEACHER
                )
            }
            PortalTab.STAFF_CHAT -> {
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

    if (showCreateCbtDialog) {
        CreateCbtDialog(
            viewModel = viewModel,
            onDismiss = { showCreateCbtDialog = false },
            onConfirm = { title, subject, className, type, duration, passMark, instructions, questions ->
                viewModel.createCbtExam(
                    title = title,
                    subjectName = subject,
                    className = className,
                    examType = type,
                    durationMinutes = duration,
                    passMark = passMark,
                    instructions = instructions,
                    questions = questions
                )
                showCreateCbtDialog = false
            }
        )
    }

    if (showCreateAssignmentDialog) {
        CreateAssignmentDialog(
            onDismiss = { showCreateAssignmentDialog = false },
            onConfirm = { title, subject, className, dueMillis, maxMarks, description, instructions ->
                viewModel.createAssignment(
                    title = title,
                    subjectName = subject,
                    className = className,
                    dueDateMillis = dueMillis,
                    maxMarks = maxMarks,
                    description = description,
                    instructions = instructions
                )
                showCreateAssignmentDialog = false
            }
        )
    }

    selectedExamForQuestionBank?.let { exam ->
        QuestionBankManagerDialog(
            exam = exam,
            viewModel = viewModel,
            onDismiss = { selectedExamForQuestionBank = null }
        )
    }

    selectedGradeStudent?.let { grade ->
        EditGradeCaDialog(
            grade = grade,
            onDismiss = { selectedGradeStudent = null },
            onSave = { test1, test2, midterm, exam ->
                viewModel.saveStudentGrade(
                    id = grade.id,
                    studentId = grade.studentId,
                    studentName = grade.studentName,
                    admissionNo = grade.admissionNo,
                    className = grade.className,
                    subjectName = grade.subjectName,
                    test1 = test1,
                    test2 = test2,
                    midterm = midterm,
                    exam = exam
                )
                selectedGradeStudent = null
            }
        )
    }
}

@Composable
fun TeacherDashboardContent(
    teacher: SchoolUser?,
    teacherAttendance: TeacherAttendance?,
    subjects: List<SchoolSubject>,
    cbtExams: List<CbtExam>,
    assignments: List<SchoolAssignment>,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    onNavigateToTab: (PortalTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Clock-In / Clock-Out Card
        item {
            TeacherClockInCard(
                teacherAttendance = teacherAttendance,
                onClockIn = onClockIn,
                onClockOut = onClockOut
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Teacher Corner • ${teacher?.name ?: "Educator"}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Class Teacher: ${teacher?.className.orEmpty()} | Subjects: ${teacher?.assignedSubjects.orEmpty()}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Take daily attendance register, manage subjects, compute continuous assessment grades, create CBT examinations, and publish class report cards to school admin for approval.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(title = "My Subjects", value = "${subjects.size.coerceAtLeast(2)}", icon = Icons.Rounded.Book, color = PrimaryLight, modifier = Modifier.weight(1f))
                AdminStatCard(title = "Active CBTs", value = "${cbtExams.size}", icon = Icons.Rounded.Quiz, color = AcademicAmber, modifier = Modifier.weight(1f))
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Teacher Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(PortalTab.CLASS_STUDENTS) }
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
                            .background(Color(0xFF0F766E).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.People, contentDescription = null, tint = Color(0xFF0F766E))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Manage Class Student Profiles", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Add students, upload profile photos from phone storage, view guardian contacts.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(PortalTab.TEACHER_AI_ASSISTANT) }
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
                            .background(PrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryLight)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "AI Lesson & CBT Assistant", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Generate lesson plans, WAEC/JAMB format CBT questions, and remark comments instantly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(PortalTab.CLASS_REGISTER) }
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
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.HowToReg, contentDescription = null, tint = Color(0xFF047857))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Daily Student Attendance Register", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Mark Present, Late, or Absent for automated report card records.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(PortalTab.CA_GRADING) }
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
                            .background(PrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.FactCheck, contentDescription = null, tint = PrimaryLight)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Review & Publish Class Report Cards", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Adjust CA and CBT scores, enter remarks, and submit to Admin for seal.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(PortalTab.SUBJECT_MANAGER) }
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
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.LibraryBooks, contentDescription = null, tint = Color(0xFF7C3AED))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Subject & Curriculum Management", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Add new academic subjects, set codes, and assign subject teachers.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TeacherCaGradingContent(
    grades: List<StudentGrade>,
    reportCards: List<ReportCard>,
    onEditGrade: (StudentGrade) -> Unit,
    onPublishClass: (String) -> Unit
) {
    var selectedClass by remember { mutableStateOf("SS 2 Gold") }
    val classGrades = grades.filter { it.className.equals(selectedClass, ignoreCase = true) }
    val classReportCards = reportCards.filter { it.className.equals(selectedClass, ignoreCase = true) }
    val isPublished = classReportCards.any { it.isPublishedByTeacher }
    val isApproved = classReportCards.any { it.isApprovedByAdmin }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Class Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Class:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                listOf("SS 1 Silver", "SS 2 Gold", "SS 3 Science").forEach { cName ->
                    val isSelected = selectedClass.equals(cName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedClass = cName },
                        label = { Text(cName) }
                    )
                }
            }
        }

        // Report Cards Publication Workflow Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isApproved -> Color(0xFF064E3B)
                        isPublished -> Color(0xFF1E3A8A)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
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
                            Text(
                                text = "Class Terminal Report Card Workflow",
                                fontWeight = FontWeight.Bold,
                                color = if (isApproved || isPublished) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Target: $selectedClass",
                                color = if (isApproved || isPublished) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isApproved -> Color(0xFF10B981)
                                isPublished -> Color(0xFF3B82F6)
                                else -> Color(0xFFF59E0B)
                            }
                        ) {
                            Text(
                                text = when {
                                    isApproved -> "ADMIN APPROVED"
                                    isPublished -> "SUBMITTED TO ADMIN"
                                    else -> "TEACHER DRAFT"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = when {
                            isApproved -> "These report cards have been officially sealed and approved by the School Principal. Parents and students can now view and download official PDF copies."
                            isPublished -> "Scores are submitted to the School Admin panel. The Principal is currently reviewing for final seal and release to parents."
                            else -> "Continuous assessment & CBT scores are automatically registered. You can edit any scores manually below, then click 'Publish to Admin' when finalized."
                        },
                        fontSize = 12.sp,
                        color = if (isApproved || isPublished) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { onPublishClass(selectedClass) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPublished) Color(0xFF10B981) else PrimaryLight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("publish_class_report_cards_button")
                    ) {
                        Icon(
                            if (isPublished) Icons.Rounded.PublishedWithChanges else Icons.Rounded.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPublished) "Update & Re-Publish $selectedClass Report Cards to Admin" else "Publish $selectedClass Report Cards to Admin Panel",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Continuous Assessment & Exam Score Sheets (${classGrades.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(classGrades) { grade ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = grade.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "${grade.className} • ${grade.subjectName} • ${grade.admissionNo}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        GradeBadge(grade = grade.gradeLetter)
                    }

                    // Score breakdown chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "1st CA: ${grade.test1Score}/15", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "2nd CA: ${grade.test2Score}/15", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Midterm: ${grade.midtermScore}/20", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Exam: ${grade.examScore}/50", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Score: ${grade.totalScore} / 100 (${grade.remarks})",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLight,
                            fontSize = 13.sp
                        )

                        Button(
                            onClick = { onEditGrade(grade) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Scores", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherCbtContent(
    cbtExams: List<CbtExam>,
    onCreateCbtClick: () -> Unit,
    onStartExamPreview: (CbtExam) -> Unit,
    onManageQuestionBank: (CbtExam) -> Unit
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
                Column {
                    Text(
                        text = "CBT Exams & Question Bank (${cbtExams.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage options (A, B, C, D) and answer keys",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCreateCbtClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("teacher_create_cbt_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create CBT")
                }
            }
        }

        items(cbtExams) { exam ->
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryLight.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = exam.examType,
                                color = PrimaryLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${exam.durationMinutes} Mins • Pass: ${exam.passMark} Mks",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = exam.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Subject: ${exam.subjectName} • Target Class: ${exam.className}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Marks: ${exam.totalMarks}",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLight,
                            fontSize = 13.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onManageQuestionBank(exam) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("manage_cbt_bank_button_${exam.id}")
                            ) {
                                Icon(Icons.Rounded.Rule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Question Bank", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onStartExamPreview(exam) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("preview_cbt_button_${exam.id}")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAssignmentsContent(
    assignments: List<SchoolAssignment>,
    submissions: List<AssignmentSubmission>,
    onCreateAssignmentClick: () -> Unit,
    onGradeSubmission: (AssignmentSubmission, Int, String) -> Unit
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
                    text = "Assignments & Submissions (${assignments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onCreateAssignmentClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Task")
                }
            }
        }

        items(assignments) { assignment ->
            val subsForThis = submissions.filter { it.assignmentId == assignment.id }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = assignment.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = assignment.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Class: ${assignment.className} • Max Marks: ${assignment.maxMarks} • Due: ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(assignment.dueDateMillis))}",
                        fontSize = 12.sp,
                        color = PrimaryLight,
                        fontWeight = FontWeight.Medium
                    )

                    if (subsForThis.isNotEmpty()) {
                        Text(
                            text = "Student Submissions (${subsForThis.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        subsForThis.forEach { sub ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "${sub.studentName} - Submitted", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = sub.content, fontSize = 11.sp, maxLines = 2)
                                    if (sub.status == "GRADED") {
                                        Text(text = "Score: ${sub.gradedScore}/${sub.maxMarks} • Feedback: ${sub.teacherFeedback}", color = AcademicEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

// --- Teacher Dialogs ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGradeCaDialog(
    grade: StudentGrade,
    onDismiss: () -> Unit,
    onSave: (test1: Double, test2: Double, midterm: Double, exam: Double) -> Unit
) {
    var test1 by remember { mutableStateOf(grade.test1Score.toString()) }
    var test2 by remember { mutableStateOf(grade.test2Score.toString()) }
    var midterm by remember { mutableStateOf(grade.midtermScore.toString()) }
    var exam by remember { mutableStateOf(grade.examScore.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Scores: ${grade.studentName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${grade.subjectName} (${grade.className})", fontSize = 12.sp, color = PrimaryLight, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = test1,
                    onValueChange = { test1 = it },
                    label = { Text("1st CA Test (Max 15)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = test2,
                    onValueChange = { test2 = it },
                    label = { Text("2nd CA Test (Max 15)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = midterm,
                    onValueChange = { midterm = it },
                    label = { Text("Midterm Assessment (Max 20)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = exam,
                    onValueChange = { exam = it },
                    label = { Text("Terminal Exam (Max 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        test1.toDoubleOrNull() ?: grade.test1Score,
                        test2.toDoubleOrNull() ?: grade.test2Score,
                        midterm.toDoubleOrNull() ?: grade.midtermScore,
                        exam.toDoubleOrNull() ?: grade.examScore
                    )
                }
            ) {
                Text("Save Scores")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCbtDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, className: String, type: String, duration: Int, passMark: Int, instructions: String, questions: List<CbtQuestion>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var className by remember { mutableStateOf("SS 2 Gold") }
    var duration by remember { mutableStateOf("25") }
    var passMark by remember { mutableStateOf("20") }
    var examType by remember { mutableStateOf("1ST_CA") }

    // Mode Selector: "AI" vs "MANUAL"
    var questionInputMode by remember { mutableStateOf("AI") }

    // AI Generator State
    var aiTopic by remember { mutableStateOf("") }
    var aiQuestionCount by remember { mutableStateOf(5) }
    var aiDifficulty by remember { mutableStateOf("WAEC Standard") }
    var aiMarksPerQuestion by remember { mutableStateOf("5") }
    val isAiGenerating by viewModel.isAiGeneratingCbt.collectAsState()
    val aiError by viewModel.aiCbtError.collectAsState()
    val aiGeneratedPreview = remember { mutableStateListOf<CbtQuestion>() }

    // Manual Builder state
    var currentQuestionText by remember { mutableStateOf("") }
    var currentOptionA by remember { mutableStateOf("") }
    var currentOptionB by remember { mutableStateOf("") }
    var currentOptionC by remember { mutableStateOf("") }
    var currentOptionD by remember { mutableStateOf("") }
    var currentCorrectOption by remember { mutableStateOf("A") }
    var currentMarks by remember { mutableStateOf("5") }
    var currentExplanation by remember { mutableStateOf("") }
    var questionErrorMessage by remember { mutableStateOf<String?>(null) }

    // Question Bank List
    val sampleQuestions = remember {
        mutableStateListOf(
            CbtQuestion(
                examId = 0,
                questionNumber = 1,
                questionText = "What is the derivative of 3x² with respect to x?",
                optionA = "6x",
                optionB = "3x",
                optionC = "6x²",
                optionD = "x³",
                correctOption = "A",
                explanation = "d/dx(3x²) = 3 * 2 * x^(2-1) = 6x.",
                marks = 5
            ),
            CbtQuestion(
                examId = 0,
                questionNumber = 2,
                questionText = "Which SI unit is used to measure electrical resistance?",
                optionA = "Volt",
                optionB = "Ampere",
                optionC = "Ohm (Ω)",
                optionD = "Watt",
                correctOption = "C",
                explanation = "Ohm is the standard unit of electrical resistance in Physics.",
                marks = 5
            )
        )
    }

    // Dynamic Topic Suggestions based on chosen subject
    val quickTopics = remember(subject) {
        when {
            subject.contains("Math", ignoreCase = true) -> listOf(
                "Algebra & Quadratic Equations",
                "Trigonometry & Bearings",
                "Calculus & Derivatives",
                "Statistics & Probability",
                "Logarithms & Indices"
            )
            subject.contains("Physic", ignoreCase = true) -> listOf(
                "Newton's Laws & Force",
                "Current Electricity & Ohm's Law",
                "Light Reflection & Optics",
                "Waves, Sound & Frequency",
                "Work, Energy & Power"
            )
            subject.contains("Chem", ignoreCase = true) -> listOf(
                "Acids, Bases, and Salts",
                "Periodic Table & Chemical Bonding",
                "Organic Chemistry: Hydrocarbons",
                "Electrolysis & Oxidation States",
                "Gas Laws & Mole Concept"
            )
            subject.contains("Bio", ignoreCase = true) -> listOf(
                "Cell Biology & Organelles",
                "Photosynthesis & Respiration",
                "Genetics & Heredity",
                "Digestive & Circulatory System",
                "Ecology & Ecosystems"
            )
            subject.contains("Eng", ignoreCase = true) -> listOf(
                "Subject-Verb Concord & Tenses",
                "Idioms & Figures of Speech",
                "Antonyms & Synonyms",
                "Lexis & Sentence Structure",
                "Comprehension & Vocabulary"
            )
            subject.contains("Comp", ignoreCase = true) || subject.contains("ICT", ignoreCase = true) -> listOf(
                "Computer Hardware & Peripherals",
                "Computer Networking & Internet",
                "Operating Systems & Software",
                "Database & SQL Fundamentals",
                "Cybersecurity & Data Privacy"
            )
            subject.contains("Econ", ignoreCase = true) || subject.contains("Commer", ignoreCase = true) -> listOf(
                "Supply and Demand Elasticity",
                "Market Structures & Inflation",
                "National Income & Fiscal Policy",
                "International Trade & Balance of Payments"
            )
            subject.contains("Gov", ignoreCase = true) || subject.contains("Civic", ignoreCase = true) -> listOf(
                "Arms of Government & Separation of Powers",
                "Constitutional Democracy & Rule of Law",
                "Human Rights & Citizenship",
                "Electoral Systems & Voting"
            )
            else -> listOf(
                "Core Concepts & Definitions",
                "Key Principles & Theories",
                "Problem Solving & Practical Application",
                "Review & Examination Revision"
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryLight.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Quiz, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("Create CBT Assessment", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("AI Generator & Manual Question Bank", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Assessment Meta
                item {
                    Text("1. Test Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryLight)
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Exam / Test Title") },
                        placeholder = { Text("e.g. First Term Mathematics CBT Test") },
                        modifier = Modifier.fillMaxWidth().testTag("cbt_title_input")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            modifier = Modifier.weight(1f).testTag("cbt_subject_input")
                        )
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Target Class") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Timer (Mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = passMark,
                            onValueChange = { passMark = it },
                            label = { Text("Pass Mark") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 2: Question Source Selector (AI vs Manual)
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("2. Question Source", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryLight)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { questionInputMode = "AI" },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (questionInputMode == "AI") PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (questionInputMode == "AI") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("mode_ai_button")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✨ AI Generator", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { questionInputMode = "MANUAL" },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (questionInputMode == "MANUAL") PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (questionInputMode == "MANUAL") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("mode_manual_button")
                        ) {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✍️ Manual Setter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // --- AI GENERATION PANEL ---
                if (questionInputMode == "AI") {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.Psychology, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(20.dp))
                                    Text("Generative AI CBT Question Creator", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryLight)
                                }
                                Text(
                                    text = "Specify any topic in $subject. AI will craft standard multiple-choice questions with 4 options, the correct answer key, and rationales.",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = aiTopic,
                                    onValueChange = { aiTopic = it },
                                    label = { Text("Topic or Concept") },
                                    placeholder = { Text("e.g. $subject topics (e.g. ${quickTopics.firstOrNull() ?: "Newton's Laws"})") },
                                    leadingIcon = { Icon(Icons.Rounded.Topic, contentDescription = null, tint = PrimaryLight) },
                                    trailingIcon = {
                                        if (aiTopic.isNotBlank()) {
                                            IconButton(onClick = { aiTopic = "" }) {
                                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("ai_topic_input")
                                )

                                // Topic Suggestion Chips
                                Text("Suggested Topics for $subject:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    quickTopics.chunked(2).forEach { rowTopics ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            rowTopics.forEach { topicChip ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (aiTopic == topicChip) PrimaryLight.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (aiTopic == topicChip) PrimaryLight else MaterialTheme.colorScheme.outlineVariant
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { aiTopic = topicChip }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(12.dp))
                                                        Text(
                                                            text = topicChip,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = if (aiTopic == topicChip) FontWeight.Bold else FontWeight.Normal,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Configuration: Count, Difficulty, Marks
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Questions Count", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf(3, 5, 10).forEach { cnt ->
                                                val isSel = aiQuestionCount == cnt
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSel) PrimaryLight else MaterialTheme.colorScheme.surface,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PrimaryLight else MaterialTheme.colorScheme.outlineVariant),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { aiQuestionCount = cnt }
                                                ) {
                                                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "$cnt Qs",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Difficulty Level", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("Standard", "Advanced").forEach { diff ->
                                                val fullDiff = if (diff == "Standard") "WAEC Standard" else "Advanced UTME"
                                                val isSel = aiDifficulty == fullDiff
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSel) AcademicEmerald else MaterialTheme.colorScheme.surface,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AcademicEmerald else MaterialTheme.colorScheme.outlineVariant),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { aiDifficulty = fullDiff }
                                                ) {
                                                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = diff,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                aiError?.let { err ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AcademicRose.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AcademicRose, modifier = Modifier.size(16.dp))
                                            Text(err, color = AcademicRose, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.generateAiCbtQuestions(
                                            topic = aiTopic.ifBlank { quickTopics.firstOrNull() ?: subject },
                                            subject = subject,
                                            classLevel = className,
                                            questionCount = aiQuestionCount,
                                            difficulty = aiDifficulty,
                                            marksPerQuestion = aiMarksPerQuestion.toIntOrNull() ?: 5
                                        ) { generatedList ->
                                            aiGeneratedPreview.clear()
                                            aiGeneratedPreview.addAll(generatedList)
                                        }
                                    },
                                    enabled = !isAiGenerating,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("generate_cbt_ai_button")
                                ) {
                                    if (isAiGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI is setting $aiQuestionCount questions...", fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("✨ Generate $aiQuestionCount CBT Questions with AI", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }
                                }

                                // AI Generated Staging Preview
                                if (aiGeneratedPreview.isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✨ AI Generated Preview (${aiGeneratedPreview.size} Qs):",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = PrimaryLight
                                        )

                                        Button(
                                            onClick = {
                                                // Batch add to sampleQuestions
                                                sampleQuestions.addAll(aiGeneratedPreview)
                                                aiGeneratedPreview.clear()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AcademicEmerald),
                                            modifier = Modifier.testTag("add_all_ai_to_bank_button")
                                        ) {
                                            Icon(Icons.Rounded.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("➕ Add All to Exam Bank", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    aiGeneratedPreview.forEachIndexed { pIdx, genQ ->
                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AcademicEmerald.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = AcademicEmerald.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "AI Question #${pIdx + 1} (${genQ.marks} Marks)",
                                                            color = AcademicEmerald,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Button(
                                                            onClick = {
                                                                sampleQuestions.add(genQ)
                                                                aiGeneratedPreview.removeAt(pIdx)
                                                            },
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("➕ Add", fontSize = 10.5.sp)
                                                        }

                                                        IconButton(
                                                            onClick = { aiGeneratedPreview.removeAt(pIdx) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Close, contentDescription = "Discard", tint = AcademicRose, modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }

                                                Text(genQ.questionText, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                                listOf("A" to genQ.optionA, "B" to genQ.optionB, "C" to genQ.optionC, "D" to genQ.optionD).forEach { (k, opt) ->
                                                    val isCorrect = genQ.correctOption == k
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isCorrect) AcademicEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text("[$k]", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = if (isCorrect) AcademicEmerald else MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text(opt, fontSize = 11.sp, fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                                                            if (isCorrect) {
                                                                Text("✓ Key", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AcademicEmerald)
                                                            }
                                                        }
                                                    }
                                                }

                                                if (genQ.explanation.isNotBlank()) {
                                                    Text("💡 ${genQ.explanation}", fontSize = 10.5.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- MANUAL QUESTION SETTER PANEL ---
                if (questionInputMode == "MANUAL") {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Manual Question & Option Inputs", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = PrimaryLight)

                                OutlinedTextField(
                                    value = currentQuestionText,
                                    onValueChange = {
                                        currentQuestionText = it
                                        questionErrorMessage = null
                                    },
                                    label = { Text("Question Text") },
                                    placeholder = { Text("e.g. Calculate the hypotenuse of a right triangle with sides 3cm and 4cm.") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth().testTag("cbt_question_text_input")
                                )

                                // Option A
                                OutlinedTextField(
                                    value = currentOptionA,
                                    onValueChange = { currentOptionA = it },
                                    label = { Text("Option A") },
                                    placeholder = { Text("e.g. 5 cm") },
                                    leadingIcon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (currentCorrectOption == "A") AcademicEmerald else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("A", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (currentCorrectOption == "A") Color.White else MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("cbt_option_a_input")
                                )

                                // Option B
                                OutlinedTextField(
                                    value = currentOptionB,
                                    onValueChange = { currentOptionB = it },
                                    label = { Text("Option B") },
                                    placeholder = { Text("e.g. 7 cm") },
                                    leadingIcon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (currentCorrectOption == "B") AcademicEmerald else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("B", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (currentCorrectOption == "B") Color.White else MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("cbt_option_b_input")
                                )

                                // Option C
                                OutlinedTextField(
                                    value = currentOptionC,
                                    onValueChange = { currentOptionC = it },
                                    label = { Text("Option C") },
                                    placeholder = { Text("e.g. 12 cm") },
                                    leadingIcon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (currentCorrectOption == "C") AcademicEmerald else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("C", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (currentCorrectOption == "C") Color.White else MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("cbt_option_c_input")
                                )

                                // Option D
                                OutlinedTextField(
                                    value = currentOptionD,
                                    onValueChange = { currentOptionD = it },
                                    label = { Text("Option D") },
                                    placeholder = { Text("e.g. 25 cm") },
                                    leadingIcon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (currentCorrectOption == "D") AcademicEmerald else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("D", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (currentCorrectOption == "D") Color.White else MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("cbt_option_d_input")
                                )

                                // Correct Answer Selector Buttons
                                Text(
                                    text = "Designate Correct Answer Key (Click to Select):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("A", "B", "C", "D").forEach { opt ->
                                        val isSelected = currentCorrectOption == opt
                                        Button(
                                            onClick = { currentCorrectOption = opt },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) AcademicEmerald else MaterialTheme.colorScheme.surface,
                                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) AcademicEmerald else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier.weight(1f).testTag("select_correct_opt_$opt")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                }
                                                Text(opt, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentMarks,
                                        onValueChange = { currentMarks = it },
                                        label = { Text("Marks") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = currentExplanation,
                                        onValueChange = { currentExplanation = it },
                                        label = { Text("Explanation (Optional)") },
                                        modifier = Modifier.weight(2f)
                                    )
                                }

                                questionErrorMessage?.let { err ->
                                    Text(text = err, color = AcademicRose, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        if (currentQuestionText.isBlank()) {
                                            questionErrorMessage = "Please type the question text!"
                                            return@FilledTonalButton
                                        }
                                        if (currentOptionA.isBlank() || currentOptionB.isBlank() || currentOptionC.isBlank() || currentOptionD.isBlank()) {
                                            questionErrorMessage = "Please fill all 4 options (A, B, C, D)!"
                                            return@FilledTonalButton
                                        }

                                        sampleQuestions.add(
                                            CbtQuestion(
                                                examId = 0,
                                                questionNumber = sampleQuestions.size + 1,
                                                questionText = currentQuestionText.trim(),
                                                optionA = currentOptionA.trim(),
                                                optionB = currentOptionB.trim(),
                                                optionC = currentOptionC.trim(),
                                                optionD = currentOptionD.trim(),
                                                correctOption = currentCorrectOption,
                                                explanation = currentExplanation.trim(),
                                                marks = currentMarks.toIntOrNull() ?: 5
                                            )
                                        )

                                        // Reset inputs for next question
                                        currentQuestionText = ""
                                        currentOptionA = ""
                                        currentOptionB = ""
                                        currentOptionC = ""
                                        currentOptionD = ""
                                        currentExplanation = ""
                                        questionErrorMessage = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("add_question_to_bank_button")
                                ) {
                                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Question to Exam Bank", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section 3: Questions In Bank Preview
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Questions in Bank (${sampleQuestions.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryLight
                        )
                        Text(
                            text = "Total: ${sampleQuestions.sumOf { it.marks }} Marks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AcademicEmerald
                        )
                    }
                }

                if (sampleQuestions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No questions in bank yet. Use AI generator or manual setter above.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                itemsIndexed(sampleQuestions) { idx, q ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Q${idx + 1} (${q.marks} Marks)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryLight
                                )

                                IconButton(
                                    onClick = { sampleQuestions.removeAt(idx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = AcademicRose, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(text = q.questionText, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)

                            // Display all options with highlighted correct key
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("A" to q.optionA, "B" to q.optionB, "C" to q.optionC, "D" to q.optionD).forEach { (optKey, optText) ->
                                    val isCorrect = q.correctOption == optKey
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCorrect) AcademicEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.dp, AcademicEmerald) else null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "[$optKey]",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isCorrect) AcademicEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = optText,
                                                fontSize = 11.5.sp,
                                                color = if (isCorrect) AcademicEmerald else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isCorrect) {
                                                Text("✓ Correct Key", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AcademicEmerald)
                                            }
                                        }
                                    }
                                }
                            }

                            if (q.explanation.isNotBlank()) {
                                Text(
                                    text = "💡 ${q.explanation}",
                                    fontSize = 10.5.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && sampleQuestions.isNotEmpty()) {
                        onConfirm(
                            title,
                            subject,
                            className,
                            examType,
                            duration.toIntOrNull() ?: 25,
                            passMark.toIntOrNull() ?: 20,
                            "Answer all questions. Select the single best option.",
                            sampleQuestions.toList()
                        )
                    }
                },
                enabled = title.isNotBlank() && sampleQuestions.isNotEmpty(),
                modifier = Modifier.testTag("publish_cbt_button")
            ) {
                Text("Publish CBT (${sampleQuestions.size} Qs)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankManagerDialog(
    exam: CbtExam,
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val questions by viewModel.getQuestionsForExam(exam.id).collectAsState(initial = emptyList())

    var isAddingNew by remember { mutableStateOf(false) }
    var addMode by remember { mutableStateOf("AI") } // "AI" vs "MANUAL"

    // AI Generation inside Bank Manager
    var aiTopic by remember { mutableStateOf("") }
    var aiQuestionCount by remember { mutableStateOf(5) }
    var aiDifficulty by remember { mutableStateOf("WAEC Standard") }
    var aiMarksPerQuestion by remember { mutableStateOf("5") }
    val isAiGenerating by viewModel.isAiGeneratingCbt.collectAsState()
    val aiError by viewModel.aiCbtError.collectAsState()

    // Manual inputs
    var qText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctOpt by remember { mutableStateOf("A") }
    var marks by remember { mutableStateOf("5") }
    var explanation by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Rule, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(18.dp))
                    }
                    Text("CBT Question Bank", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Text(
                    text = "${exam.title} • ${exam.subjectName} (${exam.className})",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bank Items: ${questions.size} Qs • ${questions.sumOf { it.marks }} Marks",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLight
                        )

                        Button(
                            onClick = { isAddingNew = !isAddingNew },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("toggle_add_bank_question_button")
                        ) {
                            Icon(
                                imageVector = if (isAddingNew) Icons.Rounded.Close else Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAddingNew) "Close Adder" else "Add Questions", fontSize = 11.5.sp)
                        }
                    }
                }

                // New Question Input Section (AI or Manual)
                if (isAddingNew) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { addMode = "AI" },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (addMode == "AI") PrimaryLight else MaterialTheme.colorScheme.surface,
                                            contentColor = if (addMode == "AI") Color.White else MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✨ AI Topic Generator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { addMode = "MANUAL" },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (addMode == "MANUAL") PrimaryLight else MaterialTheme.colorScheme.surface,
                                            contentColor = if (addMode == "MANUAL") Color.White else MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✍️ Manual Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (addMode == "AI") {
                                    Text(
                                        text = "Generate questions with AI and add directly to ${exam.subjectName} bank:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    OutlinedTextField(
                                        value = aiTopic,
                                        onValueChange = { aiTopic = it },
                                        label = { Text("Topic or Concept") },
                                        placeholder = { Text("e.g. ${exam.subjectName} topic") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(3, 5, 10).forEach { cnt ->
                                            val isSel = aiQuestionCount == cnt
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) PrimaryLight else MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PrimaryLight else MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { aiQuestionCount = cnt }
                                            ) {
                                                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$cnt Questions",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    aiError?.let { err ->
                                        Text(err, color = AcademicRose, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.generateAiCbtQuestions(
                                                topic = aiTopic.ifBlank { exam.subjectName },
                                                subject = exam.subjectName,
                                                classLevel = exam.className,
                                                questionCount = aiQuestionCount,
                                                difficulty = aiDifficulty,
                                                marksPerQuestion = aiMarksPerQuestion.toIntOrNull() ?: 5
                                            ) { generatedQuestions ->
                                                viewModel.addQuestionsToExamBatch(exam.id, generatedQuestions)
                                                aiTopic = ""
                                                isAddingNew = false
                                            }
                                        },
                                        enabled = !isAiGenerating,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("ai_generate_and_add_to_exam_button")
                                    ) {
                                        if (isAiGenerating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Generating & adding...", fontSize = 11.5.sp)
                                        } else {
                                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("✨ Generate & Add to Bank", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = qText,
                                        onValueChange = {
                                            qText = it
                                            errorMsg = null
                                        },
                                        label = { Text("Question Text") },
                                        placeholder = { Text("Type question...") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = optA,
                                        onValueChange = { optA = it },
                                        label = { Text("Option A") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = optB,
                                        onValueChange = { optB = it },
                                        label = { Text("Option B") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = optC,
                                        onValueChange = { optC = it },
                                        label = { Text("Option C") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = optD,
                                        onValueChange = { optD = it },
                                        label = { Text("Option D") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Select Correct Answer Button:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("A", "B", "C", "D").forEach { opt ->
                                            val isSelected = correctOpt == opt
                                            Button(
                                                onClick = { correctOpt = opt },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSelected) AcademicEmerald else MaterialTheme.colorScheme.surface,
                                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) AcademicEmerald else MaterialTheme.colorScheme.outline
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    if (isSelected) {
                                                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    }
                                                    Text(opt, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = marks,
                                            onValueChange = { marks = it },
                                            label = { Text("Marks") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = explanation,
                                            onValueChange = { explanation = it },
                                            label = { Text("Explanation") },
                                            modifier = Modifier.weight(2f)
                                        )
                                    }

                                    errorMsg?.let { err ->
                                        Text(err, color = AcademicRose, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            if (qText.isBlank()) {
                                                errorMsg = "Question text is required!"
                                                return@Button
                                            }
                                            if (optA.isBlank() || optB.isBlank() || optC.isBlank() || optD.isBlank()) {
                                                errorMsg = "All 4 options (A, B, C, D) are required!"
                                                return@Button
                                            }

                                            viewModel.addQuestionToExam(
                                                examId = exam.id,
                                                questionText = qText.trim(),
                                                optA = optA.trim(),
                                                optB = optB.trim(),
                                                optC = optC.trim(),
                                                optD = optD.trim(),
                                                correctOption = correctOpt,
                                                explanation = explanation.trim(),
                                                marks = marks.toIntOrNull() ?: 5
                                            )

                                            qText = ""
                                            optA = ""
                                            optB = ""
                                            optC = ""
                                            optD = ""
                                            explanation = ""
                                            isAddingNew = false
                                            errorMsg = null
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("save_question_to_bank_button")
                                    ) {
                                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Question to Bank")
                                    }
                                }
                            }
                        }
                    }
                }

                if (questions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No questions in bank yet. Click 'Add Questions' above.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }

                items(questions) { q ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${q.questionNumber} (${q.marks} Marks)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryLight
                                )

                                IconButton(
                                    onClick = { viewModel.deleteQuestionFromExam(q) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = AcademicRose, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(text = q.questionText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                            // Render Options with badges
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("A" to q.optionA, "B" to q.optionB, "C" to q.optionC, "D" to q.optionD).forEach { (optKey, optText) ->
                                    val isCorrect = q.correctOption == optKey
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCorrect) AcademicEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.dp, AcademicEmerald) else null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "[$optKey]",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = if (isCorrect) AcademicEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = optText,
                                                fontSize = 12.sp,
                                                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCorrect) AcademicEmerald else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isCorrect) {
                                                Text("✓ Correct Answer Key", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = AcademicEmerald)
                                            }
                                        }
                                    }
                                }
                            }

                            if (q.explanation.isNotBlank()) {
                                Text(
                                    text = "Explanation: ${q.explanation}",
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CreateAssignmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, className: String, dueMillis: Long, maxMarks: Int, description: String, instructions: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var className by remember { mutableStateOf("SS 2 Gold") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Homework Assignment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Instructions") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            subject,
                            className,
                            System.currentTimeMillis() + (86400000L * 4),
                            20,
                            description,
                            "Submit via student portal before due date."
                        )
                    }
                }
            ) {
                Text("Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
