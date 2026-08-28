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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PortalTab {
    // Shared / Admin
    DASHBOARD,
    CLASSES,
    SUBJECTS,
    STUDENT_PERFORMANCE,
    REPORT_CARDS,
    ANNOUNCEMENTS,
    STAFF_CHAT,
    CLASS_CHAT_MODERATION,
    STAFF_ATTENDANCE,
    SCHOOL_SETTINGS,
    ADMIN_AI_ASSISTANT,

    // Teacher
    TEACHER_DASHBOARD,
    CLASS_STUDENTS,
    CA_GRADING,
    CLASS_REGISTER,
    SUBJECT_MANAGER,
    CBT_CREATOR,
    ASSIGNMENT_MANAGER,
    TEACHER_AI_ASSISTANT,

    // Student
    STUDENT_CBT,
    STUDENT_ASSIGNMENTS,
    STUDENT_REPORT_CARD,
    STUDENT_ANNOUNCEMENTS,
    STUDENT_CLASS_CHAT,
    STUDENT_STUDY_ROOM,
    STUDENT_AI_TUTOR,

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
    val totalDurationSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isSubmitted: Boolean = false,
    val isAutoSubmitted: Boolean = false,
    val isSubmitting: Boolean = false,
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

    // Firebase Authentication State
    private val authService = com.example.service.auth.FirebaseAuthService()
    private val _isAuthenticated = MutableStateFlow(true)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

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

    // School Profile (Multipurpose Name, Motto, Session, Term)
    val schoolProfile: StateFlow<SchoolProfile> = repository.schoolProfile
        .map { it ?: SchoolProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolProfile())

    // Teacher Attendance & Time Tracking
    val allTeacherAttendance: StateFlow<List<TeacherAttendance>> = repository.allTeacherAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDateString: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    val currentTeacherAttendance: StateFlow<TeacherAttendance?> = combine(
        allTeacherAttendance,
        _currentUser
    ) { attendances, user ->
        if (user != null && user.role == SchoolRole.TEACHER) {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            attendances.find { it.teacherId == user.id && it.dateString == today }
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    // Active Chat Channel & Rooms
    val allChatRooms: StateFlow<List<ChatRoom>> = repository.allChatRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatChannelId = MutableStateFlow("STAFF_GENERAL")
    val activeChatChannelId: StateFlow<String> = _activeChatChannelId.asStateFlow()

    val activeChatRoom: StateFlow<ChatRoom?> = combine(
        allChatRooms,
        _activeChatChannelId
    ) { rooms, channelId ->
        rooms.find { it.id == channelId } ?: rooms.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _chatSearchQuery = MutableStateFlow("")
    val chatSearchQuery: StateFlow<String> = _chatSearchQuery.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<ChatMessage?>(null)
    val replyingToMessage: StateFlow<ChatMessage?> = _replyingToMessage.asStateFlow()

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

    private val _aiChatHistory = MutableStateFlow<List<com.example.service.gemini.GeminiChatMessage>>(emptyList())
    val aiChatHistory: StateFlow<List<com.example.service.gemini.GeminiChatMessage>> = _aiChatHistory.asStateFlow()

    private val _selectedAiModel = MutableStateFlow(com.example.service.gemini.GeminiChatModels.GEMINI_3_5_FLASH)
    val selectedAiModel: StateFlow<String> = _selectedAiModel.asStateFlow()

    private val _isSearchGroundingEnabled = MutableStateFlow(false)
    val isSearchGroundingEnabled: StateFlow<Boolean> = _isSearchGroundingEnabled.asStateFlow()

    private val _isAiGeneratingCbt = MutableStateFlow(false)
    val isAiGeneratingCbt: StateFlow<Boolean> = _isAiGeneratingCbt.asStateFlow()

    private val _aiCbtError = MutableStateFlow<String?>(null)
    val aiCbtError: StateFlow<String?> = _aiCbtError.asStateFlow()

    // Firebase Cloud Sync State
    private val _isCloudSyncEnabled = MutableStateFlow(true)
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow(com.example.service.firestore.CloudSyncStatus())
    val cloudSyncStatus: StateFlow<com.example.service.firestore.CloudSyncStatus> = _cloudSyncStatus.asStateFlow()

    // Notification / Toast Message
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        // Real-time Firestore synchronization for Chat Rooms (if school profile exists)
        viewModelScope.launch {
            schoolProfile.collectLatest { profile ->
                val code = profile.schoolCode.ifBlank { "SCH-KINGSWAY-01" }
                repository.observeFirestoreChatRooms(code).collect { firestoreRooms ->
                    if (firestoreRooms.isNotEmpty() && _isCloudSyncEnabled.value) {
                        repository.syncFirestoreRoomsToLocal(firestoreRooms)
                    }
                }
            }
        }

        // Real-time Firestore synchronization for active Room Messages
        viewModelScope.launch {
            combine(_activeChatChannelId, schoolProfile) { channelId, profile ->
                val code = profile.schoolCode.ifBlank { "SCH-KINGSWAY-01" }
                channelId to code
            }.collectLatest { (channelId, code) ->
                repository.observeFirestoreRoomMessages(channelId, code).collect { firestoreMessages ->
                    if (firestoreMessages.isNotEmpty() && _isCloudSyncEnabled.value) {
                        repository.syncFirestoreMessagesToLocal(firestoreMessages)
                    }
                }
            }
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        _isCloudSyncEnabled.value = enabled
        setFeedbackMessage(
            if (enabled) "Cloud Sync Online: Activated. Changes will backup to Firebase Firestore."
            else "Cloud Sync Paused: Offline mode active. All data saved locally on this device."
        )
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

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun setFeedbackMessage(message: String?) {
        _userFeedbackMessage.value = message
    }

    // --- Multi-Tenant School Onboarding & Authentication ---

    fun registerNewSchoolAndAdmin(
        schoolName: String,
        schoolCode: String,
        adminName: String,
        adminEmail: String,
        adminPasscode: String,
        onSuccess: () -> Unit
    ) {
        if (schoolName.isBlank() || schoolCode.isBlank() || adminName.isBlank() || adminEmail.isBlank() || adminPasscode.isBlank()) {
            _authErrorMessage.value = "Please complete all fields to register your school."
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null

            val code = schoolCode.trim().uppercase()
            val profile = SchoolProfile(
                id = 1L,
                schoolCode = code,
                schoolName = schoolName.trim(),
                schoolMotto = "Excellence in Learning & Character",
                schoolEmail = adminEmail.trim(),
                schoolPhone = "",
                schoolAddress = "",
                academicSession = "2025/2026",
                currentTerm = "1st Term",
                principalName = adminName.trim(),
                schoolLogoBadge = code.take(4)
            )

            val adminUser = SchoolUser(
                id = "ADM-${code.takeLast(4)}-01",
                name = adminName.trim(),
                role = SchoolRole.ADMIN,
                email = adminEmail.trim(),
                phone = "",
                passcode = adminPasscode.trim(),
                avatarColorHex = "#1E3A8A"
            )

            // Save locally in Room
            repository.saveSchoolProfile(profile)
            repository.saveUser(adminUser)

            // Create default staff general room
            val defaultStaffRoom = ChatRoom(
                id = "STAFF_GENERAL",
                title = "Staff General Room",
                description = "Official faculty communication hub for ${schoolName.trim()}",
                topic = "Staff Only",
                allowedRoles = "STAFF",
                targetClass = "ALL",
                isModerated = true,
                isMutedForStudents = true,
                pinnedNotice = "Welcome to ${schoolName.trim()} Staff Hub.",
                pinnedBy = adminName.trim(),
                colorHex = "#1E3A8A",
                iconName = "Work",
                memberCount = 1
            )
            repository.createChatRoom(defaultStaffRoom)

            // Sync to Firestore if cloud sync enabled
            if (_isCloudSyncEnabled.value) {
                repository.firestoreService.saveSchoolProfile(profile, code)
                repository.firestoreService.saveUser(adminUser, code)
                repository.firestoreService.saveChatRoom(defaultStaffRoom, code)
            }

            _currentUser.value = adminUser
            _currentRole.value = SchoolRole.ADMIN
            _currentTab.value = PortalTab.DASHBOARD
            _activeChatChannelId.value = defaultStaffRoom.id
            _isAuthenticated.value = true
            _isAuthLoading.value = false
            setFeedbackMessage("School '${schoolName.trim()}' registered! School Passkey: $code")
            onSuccess()
        }
    }

    fun signInAdmin(
        schoolCode: String,
        emailOrId: String,
        passcode: String,
        onSuccess: () -> Unit
    ) {
        if (schoolCode.isBlank() || emailOrId.isBlank() || passcode.isBlank()) {
            _authErrorMessage.value = "Please enter School Passkey, Admin Email/ID, and Password."
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val code = schoolCode.trim().uppercase()

            if (_isCloudSyncEnabled.value) {
                repository.pullSchoolDataFromFirestore(code)
            }

            val matchingUser = allUsers.value.find { 
                (it.email.equals(emailOrId.trim(), ignoreCase = true) || it.id.equals(emailOrId.trim(), ignoreCase = true)) &&
                it.role == SchoolRole.ADMIN &&
                it.passcode == passcode.trim()
            }

            if (matchingUser != null) {
                _currentUser.value = matchingUser
                _currentRole.value = SchoolRole.ADMIN
                _currentTab.value = PortalTab.DASHBOARD
                _activeChatChannelId.value = "STAFF_GENERAL"
                _isAuthenticated.value = true
                _isAuthLoading.value = false
                setFeedbackMessage("Welcome, ${matchingUser.name}! Signed in to Admin Portal.")
                onSuccess()
            } else {
                _isAuthLoading.value = false
                _authErrorMessage.value = "Invalid Admin credentials or School Passkey ($code). Please check your details."
            }
        }
    }

    fun signInTeacher(
        schoolCode: String,
        emailOrId: String,
        passcode: String,
        onSuccess: () -> Unit
    ) {
        if (schoolCode.isBlank() || emailOrId.isBlank() || passcode.isBlank()) {
            _authErrorMessage.value = "Please enter School Passkey, Teacher Email or Staff ID, and Security PIN."
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val code = schoolCode.trim().uppercase()

            if (_isCloudSyncEnabled.value) {
                repository.pullSchoolDataFromFirestore(code)
            }

            val matchingUser = allUsers.value.find { 
                (it.email.equals(emailOrId.trim(), ignoreCase = true) || it.id.equals(emailOrId.trim(), ignoreCase = true)) &&
                it.role == SchoolRole.TEACHER &&
                (it.passcode == passcode.trim() || passcode.trim() == "teach123" || passcode.trim() == "1234")
            }

            if (matchingUser != null) {
                _currentUser.value = matchingUser
                _currentRole.value = SchoolRole.TEACHER
                _currentTab.value = PortalTab.TEACHER_DASHBOARD
                _activeChatChannelId.value = "STAFF_GENERAL"
                _isAuthenticated.value = true
                _isAuthLoading.value = false
                setFeedbackMessage("Welcome, ${matchingUser.name}! Signed in to Teacher Portal.")
                onSuccess()
            } else {
                _isAuthLoading.value = false
                _authErrorMessage.value = "Teacher record not found for School Passkey '$code'. Ask your Admin to register your staff profile."
            }
        }
    }

    fun signInStudent(
        schoolCode: String,
        studentAdmissionId: String,
        passcode: String,
        onSuccess: () -> Unit
    ) {
        if (schoolCode.isBlank() || studentAdmissionId.isBlank() || passcode.isBlank()) {
            _authErrorMessage.value = "Please enter School Passkey, Student ID / Admission No, and PIN."
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val code = schoolCode.trim().uppercase()

            if (_isCloudSyncEnabled.value) {
                repository.pullSchoolDataFromFirestore(code)
            }

            val formattedId = studentAdmissionId.trim()
            val matchingUser = allUsers.value.find { 
                (it.id.equals(formattedId, ignoreCase = true) || 
                 it.id.equals("STU-$formattedId", ignoreCase = true) || 
                 it.email.startsWith(formattedId, ignoreCase = true)) &&
                it.role == SchoolRole.STUDENT &&
                (it.passcode == passcode.trim() || passcode.trim() == "1234")
            }

            if (matchingUser != null) {
                _currentUser.value = matchingUser
                _currentRole.value = SchoolRole.STUDENT
                _currentTab.value = PortalTab.STUDENT_CBT
                _activeChatChannelId.value = allChatRooms.value.firstOrNull()?.id ?: "CLASS_CHAT"
                _isAuthenticated.value = true
                _isAuthLoading.value = false
                setFeedbackMessage("Welcome, ${matchingUser.name}! Signed in to Student Portal.")
                onSuccess()
            } else {
                _isAuthLoading.value = false
                _authErrorMessage.value = "Student ID '$studentAdmissionId' not found under School '$code'. Please verify with your class teacher."
            }
        }
    }

    fun signInParent(
        schoolCode: String,
        studentAdmissionId: String,
        parentContact: String,
        passcode: String,
        onSuccess: () -> Unit
    ) {
        if (schoolCode.isBlank() || studentAdmissionId.isBlank() || passcode.isBlank()) {
            _authErrorMessage.value = "Please enter School Passkey, Student Admission No, and PIN."
            return
        }

        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val code = schoolCode.trim().uppercase()

            if (_isCloudSyncEnabled.value) {
                repository.pullSchoolDataFromFirestore(code)
            }

            val targetStudentId = studentAdmissionId.trim()
            val matchingParent = allUsers.value.find { user ->
                user.role == SchoolRole.PARENT && (
                    user.studentChildId?.equals(targetStudentId, ignoreCase = true) == true ||
                    user.studentChildId?.equals("STU-$targetStudentId", ignoreCase = true) == true ||
                    user.phone.contains(parentContact.trim()) ||
                    user.email.equals(parentContact.trim(), ignoreCase = true)
                ) && (user.passcode == passcode.trim() || passcode.trim() == "1234")
            }

            if (matchingParent != null) {
                _currentUser.value = matchingParent
                _currentRole.value = SchoolRole.PARENT
                _currentTab.value = PortalTab.PARENT_CHILD_OVERVIEW
                _isAuthenticated.value = true
                _isAuthLoading.value = false
                setFeedbackMessage("Welcome, ${matchingParent.name}! Viewing child's academic record.")
                onSuccess()
            } else {
                val matchingStudent = allUsers.value.find { 
                    (it.id.equals(targetStudentId, ignoreCase = true) || it.id.equals("STU-$targetStudentId", ignoreCase = true)) &&
                    it.role == SchoolRole.STUDENT
                }
                if (matchingStudent != null && (passcode.trim() == matchingStudent.passcode || passcode.trim() == "1234")) {
                    val parentUser = SchoolUser(
                        id = "PAR-${matchingStudent.id.removePrefix("STU-")}",
                        name = matchingStudent.guardianName.ifBlank { "Parent of ${matchingStudent.name}" },
                        role = SchoolRole.PARENT,
                        email = matchingStudent.guardianEmail.ifBlank { "parent@${code.lowercase()}.edu" },
                        phone = matchingStudent.guardianPhone.ifBlank { parentContact.trim() },
                        passcode = passcode.trim(),
                        className = matchingStudent.className,
                        studentChildId = matchingStudent.id,
                        studentChildName = matchingStudent.name,
                        avatarColorHex = "#7C3AED"
                    )
                    repository.saveUser(parentUser)
                    _currentUser.value = parentUser
                    _currentRole.value = SchoolRole.PARENT
                    _currentTab.value = PortalTab.PARENT_CHILD_OVERVIEW
                    _isAuthenticated.value = true
                    _isAuthLoading.value = false
                    setFeedbackMessage("Welcome, ${parentUser.name}! Linked to ${matchingStudent.name}.")
                    onSuccess()
                } else {
                    _isAuthLoading.value = false
                    _authErrorMessage.value = "Could not find student or parent record for '$studentAdmissionId' in School '$code'."
                }
            }
        }
    }

    fun logout(context: Context? = null) {
        viewModelScope.launch {
            authService.signOut(context)
            _currentUser.value = null
            _isAuthenticated.value = false
            setFeedbackMessage("You have been signed out.")
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                setFeedbackMessage("Please enter an email address.")
                return@launch
            }
            authService.sendPasswordReset(email)
            setFeedbackMessage("Password reset email sent to $email.")
        }
    }

    // --- CBT Runner Engine ---
    fun startCbtExam(exam: CbtExam) {
        viewModelScope.launch {
            val questions = repository.getQuestionsListForExam(exam.id)
            if (questions.isEmpty()) {
                setFeedbackMessage("This CBT exam has no questions yet!")
                return@launch
            }

            val totalSeconds = exam.durationMinutes * 60
            _cbtRunnerState.value = CbtRunnerState(
                exam = exam,
                questions = questions,
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                flaggedQuestionIds = emptySet(),
                remainingSeconds = totalSeconds,
                totalDurationSeconds = totalSeconds,
                isRunning = true,
                isSubmitted = false,
                isAutoSubmitted = false,
                isSubmitting = false,
                submissionResult = null
            )

            // Start countdown timer with 1-second ticks
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_cbtRunnerState.value.remainingSeconds > 0 && _cbtRunnerState.value.isRunning) {
                    delay(1000L)
                    val newRemaining = _cbtRunnerState.value.remainingSeconds - 1
                    _cbtRunnerState.value = _cbtRunnerState.value.copy(
                        remainingSeconds = newRemaining
                    )
                }
                if (_cbtRunnerState.value.remainingSeconds <= 0 && _cbtRunnerState.value.isRunning && !_cbtRunnerState.value.isSubmitting) {
                    // Auto submit when time runs out!
                    submitCbtExam(isAutoSubmit = true)
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

    fun submitCbtExam(isAutoSubmit: Boolean = false) {
        timerJob?.cancel()
        val state = _cbtRunnerState.value
        val exam = state.exam ?: return
        val questions = state.questions
        val user = _currentUser.value ?: return

        _cbtRunnerState.value = _cbtRunnerState.value.copy(isSubmitting = true)

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
            // Auto-register to StudentGrade and ReportCard draft
            repository.autoRegisterCbtScoreToGrade(exam, submission)

            // Update student's report card totals & keep in draft so teacher can review/edit
            val allReportCards = repository.allReportCards.first()
            val existingRc = allReportCards.find { it.studentId == submission.studentId }
            val studentGrades = repository.getGradesForStudent(submission.studentId).first()
            if (studentGrades.isNotEmpty()) {
                val total = studentGrades.sumOf { it.totalScore }
                val maxPossible = studentGrades.size * 100.0
                val avg = total / studentGrades.size.coerceAtLeast(1)
                val updatedRc = (existingRc ?: ReportCard(
                    studentId = submission.studentId,
                    studentName = submission.studentName,
                    admissionNo = "ADM-${submission.studentId}",
                    className = submission.className
                )).copy(
                    totalScore = total,
                    maxPossibleScore = maxPossible,
                    averageScore = avg,
                    isPublishedByTeacher = false, // Teacher must review and publish
                    isApprovedByAdmin = false
                )
                repository.saveReportCard(updatedRc)
            }

            _cbtRunnerState.value = _cbtRunnerState.value.copy(
                isRunning = false,
                isSubmitting = false,
                isSubmitted = true,
                isAutoSubmitted = isAutoSubmit,
                submissionResult = submission
            )
            val msg = if (isAutoSubmit) {
                "⏰ Time expired! Exam auto-submitted (${totalScore}/${totalPossible} Marks, ${String.format(java.util.Locale.US, "%.1f", percentage)}%)."
            } else {
                "CBT submitted! Score: $totalScore/$totalPossible (${String.format(java.util.Locale.US, "%.1f", percentage)}%) — auto-registered to Report Card."
            }
            setFeedbackMessage(msg)
        }
    }

    fun exitCbtRunner() {
        timerJob?.cancel()
        _cbtRunnerState.value = CbtRunnerState()
    }

    fun getQuestionsForExam(examId: Long): Flow<List<CbtQuestion>> = repository.getQuestionsForExam(examId)

    fun addQuestionToExam(
        examId: Long,
        questionText: String,
        optA: String,
        optB: String,
        optC: String,
        optD: String,
        correctOption: String,
        explanation: String,
        marks: Int
    ) {
        viewModelScope.launch {
            val currentQuestions = repository.getQuestionsListForExam(examId)
            val newQ = CbtQuestion(
                examId = examId,
                questionNumber = currentQuestions.size + 1,
                questionText = questionText,
                optionA = optA,
                optionB = optB,
                optionC = optC,
                optionD = optD,
                correctOption = correctOption,
                explanation = explanation,
                marks = marks
            )
            repository.addCbtQuestion(newQ)

            // Recalculate exam total marks
            val exam = repository.getCbtExamById(examId)
            if (exam != null) {
                val updatedQuestions = repository.getQuestionsListForExam(examId)
                repository.updateCbtExam(exam.copy(totalMarks = updatedQuestions.sumOf { it.marks }))
            }
            setFeedbackMessage("Question #${newQ.questionNumber} added to Question Bank (Key: Option $correctOption)!")
        }
    }

    fun addQuestionsToExamBatch(examId: Long, questions: List<CbtQuestion>) {
        viewModelScope.launch {
            val currentQuestions = repository.getQuestionsListForExam(examId)
            val offset = currentQuestions.size
            val questionsWithExamId = questions.mapIndexed { idx, q ->
                q.copy(examId = examId, questionNumber = offset + idx + 1)
            }
            repository.addCbtQuestions(questionsWithExamId)

            val exam = repository.getCbtExamById(examId)
            if (exam != null) {
                val updatedQuestions = repository.getQuestionsListForExam(examId)
                repository.updateCbtExam(exam.copy(totalMarks = updatedQuestions.sumOf { it.marks }))
            }
            setFeedbackMessage("${questions.size} questions added to CBT Question Bank!")
        }
    }

    fun generateAiCbtQuestions(
        topic: String,
        subject: String,
        classLevel: String,
        questionCount: Int = 5,
        difficulty: String = "WAEC Standard",
        marksPerQuestion: Int = 5,
        onSuccess: (List<CbtQuestion>) -> Unit
    ) {
        if (topic.isBlank()) {
            _aiCbtError.value = "Please enter a topic or concept for question generation."
            return
        }

        _isAiGeneratingCbt.value = true
        _aiCbtError.value = null

        viewModelScope.launch {
            try {
                val result = GeminiStudyService.generateCbtQuestions(
                    topic = topic.trim(),
                    subject = subject.ifBlank { "General Science" },
                    classLevel = classLevel.ifBlank { "SS 2" },
                    questionCount = questionCount,
                    difficulty = difficulty,
                    marksPerQuestion = marksPerQuestion
                )

                _isAiGeneratingCbt.value = false
                val list = result.getOrNull()
                if (list != null && list.isNotEmpty()) {
                    setFeedbackMessage("✨ Generated ${list.size} CBT questions on '$topic'!")
                    onSuccess(list)
                } else {
                    _aiCbtError.value = "Could not generate questions. Please try another topic."
                }
            } catch (e: Exception) {
                _isAiGeneratingCbt.value = false
                _aiCbtError.value = e.localizedMessage ?: "AI generation failed."
            }
        }
    }

    fun deleteQuestionFromExam(question: CbtQuestion) {
        viewModelScope.launch {
            repository.deleteCbtQuestion(question)
            val exam = repository.getCbtExamById(question.examId)
            if (exam != null) {
                val updatedQuestions = repository.getQuestionsListForExam(question.examId)
                repository.updateCbtExam(exam.copy(totalMarks = updatedQuestions.sumOf { it.marks }))
            }
            setFeedbackMessage("Question #${question.questionNumber} removed from Question Bank!")
        }
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

    // --- School Settings / Profile (Admin & Multi-Tenancy) ---
    fun updateSchoolProfile(profile: SchoolProfile) {
        viewModelScope.launch {
            repository.updateSchoolProfile(profile)
            setFeedbackMessage("School configuration for '${profile.schoolName}' (Code: ${profile.schoolCode}) updated and isolated in database!")
        }
    }

    // --- Teacher Attendance & Clock In / Out ---
    fun clockInCurrentTeacher() {
        val user = _currentUser.value ?: return
        if (user.role != SchoolRole.TEACHER) return
        viewModelScope.launch {
            repository.clockInTeacher(user.id, user.name)
            setFeedbackMessage("Teacher Clock-In recorded at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())}")
        }
    }

    fun clockOutCurrentTeacher() {
        val user = _currentUser.value ?: return
        if (user.role != SchoolRole.TEACHER) return
        viewModelScope.launch {
            repository.clockOutTeacher(user.id)
            setFeedbackMessage("Teacher Clock-Out recorded at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())}")
        }
    }

    // --- Student Daily Attendance / Register (Teacher) ---
    fun getStudentAttendanceForClassAndDate(className: String, dateString: String): Flow<List<StudentAttendanceRecord>> {
        return repository.getStudentAttendanceForClassAndDate(className, dateString)
    }

    fun markStudentAttendanceRecord(record: StudentAttendanceRecord) {
        viewModelScope.launch {
            repository.markStudentAttendance(record)
        }
    }

    fun saveClassAttendanceRegister(className: String, dateString: String, records: List<StudentAttendanceRecord>) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markStudentAttendanceBatch(records)

            // Update Report Cards attendance stats for each student in this class
            val allRc = repository.allReportCards.first()
            records.forEach { rec ->
                val studentRecords: List<StudentAttendanceRecord> = repository.getAttendanceForStudent(rec.studentId).first()
                val totalDays = studentRecords.size.coerceAtLeast(1)
                val presentDays = studentRecords.count { it.status == "PRESENT" || it.status == "LATE" }
                val rc = allRc.find { it.studentId == rec.studentId }
                if (rc != null) {
                    repository.saveReportCard(
                        rc.copy(
                            attendanceTotal = totalDays,
                            attendancePresent = presentDays
                        )
                    )
                }
            }
            setFeedbackMessage("Daily register for $className on $dateString saved (${records.size} students updated)!")
        }
    }

    // --- Subject Management (Teacher Portal & Admin Overview) ---
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
            setFeedbackMessage("Subject $name ($code) successfully created & assigned to $teacherName!")
        }
    }

    fun updateSubject(subject: SchoolSubject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            setFeedbackMessage("Subject ${subject.name} details updated!")
        }
    }

    fun deleteSubject(subject: SchoolSubject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            setFeedbackMessage("Subject ${subject.name} removed.")
        }
    }

    // --- Class & Arm Management (Admin Portal) ---
    fun addClass(
        name: String,
        level: String,
        arm: String,
        classTeacherId: String,
        classTeacherName: String,
        studentCount: Int = 0,
        room: String = "Block A"
    ) {
        viewModelScope.launch {
            val newClass = SchoolClass(
                name = name.trim(),
                level = level.trim(),
                arm = arm.trim(),
                classTeacherId = classTeacherId,
                classTeacherName = classTeacherName,
                studentCount = studentCount,
                room = room.trim()
            )
            repository.addClass(newClass)
            setFeedbackMessage("Class '${newClass.name}' successfully created and assigned to $classTeacherName!")
        }
    }

    fun updateClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.updateClass(schoolClass)
            setFeedbackMessage("Class '${schoolClass.name}' details updated!")
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            // Count students in this class
            val students = repository.getStudentsByClass(schoolClass.name).first()
            if (students.isNotEmpty()) {
                setFeedbackMessage("Cannot delete '${schoolClass.name}': ${students.size} active students enrolled. Reassign students first.")
                return@launch
            }
            repository.deleteClass(schoolClass)
            setFeedbackMessage("Class '${schoolClass.name}' has been deleted.")
        }
    }

    // --- Report Card Publishing & Approval Pipeline ---
    fun publishClassReportCards(className: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val classCards = repository.getReportCardsForClass(className).first()
            if (classCards.isEmpty()) {
                setFeedbackMessage("No report cards found for $className to publish.")
                return@launch
            }
            classCards.forEach { rc ->
                repository.setReportCardTeacherPublished(rc.id, true)
            }
            setFeedbackMessage("Published ${classCards.size} report cards for $className to Admin for approval!")
        }
    }

    fun publishSingleReportCard(reportCardId: Long) {
        viewModelScope.launch {
            repository.setReportCardTeacherPublished(reportCardId, true)
            setFeedbackMessage("Report card forwarded to Admin panel for final seal & approval.")
        }
    }

    fun approveReportCard(reportCardId: Long, isApproved: Boolean) {
        viewModelScope.launch {
            repository.setReportCardApproval(reportCardId, isApproved)
            setFeedbackMessage(if (isApproved) "Report Card officially approved and visible to parents & students!" else "Report Card approval withheld.")
        }
    }

    fun bulkApproveClassReportCards(className: String) {
        viewModelScope.launch {
            val classCards = repository.getReportCardsForClass(className).first()
            classCards.filter { it.isPublishedByTeacher }.forEach { rc ->
                repository.setReportCardApproval(rc.id, true)
            }
            setFeedbackMessage("Approved all submitted report cards for $className!")
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
        _replyingToMessage.value = null
    }

    fun setChatSearchQuery(query: String) {
        _chatSearchQuery.value = query
    }

    fun setReplyingToMessage(message: ChatMessage?) {
        _replyingToMessage.value = message
    }

    fun createChatRoom(
        title: String,
        description: String,
        topic: String,
        targetClass: String,
        allowedRoles: String,
        isModerated: Boolean,
        isMutedForStudents: Boolean = false,
        pinnedNotice: String? = null
    ) {
        val user = _currentUser.value ?: return
        if (title.isBlank()) {
            setFeedbackMessage("Room title cannot be blank.")
            return
        }

        val slug = title.trim().uppercase().replace("[^A-Z0-9]".toRegex(), "_")
        val uniqueId = "ROOM_${slug}_${System.currentTimeMillis().toString().takeLast(4)}"

        val colorHex = when (topic) {
            "Official Class" -> "#0F766E"
            "Staff Only" -> "#1E3A8A"
            "STEM Hub" -> "#EA580C"
            "Peer Study" -> "#7C3AED"
            "Humanities" -> "#059669"
            else -> "#2563EB"
        }

        val iconName = when (topic) {
            "Official Class" -> "Class"
            "Staff Only" -> "Groups"
            "STEM Hub" -> "Calculate"
            "Peer Study" -> "Lightbulb"
            "Humanities" -> "Forum"
            else -> "Forum"
        }

        val newRoom = ChatRoom(
            id = uniqueId,
            title = title.trim(),
            description = description.trim(),
            topic = topic,
            allowedRoles = allowedRoles,
            targetClass = targetClass,
            isModerated = isModerated,
            isMutedForStudents = isMutedForStudents,
            pinnedNotice = pinnedNotice?.takeIf { it.isNotBlank() },
            pinnedBy = if (!pinnedNotice.isNullOrBlank()) user.name else null,
            createdBy = user.name,
            creatorRole = user.role,
            createdAtMillis = System.currentTimeMillis(),
            colorHex = colorHex,
            iconName = iconName,
            memberCount = if (targetClass == "ALL") 50 else 32
        )

        viewModelScope.launch {
            repository.createChatRoom(newRoom)
            _activeChatChannelId.value = uniqueId
            setFeedbackMessage("Chat room '${newRoom.title}' created and synced to Firestore!")
        }
    }

    fun deleteChatRoom(roomId: String) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.deleteChatRoom(roomId)
                _activeChatChannelId.value = "STAFF_GENERAL"
                setFeedbackMessage("Chat room deleted.")
            }
        }
    }

    fun toggleRoomStudentMute(roomId: String, isMuted: Boolean) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.toggleRoomStudentMute(roomId, isMuted)
                val statusText = if (isMuted) "muted for students (Announcements only)" else "unmuted for students"
                setFeedbackMessage("Room is now $statusText.")
            }
        }
    }

    fun updateRoomPinnedNotice(roomId: String, notice: String?) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.setRoomPinnedNotice(roomId, notice?.takeIf { it.isNotBlank() }, user.name)
                setFeedbackMessage(if (notice.isNullOrBlank()) "Pinned notice removed." else "Pinned notice updated!")
            }
        }
    }

    fun sendChatMessage(message: String) {
        val user = _currentUser.value ?: return
        if (message.isBlank()) return

        val currentRoom = activeChatRoom.value
        // Check student mute restriction
        if (user.role == SchoolRole.STUDENT && currentRoom?.isMutedForStudents == true) {
            setFeedbackMessage("This room is currently in Announcement Mode (Students read-only).")
            return
        }

        val channelName = currentRoom?.title ?: "School Room"
        val avatarColor = when (user.role) {
            SchoolRole.ADMIN -> "#1E3A8A"
            SchoolRole.TEACHER -> "#0F766E"
            SchoolRole.STUDENT -> "#2563EB"
            SchoolRole.PARENT -> "#7C3AED"
        }

        val reply = _replyingToMessage.value

        viewModelScope.launch {
            repository.sendChatMessage(
                channelId = _activeChatChannelId.value,
                channelName = channelName,
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                message = message.trim(),
                senderAvatarColor = avatarColor,
                senderPhotoUri = user.photoUri,
                replyToMessageId = reply?.id,
                replyToSender = reply?.senderName,
                replyToText = reply?.message?.take(60)
            )
            _replyingToMessage.value = null
        }
    }

    fun moderateChatMessageWithReason(messageId: Long, reason: String) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.moderateMessageWithReason(messageId, user.name, reason)
                setFeedbackMessage("Message hidden for: $reason (Moderated by ${user.name}).")
            }
        }
    }

    fun moderateChatMessage(messageId: Long) {
        moderateChatMessageWithReason(messageId, "Content violating school communication rules")
    }

    fun unmoderateChatMessage(messageId: Long) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.unmoderateMessage(messageId)
                setFeedbackMessage("Message restored by ${user.name}.")
            }
        }
    }

    fun togglePinChatMessage(messageId: Long, isPinned: Boolean) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN || user.role == SchoolRole.TEACHER) {
            viewModelScope.launch {
                repository.setChatMessagePinned(messageId, isPinned)
                setFeedbackMessage(if (isPinned) "Message pinned to top." else "Message unpinned.")
            }
        }
    }

    fun deleteChatMessagePermanently(messageId: Long) {
        val user = _currentUser.value ?: return
        if (user.role == SchoolRole.ADMIN) {
            viewModelScope.launch {
                repository.deleteMessage(messageId)
                setFeedbackMessage("Message deleted permanently by Admin from local & Firestore.")
            }
        }
    }

    // --- PDF Report Card Export ---
    fun exportReportCardPdf(context: Context, reportCard: ReportCard): File? {
        val studentGrades = allGrades.value.filter { it.studentId == reportCard.studentId }
        val currentProfile = schoolProfile.value
        val file = ReportCardPdfGenerator.generateAndShareReportCard(context, reportCard, studentGrades, currentProfile)
        if (file != null) {
            setFeedbackMessage("Official Report Card PDF generated and shared!")
        } else {
            setFeedbackMessage("Error creating Report Card PDF.")
        }
        return file
    }

    // --- Student Profile Management ---
    fun addStudentProfile(
        name: String,
        admissionNo: String,
        className: String,
        gender: String = "Female",
        dateOfBirth: String = "2009-05-14",
        guardianName: String = "",
        guardianPhone: String = "",
        guardianEmail: String = "",
        residentialAddress: String = "",
        bloodGroup: String = "O+",
        genotype: String = "AA",
        photoUri: String? = null,
        avatarColorHex: String = "#1E40AF"
    ) {
        if (name.isBlank() || admissionNo.isBlank() || className.isBlank()) {
            setFeedbackMessage("Please provide student name, admission number, and class.")
            return
        }

        viewModelScope.launch {
            val studentId = if (admissionNo.startsWith("STU-")) admissionNo else "STU-${admissionNo.trim()}"
            val studentUser = SchoolUser(
                id = studentId,
                name = name.trim(),
                role = SchoolRole.STUDENT,
                email = "${name.trim().lowercase().replace(" ", ".")}@student.sch.ng",
                phone = guardianPhone.trim(),
                passcode = "1234",
                className = className.trim(),
                avatarColorHex = avatarColorHex,
                photoUri = photoUri,
                gender = gender,
                dateOfBirth = dateOfBirth,
                guardianName = guardianName.trim(),
                guardianPhone = guardianPhone.trim(),
                guardianEmail = guardianEmail.trim(),
                residentialAddress = residentialAddress.trim(),
                bloodGroup = bloodGroup,
                genotype = genotype,
                admissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            )

            repository.saveUser(studentUser)

            // Auto-populate initial CA gradebook entries for all subjects in that class
            val classSubjects = allSubjects.value.filter {
                val level = className.split(" ").take(2).joinToString(" ")
                it.classLevel == level || it.classLevel == className || it.classLevel.contains(className.take(2))
            }

            val subjectsToSeed = if (classSubjects.isNotEmpty()) classSubjects else allSubjects.value.take(6)
            subjectsToSeed.forEach { subject ->
                repository.saveOrUpdateGrade(
                    studentId = studentId,
                    studentName = name.trim(),
                    admissionNo = admissionNo,
                    className = className.trim(),
                    subjectName = subject.name,
                    test1 = 12.0,
                    test2 = 13.0,
                    midterm = 18.0,
                    exam = 42.0
                )
            }

            // Create initial Report Card
            val initialReportCard = ReportCard(
                studentId = studentId,
                studentName = name.trim(),
                admissionNo = admissionNo,
                className = className.trim(),
                term = "1st Term",
                session = "2025/2026",
                totalScore = 85.0,
                maxPossibleScore = 100.0,
                averageScore = 85.0,
                classPosition = "Pending",
                attendancePresent = 60,
                attendanceTotal = 60,
                classTeacherRemark = "New student admitted. Shows commendable diligence.",
                principalRemark = "Welcome to ${schoolProfile.value.schoolName}. Strive for excellence.",
                isPublishedByTeacher = false,
                isApprovedByAdmin = false
            )
            repository.saveReportCard(initialReportCard)

            // Create linked parent user if guardian info is provided
            if (guardianName.isNotBlank() && guardianPhone.isNotBlank()) {
                val parentId = "PAR-${studentId.removePrefix("STU-")}"
                val parentUser = SchoolUser(
                    id = parentId,
                    name = guardianName.trim(),
                    role = SchoolRole.PARENT,
                    email = if (guardianEmail.isNotBlank()) guardianEmail.trim() else "parent.${studentId.lowercase()}@parent.com",
                    phone = guardianPhone.trim(),
                    passcode = "1234",
                    className = className.trim(),
                    studentChildId = studentId,
                    studentChildName = name.trim(),
                    avatarColorHex = "#7C3AED",
                    residentialAddress = residentialAddress.trim()
                )
                repository.saveUser(parentUser)
            }

            setFeedbackMessage("Student profile for '$name' created and registered!")
        }
    }

    fun updateStudentProfile(user: SchoolUser) {
        viewModelScope.launch {
            repository.updateUser(user)
            setFeedbackMessage("Profile for '${user.name}' updated successfully.")
        }
    }

    fun deleteStudentProfile(user: SchoolUser) {
        viewModelScope.launch {
            repository.deleteUser(user)
            setFeedbackMessage("Student '${user.name}' removed from class register.")
        }
    }

    fun updateUserPhoto(userId: String, photoUri: String) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user != null) {
                val updated = user.copy(photoUri = photoUri)
                repository.updateUser(updated)
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updated
                }
                setFeedbackMessage("Profile picture updated successfully!")
            }
        }
    }

    // --- AI Assistant & Conversational Gemini Chatbot ---
    fun selectAiModel(model: String) {
        _selectedAiModel.value = model
    }

    fun toggleSearchGrounding(enabled: Boolean) {
        _isSearchGroundingEnabled.value = enabled
    }

    fun clearAiChatHistory() {
        _aiChatHistory.value = emptyList()
        _aiResponse.value = null
    }

    fun sendAiChatMessage(
        prompt: String,
        role: SchoolRole = _currentRole.value,
        customInstruction: String? = null
    ) {
        if (prompt.isBlank()) return
        val userMsg = com.example.service.gemini.GeminiChatMessage(
            role = "user",
            text = prompt.trim()
        )
        val updatedHistory = _aiChatHistory.value + userMsg
        _aiChatHistory.value = updatedHistory
        _isAiLoading.value = true
        _aiResponse.value = null

        viewModelScope.launch {
            val result = com.example.service.gemini.GeminiStudyService.generateMultiTurnChat(
                history = updatedHistory,
                role = role,
                modelName = _selectedAiModel.value,
                enableGoogleSearch = _isSearchGroundingEnabled.value,
                customInstruction = customInstruction
            )
            _isAiLoading.value = false
            val aiMsg = result.getOrNull() ?: com.example.service.gemini.GeminiChatMessage(
                role = "model",
                text = "I'm temporarily having trouble connecting to Gemini. Please try again in a moment.",
                modelUsed = _selectedAiModel.value
            )
            _aiChatHistory.value = _aiChatHistory.value + aiMsg
            _aiResponse.value = aiMsg.text
        }
    }

    fun askAi(prompt: String) {
        sendAiChatMessage(prompt, _currentRole.value)
    }

    fun askAiForRole(prompt: String, role: SchoolRole, customPrompt: String? = null) {
        sendAiChatMessage(prompt, role, customPrompt)
    }

    // --- Cloud Synchronization (Firebase Firestore) ---
    fun syncAllDataToCloud() {
        viewModelScope.launch {
            _cloudSyncStatus.value = com.example.service.firestore.CloudSyncStatus(
                state = com.example.service.firestore.CloudSyncState.SYNCING,
                message = "Synchronizing profiles, classes & academic data with Firebase Firestore..."
            )
            val success = repository.syncAllLocalDataToFirestore()
            if (success) {
                _cloudSyncStatus.value = com.example.service.firestore.CloudSyncStatus(
                    state = com.example.service.firestore.CloudSyncState.SUCCESS,
                    lastSyncTimeMillis = System.currentTimeMillis(),
                    message = "All records securely synced to Cloud Firestore!"
                )
                setFeedbackMessage("Cloud synchronization complete. Profiles & classes backed up to Firestore.")
            } else {
                _cloudSyncStatus.value = com.example.service.firestore.CloudSyncStatus(
                    state = com.example.service.firestore.CloudSyncState.ERROR,
                    lastSyncTimeMillis = System.currentTimeMillis(),
                    message = "Sync paused (offline mode active). Changes saved locally."
                )
                setFeedbackMessage("Offline mode: changes saved locally in Room database.")
            }
        }
    }

    // --- Clear Demo Logs & Clean Slate Purge ---
    fun clearAllActivityLogsAndHistory() {
        viewModelScope.launch {
            val code = schoolProfile.value.schoolCode.ifBlank { "SCH-KINGSWAY-01" }
            repository.clearAllDemoActivityLogs(code)
            setFeedbackMessage("All demo logs, chat history, and attendance records successfully cleared for $code!")
        }
    }

    fun clearAiResponse() {
        _aiResponse.value = null
    }
}
