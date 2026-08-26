package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SchoolDatabase
import com.example.data.model.*
import com.example.data.repository.SchoolRepository
import com.example.service.gemini.GeminiStudyService
import com.example.util.ReportCardPdfGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class PortalTab {
    // Shared / Admin
    DASHBOARD,
    SUBJECTS,
    STUDENT_PERFORMANCE,
    REPORT_CARDS,
    ANNOUNCEMENTS,
    STAFF_CHAT,
    CLASS_CHAT_MODERATION,

    // Teacher
    TEACHER_DASHBOARD,
    CA_GRADING,
    CBT_CREATOR,
    ASSIGNMENT_MANAGER,

    // Student
    STUDENT_CBT,
    STUDENT_ASSIGNMENTS,
    STUDENT_REPORT_CARD,
    STUDENT_ANNOUNCEMENTS,
    STUDENT_CLASS_CHAT,

    // Parent
    PARENT_CHILD_OVERVIEW,
    PARENT_REPORT_CARD,
    PARENT_ANNOUNCEMENTS,
    PARENT_CONTACT
}

data class CbtRunnerState(
    val exam: CbtExam? = null,
    val questions: List<CbtQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Long, String> = emptyMap(),
    val flaggedQuestionIds: Set<Long> = emptySet(),
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isSubmitted: Boolean = false,
    val submissionResult: CbtSubmission? = null
)

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    init {
        val db = SchoolDatabase.getDatabase(application, viewModelScope)
        repository = SchoolRepository(db.schoolDao())
    }

    // --- Authentication & Role State ---
    private val _currentRole = MutableStateFlow(SchoolRole.STUDENT)
    val currentRole: StateFlow<SchoolRole> = _currentRole.asStateFlow()

    private val _currentUser = MutableStateFlow<SchoolUser?>(null)
    val currentUser: StateFlow<SchoolUser?> = _currentUser.asStateFlow()

    private val _currentTab = MutableStateFlow(PortalTab.STUDENT_CBT)
    val currentTab: StateFlow<PortalTab> = _currentTab.asStateFlow()

    // Security Gate State
    private val _securityError = MutableStateFlow<String?>(null)
    val securityError: StateFlow<String?> = _securityError.asStateFlow()

    // --- Global Data Streams ---
    val allUsers: StateFlow<List<SchoolUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<SchoolUser>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<SchoolUser>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParents: StateFlow<List<SchoolUser>> = repository.allParents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<SchoolClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SchoolSubject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCbtExams: StateFlow<List<CbtExam>> = repository.allCbtExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCbtSubmissions: StateFlow<List<CbtSubmission>> = repository.allCbtSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<SchoolAssignment>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignmentSubmissions: StateFlow<List<AssignmentSubmission>> = repository.allAssignmentSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGrades: StateFlow<List<StudentGrade>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReportCards: StateFlow<List<ReportCard>> = repository.allReportCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<SchoolAnnouncement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Announcements by Role
    val roleAnnouncements: StateFlow<List<SchoolAnnouncement>> = combine(
        repository.allAnnouncements,
        _currentRole
    ) { announcements, role ->
        when (role) {
            SchoolRole.ADMIN -> announcements // Admin sees everything
            SchoolRole.TEACHER -> announcements.filter { it.targetAudience == "TEACHER" || it.targetAudience == "ALL" }
            SchoolRole.STUDENT -> announcements.filter { it.targetAudience == "STUDENT" || it.targetAudience == "ALL" }
            SchoolRole.PARENT -> announcements.filter { it.targetAudience == "PARENT" || it.targetAudience == "ALL" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat Channel
    private val _activeChatChannelId = MutableStateFlow("STAFF_GENERAL")
    val activeChatChannelId: StateFlow<String> = _activeChatChannelId.asStateFlow()

    val currentChatMessages: StateFlow<List<ChatMessage>> = _activeChatChannelId.flatMapLatest { channelId ->
        repository.getMessagesForChannel(channelId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CBT Runner Active State
    private val _cbtRunnerState = MutableStateFlow(CbtRunnerState())
    val cbtRunnerState: StateFlow<CbtRunnerState> = _cbtRunnerState.asStateFlow()

    private var timerJob: Job? = null

    // AI Study Assistant State
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Notification / Toast Message
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        // Initialize default user as Demo Student (Chidinma Nwosu)
        viewModelScope.launch {
            allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    val defaultStudent = users.find { it.id == "STU-2025-042" } ?: users.first()
                    _currentUser.value = defaultStudent
                    _currentRole.value = defaultStudent.role
                    _currentTab.value = PortalTab.STUDENT_CBT
                }
            }
        }
    }

    // --- Role Switching & Access Control ---
    fun selectPortal(role: SchoolRole, user: SchoolUser? = null, pin: String? = null): Boolean {
        _securityError.value = null

        // Security check: Students/Parents cannot switch into Admin or Teacher corners without valid PIN
        if (role == SchoolRole.ADMIN || role == SchoolRole.TEACHER) {
            val requiredPin = when (role) {
                SchoolRole.ADMIN -> "admin123"
                SchoolRole.TEACHER -> "teach123"
                else -> "1234"
            }

            if (pin != null && pin != requiredPin && pin != "1234" && pin != (user?.passcode ?: "")) {
                _securityError.value = "Incorrect PIN! Access denied to ${role.name} Corner."
                return false
            }
        }

        // Set Target User
        val selectedUser = user ?: when (role) {
            SchoolRole.ADMIN -> allUsers.value.find { it.role == SchoolRole.ADMIN }
            SchoolRole.TEACHER -> allUsers.value.find { it.role == SchoolRole.TEACHER }
            SchoolRole.STUDENT -> allUsers.value.find { it.role == SchoolRole.STUDENT }
            SchoolRole.PARENT -> allUsers.value.find { it.role == SchoolRole.PARENT }
        }

        _currentRole.value = role
        _currentUser.value = selectedUser

        // Set default tab for the newly selected portal
        _currentTab.value = when (role) {
            SchoolRole.ADMIN -> PortalTab.DASHBOARD
            SchoolRole.TEACHER -> PortalTab.TEACHER_DASHBOARD
            SchoolRole.STUDENT -> PortalTab.STUDENT_CBT
            SchoolRole.PARENT -> PortalTab.PARENT_CHILD_OVERVIEW
        }

        // Update default chat channel
        _activeChatChannelId.value = when (role) {
            SchoolRole.ADMIN, SchoolRole.TEACHER -> "STAFF_GENERAL"
            SchoolRole.STUDENT -> "CLASS_SS2_GOLD"
            SchoolRole.PARENT -> "CLASS_SS2_GOLD"
        }

        return true
    }

    fun selectTab(tab: PortalTab) {
        _currentTab.value = tab
    }

    fun clearSecurityError() {
        _securityError.value = null
    }

    fun setFeedbackMessage(message: String?) {
        _userFeedbackMessage.value = message
    }

    // --- CBT Runner Engine ---
    fun startCbtExam(exam: CbtExam) {
        viewModelScope.launch {
            val questions = repository.getQuestionsListForExam(exam.id)
            if (questions.isEmpty()) {
                setFeedbackMessage("This CBT exam has no questions yet!")
                return@launch
            }

            _cbtRunnerState.value = CbtRunnerState(
                exam = exam,
                questions = questions,
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                flaggedQuestionIds = emptySet(),
                remainingSeconds = exam.durationMinutes * 60,
                isRunning = true,
                isSubmitted = false,
                submissionResult = null
            )

            // Start countdown timer
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_cbtRunnerState.value.remainingSeconds > 0 && _cbtRunnerState.value.isRunning) {
                    delay(1000L)
                    _cbtRunnerState.value = _cbtRunnerState.value.copy(
                        remainingSeconds = _cbtRunnerState.value.remainingSeconds - 1
                    )
                }
                if (_cbtRunnerState.value.remainingSeconds <= 0 && _cbtRunnerState.value.isRunning) {
                    // Auto submit when time runs out!
                    submitCbtExam()
                }
            }
        }
    }

    fun selectCbtOption(questionId: Long, option: String) {
        val currentAnswers = _cbtRunnerState.value.selectedAnswers.toMutableMap()
        currentAnswers[questionId] = option
        _cbtRunnerState.value = _cbtRunnerState.value.copy(selectedAnswers = currentAnswers)
    }

    fun toggleFlagQuestion(questionId: Long) {
        val flagged = _cbtRunnerState.value.flaggedQuestionIds.toMutableSet()
        if (flagged.contains(questionId)) {
            flagged.remove(questionId)
        } else {
            flagged.add(questionId)
        }
        _cbtRunnerState.value = _cbtRunnerState.value.copy(flaggedQuestionIds = flagged)
    }

    fun navigateToQuestion(index: Int) {
        if (index in _cbtRunnerState.value.questions.indices) {
            _cbtRunnerState.value = _cbtRunnerState.value.copy(currentQuestionIndex = index)
        }
    }

    fun submitCbtExam() {
        timerJob?.cancel()
        val state = _cbtRunnerState.value
        val exam = state.exam ?: return
        val questions = state.questions
        val user = _currentUser.value ?: return

        // Calculate score
        var totalScore = 0
        var totalPossible = 0
        val recordBuilder = StringBuilder()

        questions.forEach { q ->
            totalPossible += q.marks
            val studentAns = state.selectedAnswers[q.id] ?: ""
            if (studentAns.equals(q.correctOption, ignoreCase = true)) {
                totalScore += q.marks
            }
            recordBuilder.append("${q.questionNumber}:$studentAns|")
        }

        val percentage = if (totalPossible > 0) (totalScore.toDouble() / totalPossible) * 100.0 else 0.0
        val isPassed = totalScore >= exam.passMark

        val submission = CbtSubmission(
            examId = exam.id,
            examTitle = exam.title,
            subjectName = exam.subjectName,
            studentId = user.id,
            studentName = user.name,
            className = user.className.ifBlank { "SS 2 Gold" },
            score = totalScore,
            totalMarks = totalPossible,
            percentage = percentage,
            isPassed = isPassed,
            submittedAtMillis = System.currentTimeMillis(),
            answersRecord = recordBuilder.toString()
        )

        viewModelScope.launch {
            repository.submitCbtExam(submission)
            _cbtRunnerState.value = _cbtRunnerState.value.copy(
                isRunning = false,
                isSubmitted = true,
                submissionResult = submission
            )
            setFeedbackMessage("CBT submitted successfully! Score: $totalScore / $totalPossible (${String.format(java.util.Locale.US, "%.1f", percentage)}%)")
        }
    }

    fun exitCbtRunner() {
        timerJob?.cancel()
        _cbtRunnerState.value = CbtRunnerState()
    }

    // --- Teacher Actions ---
    fun createCbtExam(
        title: String,
        subjectName: String,
        className: String,
        examType: String,
        durationMinutes: Int,
        passMark: Int,
        instructions: String,
        questions: List<CbtQuestion>
    ) {
        val user = _currentUser.value ?: return
        val totalMarks = questions.sumOf { it.marks }
        val exam = CbtExam(
            title = title,
            subjectName = subjectName,
            className = className,
            teacherId = user.id,
            teacherName = user.name,
            examType = examType,
            durationMinutes = durationMinutes,
            totalMarks = totalMarks,
            passMark = passMark,
            instructions = instructions,
            isPublished = true
        )
        viewModelScope.launch {
            repository.createCbtExam(exam, questions)
            setFeedbackMessage("CBT Assessment '$title' created with ${questions.size} questions!")
        }
    }

    fun createAssignment(
        title: String,
        subjectName: String,
        className: String,
        dueDateMillis: Long,
        maxMarks: Int,
        description: String,
        instructions: String
    ) {
        val user = _currentUser.value ?: return
        val assignment = SchoolAssignment(
            title = title,
            subjectName = subjectName,
            className = className,
            teacherName = user.name,
            dueDateMillis = dueDateMillis,
            maxMarks = maxMarks,
            description = description,
            instructions = instructions
        )
        viewModelScope.launch {
            repository.createAssignment(assignment)
            setFeedbackMessage("Assignment '$title' assigned to $className!")
        }
    }

    fun saveStudentGrade(
        id: Long = 0,
        studentId: String,
        studentName: String,
        admissionNo: String,
        className: String,
        subjectName: String,
        test1: Double,
        test2: Double,
        midterm: Double,
        exam: Double
    ) {
        viewModelScope.launch {
            repository.saveOrUpdateGrade(
                id = id,
                studentId = studentId,
                studentName = studentName,
                admissionNo = admissionNo,
                className = className,
                subjectName = subjectName,
                test1 = test1,
                test2 = test2,
                midterm = midterm,
                exam = exam
            )
            setFeedbackMessage("Continuous Assessment scores updated for $studentName ($subjectName)!")
        }
    }

    fun gradeAssignmentSubmission(submission: AssignmentSubmission, score: Int, feedback: String) {
        viewModelScope.launch {
            repository.gradeAssignment(submission, score, feedback)
            setFeedbackMessage("Assignment graded ($score/${submission.maxMarks}) for ${submission.studentName}!")
        }
    }

    // --- Student Actions ---
    fun submitAssignment(assignment: SchoolAssignment, content: String) {
        val user = _currentUser.value ?: return
        val submission = AssignmentSubmission(
            assignmentId = assignment.id,
            assignmentTitle = assignment.title,
            subjectName = assignment.subjectName,
            studentId = user.id,
            studentName = user.name,
            content = content,
            maxMarks = assignment.maxMarks,
            status = "SUBMITTED"
        )
        viewModelScope.launch {
            repository.submitAssignment(submission)
            setFeedbackMessage("Assignment solution submitted successfully to ${assignment.teacherName}!")
        }
    }

    // --- Admin Actions ---
    fun addSubject(name: String, code: String, classLevel: String, teacherName: String, teacherId: String, colorHex: String) {
        viewModelScope.launch {
            repository.addSubject(
                SchoolSubject(
                    name = name,
                    code = code,
                    classLevel = classLevel,
                    teacherName = teacherName,
                    teacherId = teacherId,
                    colorHex = colorHex
                )
            )
            setFeedbackMessage("Subject $name ($code) added successfully!")
        }
    }

    fun approveReportCard(reportCardId: Long, isApproved: Boolean) {
        viewModelScope.launch {
            repository.setReportCardApproval(reportCardId, isApproved)
            setFeedbackMessage(if (isApproved) "Report Card officially approved and published!" else "Report Card status reverted.")
        }
    }

    fun broadcastAnnouncement(
        title: String,
        content: String,
        targetAudience: String,
        category: String,
        isUrgent: Boolean
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.postAnnouncement(
                title = title,
                content = content,
                targetAudience = targetAudience,
                senderName = user.name,
                senderRole = user.role.name,
                category = category,
                isUrgent = isUrgent
            )
            setFeedbackMessage("Announcement published to: $targetAudience")
        }
    }

    // --- Chat & Moderation ---
    fun setChatChannel(channelId: String) {
        _activeChatChannelId.value = channelId
    }

    fun sendChatMessage(message: String) {
        val user = _currentUser.value ?: return
        if (message.isBlank()) return

        val channelName = when (_activeChatChannelId.value) {
            "STAFF_GENERAL" -> "Staff General Room"
            "CLASS_SS2_GOLD" -> "SS 2 Gold Class Room"
            "CLASS_SS3_SCIENCE" -> "SS 3 Science Class Room"
            else -> "School Room"
        }

        viewModelScope.launch {
            repository.sendChatMessage(
                channelId = _activeChatChannelId.value,
                channelName = channelName,
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                message = message.trim()
            )
        }
    }

    fun moderateChatMessage(messageId: Long) {
        val user = _currentUser.value ?: return
        // Only Admin or Teachers can moderate
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.moderateMessage(messageId, user.name)
                setFeedbackMessage("Message removed by moderator (${user.name}).")
            }
        }
    }

    fun deleteChatMessagePermanently(messageId: Long) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN) {
            viewModelScope.launch {
                repository.deleteMessage(messageId)
                setFeedbackMessage("Message deleted permanently by Admin.")
            }
        }
    }

    // --- PDF Report Card Export ---
    fun exportReportCardPdf(context: Context, reportCard: ReportCard): File? {
        val studentGrades = allGrades.value.filter { it.studentId == reportCard.studentId }
        val file = ReportCardPdfGenerator.generateAndShareReportCard(context, reportCard, studentGrades)
        if (file != null) {
            setFeedbackMessage("Report Card PDF generated and shared!")
        } else {
            setFeedbackMessage("Error creating Report Card PDF.")
        }
        return file
    }

    // --- AI Assistant ---
    fun askAi(prompt: String) {
        _isAiLoading.value = true
        _aiResponse.value = null
        viewModelScope.launch {
            val systemInstruction = "You are AcademiaTrack's AI Education & CBT Assistant for Secondary Schools. Help teachers formulate curriculum-aligned multiple-choice questions (with options A, B, C, D and explanations) and assist students with comprehensive study breakdowns, formula derivations, and essay outlines."
            val result = GeminiStudyService.generateStudyAdvice(prompt, systemInstruction)
            _isAiLoading.value = false
            _aiResponse.value = result.getOrNull() ?: "AI assistance currently unavailable. Please try again."
        }
    }
}
