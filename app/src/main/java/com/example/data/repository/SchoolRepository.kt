package com.example.data.repository

import com.example.data.local.SchoolDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class SchoolRepository(private val dao: SchoolDao) {

    // --- Users & Auth ---
    val allUsers: Flow<List<SchoolUser>> = dao.getAllUsers()
    val allTeachers: Flow<List<SchoolUser>> = dao.getUsersByRole(SchoolRole.TEACHER)
    val allStudents: Flow<List<SchoolUser>> = dao.getUsersByRole(SchoolRole.STUDENT)
    val allParents: Flow<List<SchoolUser>> = dao.getUsersByRole(SchoolRole.PARENT)

    suspend fun getUserById(id: String): SchoolUser? = dao.getUserById(id)
    fun getStudentsByClass(className: String): Flow<List<SchoolUser>> = dao.getStudentsByClass(className)

    suspend fun saveUser(user: SchoolUser) = dao.insertUser(user)

    // --- Classes & Subjects ---
    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val allSubjects: Flow<List<SchoolSubject>> = dao.getAllSubjects()

    suspend fun addSubject(subject: SchoolSubject): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SchoolSubject) = dao.updateSubject(subject)
    suspend fun addClass(schoolClass: SchoolClass): Long = dao.insertClass(schoolClass)

    // --- CBT Exams & Questions ---
    val allCbtExams: Flow<List<CbtExam>> = dao.getAllCbtExams()
    fun getCbtExamsForClass(className: String): Flow<List<CbtExam>> = dao.getCbtExamsForClass(className)
    fun getCbtExamsByTeacher(teacherId: String): Flow<List<CbtExam>> = dao.getCbtExamsByTeacher(teacherId)

    suspend fun getCbtExamById(examId: Long): CbtExam? = dao.getCbtExamById(examId)
    fun getQuestionsForExam(examId: Long): Flow<List<CbtQuestion>> = dao.getQuestionsForExam(examId)
    suspend fun getQuestionsListForExam(examId: Long): List<CbtQuestion> = dao.getQuestionsListForExam(examId)

    suspend fun createCbtExam(exam: CbtExam, questions: List<CbtQuestion>): Long {
        val examId = dao.insertCbtExam(exam)
        val questionsWithExamId = questions.mapIndexed { index, q ->
            q.copy(examId = examId, questionNumber = index + 1)
        }
        dao.insertCbtQuestions(questionsWithExamId)
        return examId
    }

    suspend fun updateCbtExam(exam: CbtExam) = dao.updateCbtExam(exam)
    suspend fun deleteCbtExam(exam: CbtExam) = dao.deleteCbtExam(exam)
    suspend fun addCbtQuestion(question: CbtQuestion): Long = dao.insertCbtQuestion(question)
    suspend fun deleteCbtQuestion(question: CbtQuestion) = dao.deleteCbtQuestion(question)

    // --- CBT Submissions ---
    val allCbtSubmissions: Flow<List<CbtSubmission>> = dao.getAllCbtSubmissions()
    fun getSubmissionsForExam(examId: Long): Flow<List<CbtSubmission>> = dao.getSubmissionsForExam(examId)
    fun getSubmissionsByStudent(studentId: String): Flow<List<CbtSubmission>> = dao.getSubmissionsByStudent(studentId)

    suspend fun submitCbtExam(submission: CbtSubmission): Long = dao.insertCbtSubmission(submission)

    // --- Assignments ---
    val allAssignments: Flow<List<SchoolAssignment>> = dao.getAllAssignments()
    fun getAssignmentsForClass(className: String): Flow<List<SchoolAssignment>> = dao.getAssignmentsForClass(className)

    suspend fun createAssignment(assignment: SchoolAssignment): Long = dao.insertAssignment(assignment)
    suspend fun deleteAssignment(assignment: SchoolAssignment) = dao.deleteAssignment(assignment)

    val allAssignmentSubmissions: Flow<List<AssignmentSubmission>> = dao.getAllAssignmentSubmissions()
    fun getSubmissionsForAssignment(assignmentId: Long): Flow<List<AssignmentSubmission>> = dao.getSubmissionsForAssignment(assignmentId)
    fun getSubmissionsByStudentAssignment(studentId: String): Flow<List<AssignmentSubmission>> = dao.getSubmissionsByStudentAssignment(studentId)

    suspend fun submitAssignment(submission: AssignmentSubmission): Long = dao.insertAssignmentSubmission(submission)
    suspend fun gradeAssignment(submission: AssignmentSubmission, score: Int, feedback: String) {
        dao.updateAssignmentSubmission(
            submission.copy(
                gradedScore = score,
                teacherFeedback = feedback,
                status = "GRADED"
            )
        )
    }

    // --- Student Grades ---
    val allGrades: Flow<List<StudentGrade>> = dao.getAllGrades()
    fun getGradesForStudent(studentId: String): Flow<List<StudentGrade>> = dao.getGradesForStudent(studentId)
    fun getGradesForClass(className: String): Flow<List<StudentGrade>> = dao.getGradesForClass(className)
    fun getGradesForSubjectAndClass(subjectName: String, className: String): Flow<List<StudentGrade>> =
        dao.getGradesForSubjectAndClass(subjectName, className)

    suspend fun saveOrUpdateGrade(
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
    ): Long {
        val total = (test1 + test2 + midterm + exam).coerceIn(0.0, 100.0)
        val (gradeLetter, remarks) = calculateGradeDetails(total)

        val grade = StudentGrade(
            id = id,
            studentId = studentId,
            studentName = studentName,
            admissionNo = admissionNo,
            className = className,
            subjectName = subjectName,
            test1Score = test1,
            test2Score = test2,
            midtermScore = midterm,
            examScore = exam,
            totalScore = total,
            gradeLetter = gradeLetter,
            remarks = remarks
        )
        return if (id == 0L) {
            dao.insertGrade(grade)
        } else {
            dao.updateGrade(grade)
            id
        }
    }

    private fun calculateGradeDetails(total: Double): Pair<String, String> {
        return when {
            total >= 75.0 -> Pair("A1", "Excellent")
            total >= 70.0 -> Pair("B2", "Very Good")
            total >= 65.0 -> Pair("B3", "Good")
            total >= 60.0 -> Pair("C4", "Credit")
            total >= 55.0 -> Pair("C5", "Credit")
            total >= 50.0 -> Pair("C6", "Credit")
            total >= 45.0 -> Pair("D7", "Pass")
            total >= 40.0 -> Pair("E8", "Pass")
            else -> Pair("F9", "Fail")
        }
    }

    // --- Report Cards ---
    val allReportCards: Flow<List<ReportCard>> = dao.getAllReportCards()
    fun getReportCardForStudent(studentId: String): Flow<ReportCard?> = dao.getReportCardForStudent(studentId)
    fun getReportCardsForClass(className: String): Flow<List<ReportCard>> = dao.getReportCardsForClass(className)

    suspend fun saveReportCard(reportCard: ReportCard): Long {
        return if (reportCard.id == 0L) {
            dao.insertReportCard(reportCard)
        } else {
            dao.updateReportCard(reportCard)
            reportCard.id
        }
    }

    suspend fun setReportCardApproval(reportCardId: Long, isApproved: Boolean) {
        dao.setReportCardApproval(reportCardId, isApproved, System.currentTimeMillis())
    }

    // --- Announcements ---
    val allAnnouncements: Flow<List<SchoolAnnouncement>> = dao.getAllAnnouncements()
    fun getAnnouncementsForAudience(audience: String): Flow<List<SchoolAnnouncement>> =
        dao.getAnnouncementsForAudience(audience)

    suspend fun postAnnouncement(
        title: String,
        content: String,
        targetAudience: String,
        senderName: String,
        senderRole: String,
        category: String,
        isUrgent: Boolean
    ): Long {
        val announcement = SchoolAnnouncement(
            title = title,
            content = content,
            targetAudience = targetAudience,
            senderName = senderName,
            senderRole = senderRole,
            category = category,
            isUrgent = isUrgent,
            postedAtMillis = System.currentTimeMillis()
        )
        return dao.insertAnnouncement(announcement)
    }

    suspend fun deleteAnnouncement(announcement: SchoolAnnouncement) = dao.deleteAnnouncement(announcement)

    // --- Chat Messages & Moderation ---
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessage>> = dao.getMessagesForChannel(channelId)

    suspend fun sendChatMessage(
        channelId: String,
        channelName: String,
        senderId: String,
        senderName: String,
        senderRole: SchoolRole,
        message: String
    ): Long {
        val chatMsg = ChatMessage(
            channelId = channelId,
            channelName = channelName,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            message = message,
            timestampMillis = System.currentTimeMillis()
        )
        return dao.insertChatMessage(chatMsg)
    }

    suspend fun moderateMessage(messageId: Long, moderatorName: String) {
        dao.moderateMessage(messageId, moderatorName)
    }

    suspend fun deleteMessage(messageId: Long) {
        dao.deleteChatMessage(messageId)
    }
}
