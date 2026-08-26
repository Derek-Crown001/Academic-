package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    var showCreateCbtDialog by remember { mutableStateOf(false) }
    var showCreateAssignmentDialog by remember { mutableStateOf(false) }
    var selectedGradeStudent by remember { mutableStateOf<StudentGrade?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Teacher Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = when (currentTab) {
                PortalTab.TEACHER_DASHBOARD -> 0
                PortalTab.CA_GRADING -> 1
                PortalTab.CBT_CREATOR -> 2
                PortalTab.ASSIGNMENT_MANAGER -> 3
                PortalTab.STAFF_CHAT -> 4
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
                selected = currentTab == PortalTab.CA_GRADING,
                onClick = { viewModel.selectTab(PortalTab.CA_GRADING) },
                text = { Text("CA & Grades", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.FactCheck, contentDescription = null) }
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
                    subjects = allSubjects.filter { it.teacherId == currentUser?.id || it.teacherName.contains(currentUser?.name ?: "xyz") },
                    cbtExams = allCbtExams,
                    assignments = allAssignments,
                    onNavigateToTab = { viewModel.selectTab(it) }
                )
            }
            PortalTab.CA_GRADING -> {
                TeacherCaGradingContent(
                    grades = allGrades,
                    onEditGrade = { grade -> selectedGradeStudent = grade }
                )
            }
            PortalTab.CBT_CREATOR -> {
                TeacherCbtContent(
                    cbtExams = allCbtExams,
                    onCreateCbtClick = { showCreateCbtDialog = true },
                    onStartExamPreview = { viewModel.startCbtExam(it) }
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
            PortalTab.STAFF_CHAT -> {
                ChatRoomScreen(viewModel = viewModel)
            }
            else -> {}
        }
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
    subjects: List<SchoolSubject>,
    cbtExams: List<CbtExam>,
    assignments: List<SchoolAssignment>,
    onNavigateToTab: (PortalTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        text = "Manage continuous assessment grading, create CBT examinations, and assess student homework.",
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
                        Icon(Icons.Rounded.Calculate, contentDescription = null, tint = PrimaryLight)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Continuous Assessment Score Entry", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Input Test 1 (15), Test 2 (15), Midterm (20), and Exam (50) marks.",
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
    onEditGrade: (StudentGrade) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Continuous Assessment (CA) Score Sheets (${grades.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(grades) { grade ->
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
    onStartExamPreview: (CbtExam) -> Unit
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
                    text = "CBT Exams & Test Center (${cbtExams.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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
                            text = "${exam.durationMinutes} Minutes • Pass: ${exam.passMark} Mks",
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

                        OutlinedButton(
                            onClick = { onStartExamPreview(exam) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview Test")
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
    var qText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctOpt by remember { mutableStateOf("A") }

    val sampleQuestions = remember {
        mutableStateListOf(
            CbtQuestion(
                examId = 0,
                questionNumber = 1,
                questionText = "What is the result of differentiating 3x² with respect to x?",
                optionA = "6x",
                optionB = "3x",
                optionC = "6x²",
                optionD = "x³",
                correctOption = "A",
                explanation = "d/dx(3x²) = 3 * 2 * x^(2-1) = 6x.",
                marks = 10
            ),
            CbtQuestion(
                examId = 0,
                questionNumber = 2,
                questionText = "Which SI unit is used to measure electric resistance?",
                optionA = "Volt",
                optionB = "Ampere",
                optionC = "Ohm (Ω)",
                optionD = "Watt",
                correctOption = "C",
                explanation = "Ohm is the standard unit of electrical resistance.",
                marks = 10
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New CBT Assessment", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Exam / Test Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. Mathematics)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Timer (Mins)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = passMark,
                            onValueChange = { passMark = it },
                            label = { Text("Pass Mark") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("Pre-loaded Questions (${sampleQuestions.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    sampleQuestions.forEachIndexed { idx, q ->
                        Text("${idx + 1}. ${q.questionText} (Ans: ${q.correctOption})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
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
                            "EXAM",
                            duration.toIntOrNull() ?: 25,
                            passMark.toIntOrNull() ?: 20,
                            "Answer all questions. Calculators permitted.",
                            sampleQuestions.toList()
                        )
                    }
                }
            ) {
                Text("Publish CBT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
