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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademiaViewModel
import com.example.ui.viewmodel.AppNavTab

class MainActivity : ComponentActivity() {
    private val viewModel: AcademiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcademiaTrackTheme {
                AcademiaTrackApp(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItem(
    val tab: AppNavTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademiaTrackApp(viewModel: AcademiaViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val overview by viewModel.academicOverview.collectAsState()
    val semesters by viewModel.allSemesters.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val attendanceStats by viewModel.attendanceStats.collectAsState()
    val allScheduleSlots by viewModel.allScheduleSlots.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val assignmentFilter by viewModel.assignmentFilter.collectAsState()
    val allExams by viewModel.allExams.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    // Dialog & Sheet States
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var selectedCourseDetail by remember { mutableStateOf<Course?>(null) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showAddScheduleSlotDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavigationItem(AppNavTab.DASHBOARD, "Overview", Icons.Rounded.Dashboard, Icons.Rounded.DashboardCustomize),
        NavigationItem(AppNavTab.COURSES, "Courses", Icons.Rounded.Class, Icons.Rounded.School),
        NavigationItem(AppNavTab.TIMETABLE, "Timetable", Icons.Rounded.CalendarMonth, Icons.Rounded.CalendarToday),
        NavigationItem(AppNavTab.ASSIGNMENTS, "Tasks", Icons.Rounded.Assignment, Icons.Rounded.AssignmentTurnedIn),
        NavigationItem(AppNavTab.EXAMS_GPA, "Exams & GPA", Icons.Rounded.Analytics, Icons.Rounded.BarChart),
        NavigationItem(AppNavTab.AI_STUDY, "AI Mentor", Icons.Rounded.AutoAwesome, Icons.Rounded.Psychology)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(PrimaryLight, AcademicViolet)
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

                        Text(
                            text = "AcademiaTrack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Quick AI Button
                    FilledTonalIconButton(
                        onClick = { viewModel.selectTab(AppNavTab.AI_STUDY) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AcademicViolet.copy(alpha = 0.15f),
                            contentColor = AcademicViolet
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Mentor",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Profile Avatar
                    IconButton(onClick = { showProfileDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userProfile?.name?.take(1) ?: "A").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryLight,
                            selectedTextColor = PrimaryLight,
                            indicatorColor = PrimaryLight.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                AppNavTab.DASHBOARD -> {
                    val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                    val dayOfWeek = if (currentDay == java.util.Calendar.SUNDAY) 7 else currentDay - 1
                    val todaySlots = allScheduleSlots.filter { it.slot.dayOfWeek == dayOfWeek }

                    DashboardScreen(
                        overview = overview,
                        allCourses = allCourses,
                        attendanceStats = attendanceStats,
                        todaySlots = todaySlots,
                        pendingAssignments = allAssignments.filter { it.assignment.status != "COMPLETED" && it.assignment.status != "GRADED" },
                        upcomingExams = allExams.filter { it.exam.examDateMillis >= System.currentTimeMillis() },
                        onNavigateTab = { viewModel.selectTab(it) },
                        onQuickAddAssignment = { showAddAssignmentDialog = true },
                        onQuickAddCourse = { showAddCourseDialog = true },
                        onQuickOpenAi = { prompt ->
                            viewModel.askAi(prompt)
                            viewModel.selectTab(AppNavTab.AI_STUDY)
                        },
                        onOpenProfile = { showProfileDialog = true }
                    )
                }

                AppNavTab.COURSES -> {
                    CoursesScreen(
                        semesters = semesters,
                        courses = allCourses,
                        attendanceStats = attendanceStats,
                        onSelectCourse = { course -> selectedCourseDetail = course },
                        onAddCourse = { showAddCourseDialog = true },
                        onLogAttendance = { courseId, status -> viewModel.logAttendance(courseId, status) }
                    )
                }

                AppNavTab.TIMETABLE -> {
                    TimetableScreen(
                        slots = allScheduleSlots,
                        courses = allCourses,
                        selectedDay = selectedDay,
                        onSelectDay = { viewModel.selectDay(it) },
                        onAddSlot = { showAddScheduleSlotDialog = true },
                        onDeleteSlot = { viewModel.deleteScheduleSlot(it) }
                    )
                }

                AppNavTab.ASSIGNMENTS -> {
                    AssignmentsScreen(
                        assignments = allAssignments,
                        filter = assignmentFilter,
                        onFilterChange = { viewModel.setAssignmentFilter(it) },
                        onAddAssignment = { showAddAssignmentDialog = true },
                        onUpdateStatus = { assignment, newStatus, score ->
                            viewModel.updateAssignmentStatus(assignment, newStatus, score)
                        },
                        onDeleteAssignment = { viewModel.deleteAssignment(it) }
                    )
                }

                AppNavTab.EXAMS_GPA -> {
                    ExamsAndGpaScreen(
                        userProfile = userProfile,
                        courses = allCourses,
                        exams = allExams,
                        onAddExam = { showAddExamDialog = true },
                        onDeleteExam = { viewModel.deleteExam(it) }
                    )
                }

                AppNavTab.AI_STUDY -> {
                    AiStudyHubScreen(
                        aiState = aiState,
                        notes = allNotes,
                        onAskAi = { viewModel.askAi(it) },
                        onSaveNote = { title, content ->
                            viewModel.addNote(
                                courseId = null,
                                title = title,
                                content = content,
                                tags = "AI Study Guide"
                            )
                        },
                        onDeleteNote = { viewModel.deleteNote(it) }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    if (showAddCourseDialog) {
        AddCourseDialog(
            semesters = semesters,
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { semesterId, name, code, instructor, room, credits, colorHex, targetGrade, currentScore, syllabusNotes, attendanceThreshold ->
                viewModel.addCourse(
                    semesterId = semesterId,
                    name = name,
                    code = code,
                    instructor = instructor,
                    room = room,
                    credits = credits,
                    colorHex = colorHex,
                    targetGrade = targetGrade,
                    currentScore = currentScore,
                    syllabusNotes = syllabusNotes,
                    attendanceThreshold = attendanceThreshold
                )
            }
        )
    }

    selectedCourseDetail?.let { course ->
        CourseDetailDialog(
            course = course,
            attendanceStats = attendanceStats[course.id],
            onDismiss = { selectedCourseDetail = null },
            onLogAttendance = { courseId, status -> viewModel.logAttendance(courseId, status) },
            onDeleteCourse = { c -> viewModel.deleteCourse(c) }
        )
    }

    if (showAddAssignmentDialog) {
        AddAssignmentDialog(
            courses = allCourses,
            onDismiss = { showAddAssignmentDialog = false },
            onConfirm = { courseId, title, description, dueDateMillis, priority, maxScore, subtasks ->
                viewModel.addAssignment(
                    courseId = courseId,
                    title = title,
                    description = description,
                    dueDateMillis = dueDateMillis,
                    priority = priority,
                    maxScore = maxScore,
                    subtasks = subtasks
                )
            }
        )
    }

    if (showAddExamDialog) {
        AddExamDialog(
            courses = allCourses,
            onDismiss = { showAddExamDialog = false },
            onConfirm = { courseId, title, examDateMillis, durationMinutes, room, weightPercentage, targetScore, topics ->
                viewModel.addExam(
                    courseId = courseId,
                    title = title,
                    examDateMillis = examDateMillis,
                    durationMinutes = durationMinutes,
                    room = room,
                    weightPercentage = weightPercentage,
                    targetScore = targetScore,
                    topics = topics
                )
            }
        )
    }

    if (showAddScheduleSlotDialog) {
        AddScheduleSlotDialog(
            courses = allCourses,
            defaultDay = selectedDay,
            onDismiss = { showAddScheduleSlotDialog = false },
            onConfirm = { courseId, dayOfWeek, startTime, endTime, room, slotType ->
                viewModel.addScheduleSlot(
                    courseId = courseId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    slotType = slotType
                )
            }
        )
    }

    if (showProfileDialog) {
        ProfileDialog(
            userProfile = userProfile,
            onDismiss = { showProfileDialog = false },
            onSave = { name, studentId, major, university, targetCgpa ->
                viewModel.updateProfile(name, studentId, major, university, targetCgpa)
            }
        )
    }
}
