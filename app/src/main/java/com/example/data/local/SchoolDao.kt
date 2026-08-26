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

    // --- Classes ---
    @Query("SELECT * FROM school_classes ORDER BY level ASC, arm ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

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

    // --- Announcements ---
    @Query("SELECT * FROM school_announcements ORDER BY postedAtMillis DESC")
    fun getAllAnnouncements(): Flow<List<SchoolAnnouncement>>

    @Query("SELECT * FROM school_announcements WHERE targetAudience = :audience OR targetAudience = 'ALL' ORDER BY postedAtMillis DESC")
    fun getAnnouncementsForAudience(audience: String): Flow<List<SchoolAnnouncement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: SchoolAnnouncement): Long

    @Delete
    suspend fun deleteAnnouncement(announcement: SchoolAnnouncement)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestampMillis ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Query("UPDATE chat_messages SET isModerated = 1, deletedBy = :moderatorName WHERE id = :messageId")
    suspend fun moderateMessage(messageId: Long, moderatorName: String)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteChatMessage(messageId: Long)
}
