package com.example.data.repository

import com.example.data.local.SchoolDao
import com.example.data.model.*
import com.example.service.firestore.FirestoreSchoolService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchoolRepository(
    private val dao: SchoolDao,
    private val firestoreService: FirestoreSchoolService = FirestoreSchoolService()
) {

    // --- Users & Auth ---
    val allUsers: Flow<List<SchoolUser>> = dao.getAllUsers()
    val allTeachers: Flow<List<SchoolUser>> = dao.getUsersByRole(SchoolRole.TEACHER)
    val allStudents: Flow<List<SchoolUser>> = dao.getUsersByRole(SchoolRole.STUDENT)
    val allParents: Flow<List<SchoolUser>> = dao.getUsersByRole(SchoolRole.PARENT)

    suspend fun getUserById(id: String): SchoolUser? = dao.getUserById(id)
    fun getStudentsByClass(className: String): Flow<List<SchoolUser>> = dao.getStudentsByClass(className)

    suspend fun saveUser(user: SchoolUser) {
        dao.insertUser(user)
        firestoreService.saveUser(user)
    }

    suspend fun updateUser(user: SchoolUser) {
        dao.updateUser(user)
        firestoreService.saveUser(user)
    }

    suspend fun deleteUser(user: SchoolUser) {
        dao.deleteUser(user)
        firestoreService.deleteUser(user.id)
    }

    suspend fun deleteUserById(id: String) {
        dao.deleteUserById(id)
        firestoreService.deleteUser(id)
    }

    // --- Classes & Subjects ---
    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val allSubjects: Flow<List<SchoolSubject>> = dao.getAllSubjects()

    suspend fun addSubject(subject: SchoolSubject): Long {
        val id = dao.insertSubject(subject)
        firestoreService.saveSubject(subject.copy(id = id))
        return id
    }

    suspend fun updateSubject(subject: SchoolSubject) {
        dao.updateSubject(subject)
        firestoreService.saveSubject(subject)
    }

    suspend fun deleteSubject(subject: SchoolSubject) {
        dao.deleteSubject(subject)
        firestoreService.deleteSubject(subject.id, subject.code)
    }

    suspend fun addClass(schoolClass: SchoolClass): Long {
        val id = dao.insertClass(schoolClass)
        firestoreService.saveClass(schoolClass.copy(id = id))
        return id
    }

    suspend fun updateClass(schoolClass: SchoolClass) {
        dao.updateClass(schoolClass)
        firestoreService.saveClass(schoolClass)
    }

    suspend fun deleteClass(schoolClass: SchoolClass) {
        dao.deleteClass(schoolClass)
        firestoreService.deleteClass(schoolClass.id, schoolClass.name)
    }

    suspend fun deleteClassById(id: Long) {
        dao.deleteClassById(id)
        firestoreService.deleteClass(id, "Class_$id")
    }

    // --- School Profile & Multipurpose Settings ---
    val schoolProfile: Flow<SchoolProfile?> = dao.getSchoolProfile()
    suspend fun updateSchoolProfile(profile: SchoolProfile) {
        dao.insertSchoolProfile(profile)
        firestoreService.saveSchoolProfile(profile)
    }

    // --- Cloud Synchronization Helpers ---
    suspend fun syncAllLocalDataToFirestore(): Boolean {
        return try {
            val users = dao.getAllUsers().first()
            val classes = dao.getAllClasses().first()
            val subjects = dao.getAllSubjects().first()
            val profile = dao.getSchoolProfile().first()

            val uRes = firestoreService.syncAllUsersBatch(users)
            val cRes = firestoreService.syncAllClassesBatch(classes)
            val sRes = firestoreService.syncAllSubjectsBatch(subjects)
            if (profile != null) {
                firestoreService.saveSchoolProfile(profile)
            }
            uRes && cRes && sRes
        } catch (e: Exception) {
            false
        }
    }

    fun observeCloudUsers() = firestoreService.observeUsers()
    fun observeCloudClasses() = firestoreService.observeClasses()
    fun observeCloudSubjects() = firestoreService.observeSubjects()
    fun observeCloudProfile() = firestoreService.observeSchoolProfile()

    // --- Teacher Attendance & Time Clock ---
    val allTeacherAttendance: Flow<List<TeacherAttendance>> = dao.getAllTeacherAttendance()

    fun getAttendanceForDate(dateString: String): Flow<List<TeacherAttendance>> =
        dao.getAttendanceForDate(dateString)

    fun getTodayAttendanceForTeacher(teacherId: String, dateString: String): Flow<TeacherAttendance?> =
        dao.getTodayAttendanceForTeacher(teacherId, dateString)

    suspend fun clockInTeacher(teacherId: String, teacherName: String, remarks: String = "Clocked in via Teacher Portal"): Long {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val existing = dao.getTodayAttendanceForTeacherDirect(teacherId, todayStr)
        return if (existing != null) {
            val updated = existing.copy(status = "CLOCKED_IN", clockOutTimeMillis = null, remarks = remarks)
            dao.updateTeacherAttendance(updated)
            existing.id
        } else {
            val record = TeacherAttendance(
                teacherId = teacherId,
                teacherName = teacherName,
                dateString = todayStr,
                clockInTimeMillis = System.currentTimeMillis(),
                clockOutTimeMillis = null,
                status = "CLOCKED_IN",
                remarks = remarks
            )
            dao.insertTeacherAttendance(record)
        }
    }

    suspend fun clockOutTeacher(teacherId: String, remarks: String = "Clocked out via Teacher Portal") {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val existing = dao.getTodayAttendanceForTeacherDirect(teacherId, todayStr)
        if (existing != null) {
            val updated = existing.copy(
                clockOutTimeMillis = System.currentTimeMillis(),
                status = "CLOCKED_OUT",
                remarks = remarks
            )
            dao.updateTeacherAttendance(updated)
        } else {
            val record = TeacherAttendance(
                teacherId = teacherId,
                teacherName = "Teacher",
                dateString = todayStr,
                clockInTimeMillis = System.currentTimeMillis() - 3600000L,
                clockOutTimeMillis = System.currentTimeMillis(),
                status = "CLOCKED_OUT",
                remarks = remarks
            )
            dao.insertTeacherAttendance(record)
        }
    }

    // --- Student Daily Attendance / Register ---
    fun getStudentAttendanceForClassAndDate(className: String, dateString: String): Flow<List<StudentAttendanceRecord>> =
        dao.getStudentAttendanceForClassAndDate(className, dateString)

    fun getAttendanceForStudent(studentId: String): Flow<List<StudentAttendanceRecord>> =
        dao.getAttendanceForStudent(studentId)

    suspend fun markStudentAttendance(record: StudentAttendanceRecord) =
        dao.insertStudentAttendance(record)

    suspend fun markStudentAttendanceBatch(records: List<StudentAttendanceRecord>) =
        dao.insertStudentAttendanceList(records)

    suspend fun markClassAttendanceBatch(records: List<StudentAttendanceRecord>) =
        dao.insertStudentAttendanceList(records)

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
    suspend fun addCbtQuestions(questions: List<CbtQuestion>) = dao.insertCbtQuestions(questions)
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
    fun getApprovedReportCardForStudent(studentId: String): Flow<ReportCard?> = dao.getApprovedReportCardForStudent(studentId)
    fun getReportCardsForClass(className: String): Flow<List<ReportCard>> = dao.getReportCardsForClass(className)

    suspend fun saveReportCard(reportCard: ReportCard): Long {
        return if (reportCard.id == 0L) {
            dao.insertReportCard(reportCard)
        } else {
            dao.updateReportCard(reportCard)
            reportCard.id
        }
    }

    suspend fun setReportCardTeacherPublished(reportCardId: Long, isPublished: Boolean) {
        dao.setTeacherPublished(reportCardId, isPublished, System.currentTimeMillis())
    }

    suspend fun setReportCardApproval(reportCardId: Long, isApproved: Boolean) {
        dao.setReportCardApproval(reportCardId, isApproved, System.currentTimeMillis())
    }

    suspend fun autoRegisterCbtScoreToGrade(exam: CbtExam, submission: CbtSubmission) {
        val existingGrade = dao.getGradeDirect(submission.studentId, exam.subjectName)
        val test1 = existingGrade?.test1Score ?: 12.0
        val test2 = existingGrade?.test2Score ?: 13.0
        val midterm = existingGrade?.midtermScore ?: 18.0
        val examScore = existingGrade?.examScore ?: 42.0

        val newGrade = when (exam.examType.uppercase()) {
            "1ST_CA", "TEST1" -> {
                val score15 = (submission.percentage / 100.0 * 15.0).coerceIn(0.0, 15.0)
                (existingGrade ?: createDefaultGrade(submission, exam.subjectName)).copy(test1Score = score15)
            }
            "2ND_CA", "TEST2" -> {
                val score15 = (submission.percentage / 100.0 * 15.0).coerceIn(0.0, 15.0)
                (existingGrade ?: createDefaultGrade(submission, exam.subjectName)).copy(test2Score = score15)
            }
            "MIDTERM" -> {
                val score20 = (submission.percentage / 100.0 * 20.0).coerceIn(0.0, 20.0)
                (existingGrade ?: createDefaultGrade(submission, exam.subjectName)).copy(midtermScore = score20)
            }
            else -> {
                val score50 = (submission.percentage / 100.0 * 50.0).coerceIn(0.0, 50.0)
                (existingGrade ?: createDefaultGrade(submission, exam.subjectName)).copy(examScore = score50)
            }
        }
        val total = (newGrade.test1Score + newGrade.test2Score + newGrade.midtermScore + newGrade.examScore).coerceIn(0.0, 100.0)
        val (letter, remark) = calculateGradeDetails(total)
        val finalGrade = newGrade.copy(totalScore = total, gradeLetter = letter, remarks = remark)
        if (finalGrade.id == 0L) {
            dao.insertGrade(finalGrade)
        } else {
            dao.updateGrade(finalGrade)
        }
    }

    private fun createDefaultGrade(submission: CbtSubmission, subjectName: String): StudentGrade {
        return StudentGrade(
            studentId = submission.studentId,
            studentName = submission.studentName,
            admissionNo = "ADM-${submission.studentId}",
            className = submission.className,
            subjectName = subjectName,
            test1Score = 12.0,
            test2Score = 13.0,
            midtermScore = 18.0,
            examScore = 40.0,
            totalScore = 83.0,
            gradeLetter = "A1",
            remarks = "Good"
        )
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

    // --- Chat Rooms & Moderation ---
    val allChatRooms: Flow<List<ChatRoom>> = dao.getAllChatRooms()
    fun getChatRoomById(roomId: String): Flow<ChatRoom?> = dao.getChatRoomById(roomId)

    suspend fun createChatRoom(room: ChatRoom): Long {
        val id = dao.insertChatRoom(room)
        firestoreService.saveChatRoom(room)
        return id
    }

    suspend fun updateChatRoom(room: ChatRoom) {
        dao.updateChatRoom(room)
        firestoreService.saveChatRoom(room)
    }

    suspend fun deleteChatRoom(roomId: String) {
        dao.deleteChatRoomById(roomId)
        dao.deleteMessagesForChannel(roomId)
        firestoreService.deleteChatRoom(roomId)
    }

    suspend fun toggleRoomStudentMute(roomId: String, isMuted: Boolean) {
        dao.setRoomStudentMute(roomId, isMuted)
        firestoreService.setRoomStudentMute(roomId, isMuted)
    }

    suspend fun setRoomPinnedNotice(roomId: String, notice: String?, pinnedBy: String?) {
        dao.setRoomPinnedNotice(roomId, notice, pinnedBy)
        firestoreService.setRoomPinnedNotice(roomId, notice, pinnedBy)
    }

    // Real-time Firestore Observation for Rooms & Messages
    fun observeFirestoreChatRooms(schoolId: String = "SCH-KINGSWAY-01"): Flow<List<ChatRoom>> =
        firestoreService.observeChatRooms(schoolId)

    fun observeFirestoreRoomMessages(channelId: String, schoolId: String = "SCH-KINGSWAY-01"): Flow<List<ChatMessage>> =
        firestoreService.observeRoomMessages(channelId, schoolId)

    suspend fun syncFirestoreRoomsToLocal(rooms: List<ChatRoom>) {
        if (rooms.isNotEmpty()) {
            dao.insertChatRooms(rooms)
        }
    }

    suspend fun syncFirestoreMessagesToLocal(messages: List<ChatMessage>) {
        if (messages.isNotEmpty()) {
            dao.insertChatMessages(messages)
        }
    }

    // --- Activity Log & History Clearance ---
    suspend fun clearAllDemoActivityLogs(schoolId: String) {
        dao.clearAllChatMessages()
        dao.clearAllTeacherAttendance()
        dao.clearAllStudentAttendance()
        dao.clearAllCbtSubmissions()
        dao.clearAllAssignmentSubmissions()
        firestoreService.clearAllSchoolActivityLogs(schoolId)
    }

    // --- Chat Messages & Moderation ---
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessage>> = dao.getMessagesForChannel(channelId)

    suspend fun sendChatMessage(
        channelId: String,
        channelName: String,
        senderId: String,
        senderName: String,
        senderRole: SchoolRole,
        message: String,
        senderAvatarColor: String = "#1E40AF",
        senderPhotoUri: String? = null,
        replyToMessageId: Long? = null,
        replyToSender: String? = null,
        replyToText: String? = null,
        schoolId: String = "SCH-KINGSWAY-01"
    ): Long {
        val chatMsg = ChatMessage(
            channelId = channelId,
            channelName = channelName,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            message = message,
            timestampMillis = System.currentTimeMillis(),
            senderAvatarColor = senderAvatarColor,
            senderPhotoUri = senderPhotoUri,
            replyToMessageId = replyToMessageId,
            replyToSender = replyToSender,
            replyToText = replyToText
        )
        val localId = dao.insertChatMessage(chatMsg)
        val msgWithId = if (localId > 0) chatMsg.copy(id = localId) else chatMsg
        firestoreService.saveChatMessage(msgWithId, schoolId)
        return localId
    }

    suspend fun moderateMessageWithReason(
        messageId: Long,
        moderatorName: String,
        reason: String,
        schoolId: String = "SCH-KINGSWAY-01"
    ) {
        dao.moderateMessageWithReason(messageId, moderatorName, reason)
        firestoreService.moderateChatMessage(messageId, moderatorName, reason, schoolId)
    }

    suspend fun moderateMessage(
        messageId: Long,
        moderatorName: String,
        schoolId: String = "SCH-KINGSWAY-01"
    ) {
        dao.moderateMessage(messageId, moderatorName)
        firestoreService.moderateChatMessage(messageId, moderatorName, "Moderated by staff", schoolId)
    }

    suspend fun unmoderateMessage(messageId: Long, schoolId: String = "SCH-KINGSWAY-01") {
        dao.unmoderateMessage(messageId)
        firestoreService.unmoderateChatMessage(messageId, schoolId)
    }

    suspend fun setChatMessagePinned(messageId: Long, isPinned: Boolean) {
        dao.setMessagePinned(messageId, isPinned)
    }

    suspend fun deleteMessage(messageId: Long, schoolId: String = "SCH-KINGSWAY-01") {
        dao.deleteChatMessage(messageId)
        firestoreService.deleteChatMessage(messageId, schoolId)
    }
}
