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
import androidx.compose.ui.platform.LocalContext
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPortalScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allCbtExams by viewModel.allCbtExams.collectAsState()
    val allCbtSubmissions by viewModel.allCbtSubmissions.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val allAssignmentSubmissions by viewModel.allAssignmentSubmissions.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val allReportCards by viewModel.allReportCards.collectAsState()
    val announcements by viewModel.roleAnnouncements.collectAsState()

    val studentId = currentUser?.id ?: "STU-2025-042"
    val studentGrades = allGrades.filter { it.studentId == studentId }
    val studentReportCard = allReportCards.find { it.studentId == studentId } ?: allReportCards.firstOrNull()
    val studentCbtSubmissions = allCbtSubmissions.filter { it.studentId == studentId }

    var selectedAssignmentForSubmission by remember { mutableStateOf<SchoolAssignment?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Student Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = when (currentTab) {
                PortalTab.STUDENT_CBT -> 0
                PortalTab.STUDENT_ASSIGNMENTS -> 1
                PortalTab.STUDENT_REPORT_CARD -> 2
                PortalTab.STUDENT_AI_TUTOR -> 3
                PortalTab.STUDENT_ANNOUNCEMENTS -> 4
                PortalTab.STUDENT_CLASS_CHAT -> 5
                else -> 0
            },
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryLight
        ) {
            Tab(
                selected = currentTab == PortalTab.STUDENT_CBT,
                onClick = { viewModel.selectTab(PortalTab.STUDENT_CBT) },
                text = { Text("CBT Exams", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Quiz, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STUDENT_ASSIGNMENTS,
                onClick = { viewModel.selectTab(PortalTab.STUDENT_ASSIGNMENTS) },
                text = { Text("Homework", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Assignment, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STUDENT_REPORT_CARD,
                onClick = { viewModel.selectTab(PortalTab.STUDENT_REPORT_CARD) },
                text = { Text("Report Sheet", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Assessment, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STUDENT_AI_TUTOR,
                onClick = { viewModel.selectTab(PortalTab.STUDENT_AI_TUTOR) },
                text = { Text("AI Tutor", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STUDENT_ANNOUNCEMENTS,
                onClick = { viewModel.selectTab(PortalTab.STUDENT_ANNOUNCEMENTS) },
                text = { Text("Notices", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Campaign, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.STUDENT_CLASS_CHAT,
                onClick = {
                    viewModel.setChatChannel("CLASS_SS2_GOLD")
                    viewModel.selectTab(PortalTab.STUDENT_CLASS_CHAT)
                },
                text = { Text("Class Room", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Forum, contentDescription = null) }
            )
        }

        when (currentTab) {
            PortalTab.STUDENT_CBT -> {
                StudentCbtPortalContent(
                    exams = allCbtExams,
                    submissions = studentCbtSubmissions,
                    onStartExam = { exam -> viewModel.startCbtExam(exam) }
                )
            }
            PortalTab.STUDENT_ASSIGNMENTS -> {
                StudentAssignmentsContent(
                    assignments = allAssignments,
                    submissions = allAssignmentSubmissions.filter { it.studentId == studentId },
                    onSubmitAnswerClick = { assignment -> selectedAssignmentForSubmission = assignment }
                )
            }
            PortalTab.STUDENT_REPORT_CARD -> {
                StudentReportCardContent(
                    reportCard = studentReportCard,
                    grades = studentGrades,
                    onExportPdf = { rc -> viewModel.exportReportCardPdf(context, rc) }
                )
            }
            PortalTab.STUDENT_AI_TUTOR -> {
                RoleAiAssistantScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    currentRole = SchoolRole.STUDENT
                )
            }
            PortalTab.STUDENT_ANNOUNCEMENTS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Student Announcements & Circulars (${announcements.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(announcements) { ann ->
                        AnnouncementCard(announcement = ann)
                    }
                }
            }
            PortalTab.STUDENT_CLASS_CHAT -> {
                ChatRoomScreen(viewModel = viewModel)
            }
            else -> {}
        }
    }

    selectedAssignmentForSubmission?.let { assignment ->
        SubmitAssignmentDialog(
            assignment = assignment,
            onDismiss = { selectedAssignmentForSubmission = null },
            onSubmit = { solution ->
                viewModel.submitAssignment(assignment, solution)
                selectedAssignmentForSubmission = null
            }
        )
    }
}

@Composable
fun StudentCbtPortalContent(
    exams: List<CbtExam>,
    submissions: List<CbtSubmission>,
    onStartExam: (CbtExam) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Computer-Based Testing (CBT) Center",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Write your official continuous assessment tests and terminal examinations with live instant grading.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Available CBT Assessments (${exams.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(exams) { exam ->
            val hasSubmitted = submissions.any { it.examId == exam.id }
            val pastSub = submissions.find { it.examId == exam.id }

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
                            text = "${exam.durationMinutes} Mins • Total: ${exam.totalMarks} Mks",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = exam.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Subject: ${exam.subjectName} • Teacher: ${exam.teacherName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasSubmitted && pastSub != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pastSub.isPassed) AcademicEmerald.copy(alpha = 0.15f) else AcademicRose.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Completed: ${pastSub.score}/${pastSub.totalMarks} (${String.format(Locale.US, "%.0f%%", pastSub.percentage)})",
                                    color = if (pastSub.isPassed) AcademicEmerald else AcademicRose,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Status: Ready to Take",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcademicEmerald
                            )
                        }

                        Button(
                            onClick = { onStartExam(exam) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasSubmitted) MaterialTheme.colorScheme.secondary else PrimaryLight
                            ),
                            modifier = Modifier.testTag("start_cbt_button_${exam.id}")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasSubmitted) "Retake CBT" else "Start CBT Test")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentsContent(
    assignments: List<SchoolAssignment>,
    submissions: List<AssignmentSubmission>,
    onSubmitAnswerClick: (SchoolAssignment) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Coursework & Homework (${assignments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(assignments) { assignment ->
            val mySubmission = submissions.find { it.assignmentId == assignment.id }
            val isSubmitted = mySubmission != null

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
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = assignment.subjectName,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Due: ${SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(assignment.dueDateMillis))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(text = assignment.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = assignment.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (isSubmitted && mySubmission != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Your Submitted Answer:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF166534)
                                )
                                Text(text = mySubmission.content, fontSize = 12.sp, color = Color(0xFF1F2937))

                                if (mySubmission.status == "GRADED") {
                                    Text(
                                        text = "Teacher Score: ${mySubmission.gradedScore} / ${mySubmission.maxMarks} Marks",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AcademicEmerald,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Feedback: \"${mySubmission.teacherFeedback}\"",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "Status: Awaiting Teacher Grading",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AcademicAmber
                                    )
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { onSubmitAnswerClick(assignment) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Submit Solution")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentReportCardContent(
    reportCard: ReportCard?,
    grades: List<StudentGrade>,
    onExportPdf: (ReportCard) -> Unit
) {
    if (reportCard == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No report card available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Official Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "EXCELLENCE INTERNATIONAL SECONDARY COLLEGE",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Official Terminal Report Sheet • ${reportCard.term} (${reportCard.session})",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { onExportPdf(reportCard) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_button")
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export & Download PDF Report Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Student Profile Summary
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Student: ${reportCard.studentName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Class: ${reportCard.className}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Admission No: ${reportCard.admissionNo}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Position: ${reportCard.classPosition}", fontWeight = FontWeight.Bold, color = AcademicEmerald, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Attendance: ${reportCard.attendancePresent} / ${reportCard.attendanceTotal} Days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Average: ${String.format(Locale.US, "%.1f%%", reportCard.averageScore)}", fontWeight = FontWeight.ExtraBold, color = PrimaryLight, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "Subject Grades Breakdown (${grades.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(grades) { grade ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = grade.subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "1st CA: ${grade.test1Score} | 2nd CA: ${grade.test2Score} | Midterm: ${grade.midtermScore} | Exam: ${grade.examScore}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${grade.totalScore} / 100",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        GradeBadge(grade = grade.gradeLetter)
                    }
                }
            }
        }

        // Remarks
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Class Teacher Remark: \"${reportCard.classTeacherRemark}\"", fontSize = 12.sp)
                    Text(text = "Principal Verdict: \"${reportCard.principalRemark}\"", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// --- Submit Assignment Dialog ---

@Composable
fun SubmitAssignmentDialog(
    assignment: SchoolAssignment,
    onDismiss: () -> Unit,
    onSubmit: (content: String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Solution: ${assignment.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Subject: ${assignment.subjectName} • Teacher: ${assignment.teacherName}",
                    fontSize = 12.sp,
                    color = PrimaryLight,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = assignment.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your answer / solution...") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onSubmit(content)
                    }
                }
            ) {
                Text("Submit Work")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
