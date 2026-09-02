package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Users ---
    @Query("SELECT * FROM school_users")
    fun getAllUsers(): Flow<List<SchoolUser>>

    @Query("SELECT * FROM school_users WHERE id = :id")
    suspend fun getUserById(id: String): SchoolUser?

    @Query("SELECT * FROM school_users WHERE role = :role")
    fun getUsersByRole(role: SchoolRole): Flow<List<SchoolUser>>

    @Query("SELECT * FROM school_users WHERE role = 'STUDENT' AND className = :className")
    fun getStudentsByClass(className: String): Flow<List<SchoolUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: SchoolUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<SchoolUser>)

    @Update
    suspend fun updateUser(user: SchoolUser)

    @Delete
    suspend fun deleteUser(user: SchoolUser)

    @Query("DELETE FROM school_users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    // --- Classes ---
    @Query("SELECT * FROM school_classes ORDER BY level ASC, arm ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    @Query("DELETE FROM school_classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    // --- Subjects ---
    @Query("SELECT * FROM school_subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SchoolSubject>>

    @Query("SELECT * FROM school_subjects WHERE classLevel = :classLevel")
    fun getSubjectsByLevel(classLevel: String): Flow<List<SchoolSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SchoolSubject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SchoolSubject>)

    @Update
    suspend fun updateSubject(subject: SchoolSubject)

    @Delete
    suspend fun deleteSubject(subject: SchoolSubject)

    // --- CBT Exams & Questions ---
    @Query("SELECT * FROM cbt_exams ORDER BY createdDateMillis DESC")
    fun getAllCbtExams(): Flow<List<CbtExam>>

    @Query("SELECT * FROM cbt_exams WHERE className = :className OR className = 'All' ORDER BY createdDateMillis DESC")
    fun getCbtExamsForClass(className: String): Flow<List<CbtExam>>

    @Query("SELECT * FROM cbt_exams WHERE teacherId = :teacherId ORDER BY createdDateMillis DESC")
    fun getCbtExamsByTeacher(teacherId: String): Flow<List<CbtExam>>

    @Query("SELECT * FROM cbt_exams WHERE id = :examId")
    suspend fun getCbtExamById(examId: Long): CbtExam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCbtExam(exam: CbtExam): Long

    @Update
    suspend fun updateCbtExam(exam: CbtExam)

    @Delete
    suspend fun deleteCbtExam(exam: CbtExam)

    @Query("SELECT * FROM cbt_questions WHERE examId = :examId ORDER BY questionNumber ASC")
    fun getQuestionsForExam(examId: Long): Flow<List<CbtQuestion>>

    @Query("SELECT * FROM cbt_questions WHERE examId = :examId ORDER BY questionNumber ASC")
    suspend fun getQuestionsListForExam(examId: Long): List<CbtQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCbtQuestion(question: CbtQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCbtQuestions(questions: List<CbtQuestion>)

    @Delete
    suspend fun deleteCbtQuestion(question: CbtQuestion)

    // --- CBT Submissions ---
    @Query("SELECT * FROM cbt_submissions ORDER BY submittedAtMillis DESC")
    fun getAllCbtSubmissions(): Flow<List<CbtSubmission>>

    @Query("SELECT * FROM cbt_submissions WHERE examId = :examId ORDER BY submittedAtMillis DESC")
    fun getSubmissionsForExam(examId: Long): Flow<List<CbtSubmission>>

    @Query("SELECT * FROM cbt_submissions WHERE studentId = :studentId ORDER BY submittedAtMillis DESC")
    fun getSubmissionsByStudent(studentId: String): Flow<List<CbtSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCbtSubmission(submission: CbtSubmission): Long

    // --- CBT Active Session Offline Cache ---
    @Query("SELECT * FROM cbt_active_sessions WHERE sessionKey = :sessionKey LIMIT 1")
    suspend fun getActiveCbtSession(sessionKey: String): CbtActiveSessionCache?

    @Query("SELECT * FROM cbt_active_sessions WHERE studentId = :studentId")
    fun getAllActiveCbtSessionsForStudent(studentId: String): Flow<List<CbtActiveSessionCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveCbtSession(session: CbtActiveSessionCache)

    @Query("DELETE FROM cbt_active_sessions WHERE sessionKey = :sessionKey")
    suspend fun deleteActiveCbtSession(sessionKey: String)

    @Query("DELETE FROM cbt_active_sessions WHERE examId = :examId AND studentId = :studentId")
    suspend fun clearActiveCbtSession(examId: Long, studentId: String)

    // --- Assignments & Submissions ---
    @Query("SELECT * FROM school_assignments ORDER BY dueDateMillis ASC")
    fun getAllAssignments(): Flow<List<SchoolAssignment>>

    @Query("SELECT * FROM school_assignments WHERE className = :className ORDER BY dueDateMillis ASC")
    fun getAssignmentsForClass(className: String): Flow<List<SchoolAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: SchoolAssignment): Long

    @Delete
    suspend fun deleteAssignment(assignment: SchoolAssignment)

    @Query("SELECT * FROM assignment_submissions ORDER BY submittedAtMillis DESC")
    fun getAllAssignmentSubmissions(): Flow<List<AssignmentSubmission>>

    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId")
    fun getSubmissionsForAssignment(assignmentId: Long): Flow<List<AssignmentSubmission>>

    @Query("SELECT * FROM assignment_submissions WHERE studentId = :studentId")
    fun getSubmissionsByStudentAssignment(studentId: String): Flow<List<AssignmentSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignmentSubmission(submission: AssignmentSubmission): Long

    @Update
    suspend fun updateAssignmentSubmission(submission: AssignmentSubmission)

    // --- Student Grades ---
    @Query("SELECT * FROM student_grades WHERE studentId = :studentId")
    fun getGradesForStudent(studentId: String): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE className = :className")
    fun getGradesForClass(className: String): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE subjectName = :subjectName AND className = :className")
    fun getGradesForSubjectAndClass(subjectName: String, className: String): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades")
    fun getAllGrades(): Flow<List<StudentGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: StudentGrade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<StudentGrade>)

    @Update
    suspend fun updateGrade(grade: StudentGrade)

    // --- Report Cards ---
    @Query("SELECT * FROM report_cards WHERE studentId = :studentId LIMIT 1")
    fun getReportCardForStudent(studentId: String): Flow<ReportCard?>

    @Query("SELECT * FROM report_cards WHERE className = :className")
    fun getReportCardsForClass(className: String): Flow<List<ReportCard>>

    @Query("SELECT * FROM report_cards ORDER BY className ASC, averageScore DESC")
    fun getAllReportCards(): Flow<List<ReportCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportCard(reportCard: ReportCard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportCards(reportCards: List<ReportCard>)

    @Update
    suspend fun updateReportCard(reportCard: ReportCard)

    @Query("UPDATE report_cards SET isApprovedByAdmin = :isApproved, approvedAtMillis = :timestamp WHERE id = :reportCardId")
    suspend fun setReportCardApproval(reportCardId: Long, isApproved: Boolean, timestamp: Long)

    @Query("UPDATE report_cards SET isPublishedByTeacher = :isPublished, teacherPublishedAtMillis = :timestamp WHERE id = :reportCardId")
    suspend fun setTeacherPublished(reportCardId: Long, isPublished: Boolean, timestamp: Long)

    @Query("SELECT * FROM report_cards WHERE studentId = :studentId AND isApprovedByAdmin = 1 LIMIT 1")
    fun getApprovedReportCardForStudent(studentId: String): Flow<ReportCard?>

    @Query("SELECT * FROM student_grades WHERE studentId = :studentId AND subjectName = :subjectName LIMIT 1")
    suspend fun getGradeDirect(studentId: String, subjectName: String): StudentGrade?

    // --- School Profile ---
    @Query("SELECT * FROM school_profile WHERE id = 1 LIMIT 1")
    fun getSchoolProfile(): Flow<SchoolProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolProfile(profile: SchoolProfile)

    @Update
    suspend fun updateSchoolProfile(profile: SchoolProfile)

    // --- Teacher Attendance & Clock In/Out ---
    @Query("SELECT * FROM teacher_attendance ORDER BY clockInTimeMillis DESC")
    fun getAllTeacherAttendance(): Flow<List<TeacherAttendance>>

    @Query("SELECT * FROM teacher_attendance WHERE dateString = :dateString ORDER BY clockInTimeMillis DESC")
    fun getAttendanceForDate(dateString: String): Flow<List<TeacherAttendance>>

    @Query("SELECT * FROM teacher_attendance WHERE teacherId = :teacherId AND dateString = :dateString LIMIT 1")
    fun getTodayAttendanceForTeacher(teacherId: String, dateString: String): Flow<TeacherAttendance?>

    @Query("SELECT * FROM teacher_attendance WHERE teacherId = :teacherId AND dateString = :dateString LIMIT 1")
    suspend fun getTodayAttendanceForTeacherDirect(teacherId: String, dateString: String): TeacherAttendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAttendance(attendance: TeacherAttendance): Long

    @Update
    suspend fun updateTeacherAttendance(attendance: TeacherAttendance)

    // --- Student Daily Attendance / Register ---
    @Query("SELECT * FROM student_attendance WHERE className = :className AND dateString = :dateString")
    fun getStudentAttendanceForClassAndDate(className: String, dateString: String): Flow<List<StudentAttendanceRecord>>

    @Query("SELECT * FROM student_attendance WHERE studentId = :studentId")
    fun getAttendanceForStudent(studentId: String): Flow<List<StudentAttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAttendance(record: StudentAttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAttendanceList(records: List<StudentAttendanceRecord>)

    // --- Announcements ---
    @Query("SELECT * FROM school_announcements ORDER BY postedAtMillis DESC")
    fun getAllAnnouncements(): Flow<List<SchoolAnnouncement>>

    @Query("SELECT * FROM school_announcements WHERE targetAudience = :audience OR targetAudience = 'ALL' ORDER BY postedAtMillis DESC")
    fun getAnnouncementsForAudience(audience: String): Flow<List<SchoolAnnouncement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: SchoolAnnouncement): Long

    @Delete
    suspend fun deleteAnnouncement(announcement: SchoolAnnouncement)

    // --- Chat Rooms ---
    @Query("SELECT * FROM chat_rooms ORDER BY createdAtMillis ASC")
    fun getAllChatRooms(): Flow<List<ChatRoom>>

    @Query("SELECT * FROM chat_rooms WHERE id = :roomId LIMIT 1")
    fun getChatRoomById(roomId: String): Flow<ChatRoom?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(room: ChatRoom): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(rooms: List<ChatRoom>)

    @Update
    suspend fun updateChatRoom(room: ChatRoom)

    @Delete
    suspend fun deleteChatRoom(room: ChatRoom)

    @Query("DELETE FROM chat_rooms WHERE id = :roomId")
    suspend fun deleteChatRoomById(roomId: String)

    @Query("UPDATE chat_rooms SET isMutedForStudents = :isMuted WHERE id = :roomId")
    suspend fun setRoomStudentMute(roomId: String, isMuted: Boolean)

    @Query("UPDATE chat_rooms SET pinnedNotice = :notice, pinnedBy = :pinnedBy WHERE id = :roomId")
    suspend fun setRoomPinnedNotice(roomId: String, notice: String?, pinnedBy: String?)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestampMillis ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessage>)

    @Query("UPDATE chat_messages SET isModerated = 1, deletedBy = :moderatorName, moderationReason = :reason WHERE id = :messageId")
    suspend fun moderateMessageWithReason(messageId: Long, moderatorName: String, reason: String)

    @Query("UPDATE chat_messages SET isModerated = 1, deletedBy = :moderatorName WHERE id = :messageId")
    suspend fun moderateMessage(messageId: Long, moderatorName: String)

    @Query("UPDATE chat_messages SET isModerated = 0, deletedBy = NULL, moderationReason = NULL WHERE id = :messageId")
    suspend fun unmoderateMessage(messageId: Long)

    @Query("UPDATE chat_messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun setMessagePinned(messageId: Long, isPinned: Boolean)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteChatMessage(messageId: Long)

    @Query("DELETE FROM chat_messages WHERE channelId = :channelId")
    suspend fun deleteMessagesForChannel(channelId: String)

    // --- Log & History Clearance ---
    @Query("DELETE FROM chat_messages")
    suspend fun clearAllChatMessages()

    @Query("DELETE FROM teacher_attendance")
    suspend fun clearAllTeacherAttendance()

    @Query("DELETE FROM student_attendance")
    suspend fun clearAllStudentAttendance()

    @Query("DELETE FROM cbt_submissions")
    suspend fun clearAllCbtSubmissions()

    @Query("DELETE FROM assignment_submissions")
    suspend fun clearAllAssignmentSubmissions()

    // --- App Owner Master Licensing & Remote Control ---
    @Query("SELECT * FROM app_owner_license_config ORDER BY schoolName ASC")
    fun getAllLicenseConfigsFlow(): Flow<List<AppOwnerLicenseConfig>>

    @Query("SELECT * FROM app_owner_license_config WHERE schoolCode = :schoolCode LIMIT 1")
    fun getLicenseConfigFlow(schoolCode: String = "SCH-KINGSWAY-01"): Flow<AppOwnerLicenseConfig?>

    @Query("SELECT * FROM app_owner_license_config WHERE schoolCode = :schoolCode LIMIT 1")
    suspend fun getLicenseConfig(schoolCode: String = "SCH-KINGSWAY-01"): AppOwnerLicenseConfig?

    @Query("DELETE FROM app_owner_license_config WHERE schoolCode = :schoolCode")
    suspend fun deleteLicenseConfig(schoolCode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLicenseConfig(config: AppOwnerLicenseConfig)

    // --- App Owner Memos & Invoices ---
    @Query("SELECT * FROM app_owner_memos ORDER BY createdAtMillis DESC")
    fun getAllOwnerMemosFlow(): Flow<List<AppOwnerMemo>>

    @Query("SELECT * FROM app_owner_memos WHERE isDismissed = 0 ORDER BY createdAtMillis DESC")
    fun getActiveOwnerMemosFlow(): Flow<List<AppOwnerMemo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnerMemo(memo: AppOwnerMemo): Long

    @Update
    suspend fun updateOwnerMemo(memo: AppOwnerMemo)

    @Query("UPDATE app_owner_memos SET isPaid = :isPaid WHERE id = :id")
    suspend fun setOwnerMemoPaidStatus(id: Long, isPaid: Boolean)

    @Query("UPDATE app_owner_memos SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissOwnerMemo(id: Long)

    @Delete
    suspend fun deleteOwnerMemo(memo: AppOwnerMemo)

    // --- App Owner Payment Claims ---
    @Query("SELECT * FROM app_owner_payment_claims ORDER BY submittedAtMillis DESC")
    fun getAllPaymentClaimsFlow(): Flow<List<AppOwnerPaymentClaim>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentClaim(claim: AppOwnerPaymentClaim): Long

    @Update
    suspend fun updatePaymentClaim(claim: AppOwnerPaymentClaim)

    @Delete
    suspend fun deletePaymentClaim(claim: AppOwnerPaymentClaim)

    // --- App Owner License Keys ---
    @Query("SELECT * FROM app_owner_license_keys ORDER BY generatedAtMillis DESC")
    fun getAllLicenseKeysFlow(): Flow<List<AppOwnerLicenseKey>>

    @Query("SELECT * FROM app_owner_license_keys WHERE licenseKey = :key LIMIT 1")
    suspend fun getLicenseKey(key: String): AppOwnerLicenseKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicenseKey(licenseKey: AppOwnerLicenseKey)

    @Update
    suspend fun updateLicenseKey(licenseKey: AppOwnerLicenseKey)
}
