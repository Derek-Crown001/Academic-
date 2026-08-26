package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.AcademiaRepository
import com.example.data.repository.AcademicOverview
import com.example.data.repository.CourseAttendanceStats
import com.example.service.gemini.GeminiStudyService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab {
    DASHBOARD,
    COURSES,
    TIMETABLE,
    ASSIGNMENTS,
    EXAMS_GPA,
    AI_STUDY
}

data class AiAssistantState(
    val isLoading: Boolean = false,
    val query: String = "",
    val response: String? = null,
    val error: String? = null
)

class AcademiaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AcademiaDatabase.getDatabase(application, viewModelScope)
    private val repository = AcademiaRepository(database.academiaDao())

    // Selected Navigation Tab
    private val _selectedTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val selectedTab: StateFlow<AppNavTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: AppNavTab) {
        _selectedTab.value = tab
    }

    // Academic Overview Stream
    val academicOverview: StateFlow<AcademicOverview> = repository.academicOverview
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AcademicOverview(
                userProfile = null,
                currentSemester = null,
                courses = emptyList(),
                totalCredits = 0,
                currentSgpa = 4.0,
                overallAttendancePercent = 100.0,
                pendingAssignmentsCount = 0,
                upcomingExamsCount = 0,
                nextClassSlot = null
            )
        )

    // Semesters & Courses
    val allSemesters: StateFlow<List<Semester>> = repository.allSemesters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Course Attendance Stats
    val attendanceStats: StateFlow<Map<Long, CourseAttendanceStats>> = repository.getAllCoursesAttendanceStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Schedule Slots
    val allScheduleSlots: StateFlow<List<ScheduleSlotWithCourse>> = repository.allScheduleSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDay = MutableStateFlow(1) // 1=Mon...7=Sun
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    // Assignments
    val allAssignments: StateFlow<List<AssignmentWithCourse>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _assignmentFilter = MutableStateFlow("ALL") // "ALL", "PENDING", "IN_PROGRESS", "COMPLETED", "GRADED"
    val assignmentFilter: StateFlow<String> = _assignmentFilter.asStateFlow()

    fun setAssignmentFilter(filter: String) {
        _assignmentFilter.value = filter
    }

    // Exams
    val allExams: StateFlow<List<ExamWithCourse>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes
    val allNotes: StateFlow<List<AcademicNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Profile
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI Study Assistant State
    private val _aiState = MutableStateFlow(AiAssistantState())
    val aiState: StateFlow<AiAssistantState> = _aiState.asStateFlow()

    fun askAi(prompt: String) {
        if (prompt.isBlank()) return
        _aiState.value = _aiState.value.copy(isLoading = true, query = prompt, error = null)
        viewModelScope.launch {
            val result = GeminiStudyService.generateStudyAdvice(prompt)
            result.onSuccess { responseText ->
                _aiState.value = _aiState.value.copy(isLoading = false, response = responseText)
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(isLoading = false, error = err.message ?: "Failed to generate AI response")
            }
        }
    }

    fun clearAiResponse() {
        _aiState.value = AiAssistantState()
    }

    // --- CRUD Actions ---

    // Course
    fun addCourse(
        semesterId: Long,
        name: String,
        code: String,
        instructor: String,
        room: String,
        credits: Int,
        colorHex: String,
        targetGrade: String,
        currentScore: Double,
        syllabusNotes: String,
        attendanceThreshold: Int
    ) {
        viewModelScope.launch {
            repository.insertCourse(
                Course(
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
            )
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    // Assignment
    fun addAssignment(
        courseId: Long,
        title: String,
        description: String,
        dueDateMillis: Long,
        priority: String,
        maxScore: Double,
        subtasks: String
    ) {
        viewModelScope.launch {
            repository.insertAssignment(
                Assignment(
                    courseId = courseId,
                    title = title,
                    description = description,
                    dueDateMillis = dueDateMillis,
                    priority = priority,
                    status = "PENDING",
                    maxScore = maxScore,
                    subtasks = subtasks
                )
            )
        }
    }

    fun updateAssignmentStatus(assignment: Assignment, newStatus: String, score: Double? = null) {
        viewModelScope.launch {
            repository.updateAssignment(
                assignment.copy(
                    status = newStatus,
                    scoreObtained = if (score != null) score else assignment.scoreObtained
                )
            )
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    // Exam
    fun addExam(
        courseId: Long,
        title: String,
        examDateMillis: Long,
        durationMinutes: Int,
        room: String,
        weightPercentage: Double,
        targetScore: Double,
        topics: String
    ) {
        viewModelScope.launch {
            repository.insertExam(
                Exam(
                    courseId = courseId,
                    title = title,
                    examDateMillis = examDateMillis,
                    durationMinutes = durationMinutes,
                    room = room,
                    weightPercentage = weightPercentage,
                    targetScore = targetScore,
                    topics = topics
                )
            )
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            repository.deleteExam(exam)
        }
    }

    // Timetable Slot
    fun addScheduleSlot(
        courseId: Long,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        room: String,
        slotType: String
    ) {
        viewModelScope.launch {
            repository.insertScheduleSlot(
                ScheduleSlot(
                    courseId = courseId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    slotType = slotType
                )
            )
        }
    }

    fun deleteScheduleSlot(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.deleteScheduleSlot(slot)
        }
    }

    // Attendance
    fun logAttendance(courseId: Long, status: String, note: String = "") {
        viewModelScope.launch {
            repository.insertAttendanceLog(
                AttendanceLog(
                    courseId = courseId,
                    dateMillis = System.currentTimeMillis(),
                    status = status,
                    note = note
                )
            )
        }
    }

    // Note
    fun addNote(courseId: Long?, title: String, content: String, tags: String) {
        viewModelScope.launch {
            repository.insertNote(
                AcademicNote(
                    courseId = courseId,
                    title = title,
                    content = content,
                    tags = tags,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: AcademicNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Profile
    fun updateProfile(name: String, studentId: String, major: String, university: String, targetCgpa: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateUserProfile(
                current.copy(
                    name = name,
                    studentId = studentId,
                    major = major,
                    university = university,
                    targetCgpa = targetCgpa
                )
            )
        }
    }
}
