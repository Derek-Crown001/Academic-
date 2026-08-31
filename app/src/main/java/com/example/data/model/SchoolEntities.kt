package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SchoolRole {
    ADMIN,
    TEACHER,
    STUDENT,
    PARENT,
    APP_OWNER
}

@Entity(tableName = "school_users")
data class SchoolUser(
    @PrimaryKey val id: String, // e.g. "ADM-001", "TCH-101", "STU-2025-042", "PAR-2025-042"
    val name: String,
    val role: SchoolRole,
    val email: String = "",
    val phone: String = "",
    val passcode: String = "1234", // Used for access control
    val className: String = "", // e.g. "SS 2 Gold"
    val assignedSubjects: String = "", // e.g. "Mathematics, Physics" (for teachers)
    val studentChildId: String? = null, // for parents to link their child
    val studentChildName: String? = null,
    val avatarColorHex: String = "#1E40AF",
    val photoUri: String? = null, // Profile picture from local storage / phone file manager
    val gender: String = "Female", // "Male" or "Female"
    val dateOfBirth: String = "2009-05-14",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val guardianEmail: String = "",
    val residentialAddress: String = "",
    val bloodGroup: String = "O+",
    val genotype: String = "AA",
    val admissionDate: String = "2024-09-10",
    val stateOfOrigin: String = "Lagos"
)

@Entity(tableName = "school_classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "SS 2 Gold"
    val level: String, // "SS 2"
    val arm: String, // "Gold"
    val classTeacherId: String = "",
    val classTeacherName: String = "",
    val studentCount: Int = 32,
    val room: String = "Block B, Room 4"
)

@Entity(tableName = "school_subjects")
data class SchoolSubject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // "Mathematics"
    val code: String, // "MTH 201"
    val classLevel: String, // "SS 2"
    val teacherId: String = "",
    val teacherName: String = "",
    val colorHex: String = "#2563EB",
    val periodsPerWeek: Int = 4
)

@Entity(tableName = "cbt_exams")
data class CbtExam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectName: String,
    val className: String, // "SS 2 Gold" or "SS 2 All"
    val teacherId: String,
    val teacherName: String,
    val examType: String = "EXAM", // "EXAM" or "TEST"
    val durationMinutes: Int = 30,
    val totalMarks: Int = 50,
    val passMark: Int = 25,
    val isPublished: Boolean = true,
    val createdDateMillis: Long = System.currentTimeMillis(),
    val instructions: String = "Answer all questions. Each question carries equal marks."
)

@Entity(
    tableName = "cbt_questions",
    foreignKeys = [
        ForeignKey(
            entity = CbtExam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("examId")]
)
data class CbtQuestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val questionNumber: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", or "D"
    val explanation: String = "",
    val marks: Int = 5
)

@Entity(tableName = "cbt_submissions")
data class CbtSubmission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val examTitle: String,
    val subjectName: String,
    val studentId: String,
    val studentName: String,
    val className: String,
    val score: Int,
    val totalMarks: Int,
    val percentage: Double,
    val isPassed: Boolean,
    val submittedAtMillis: Long = System.currentTimeMillis(),
    val answersRecord: String = "" // e.g. "1:A|2:C|3:B"
)

@Entity(tableName = "school_assignments")
data class SchoolAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectName: String,
    val className: String,
    val teacherName: String,
    val dueDateMillis: Long,
    val maxMarks: Int = 20,
    val description: String,
    val instructions: String = "",
    val createdDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "assignment_submissions")
data class AssignmentSubmission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assignmentId: Long,
    val assignmentTitle: String,
    val subjectName: String,
    val studentId: String,
    val studentName: String,
    val content: String,
    val submittedAtMillis: Long = System.currentTimeMillis(),
    val gradedScore: Int? = null,
    val maxMarks: Int = 20,
    val teacherFeedback: String = "",
    val status: String = "SUBMITTED" // "SUBMITTED", "GRADED"
)

@Entity(tableName = "student_grades")
data class StudentGrade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val admissionNo: String,
    val className: String,
    val subjectName: String,
    val term: String = "1st Term",
    val session: String = "2025/2026",
    val test1Score: Double = 12.0,    // max 15
    val test2Score: Double = 13.0,    // max 15
    val midtermScore: Double = 18.0,  // max 20
    val examScore: Double = 42.0,     // max 50
    val totalScore: Double = 85.0,    // max 100
    val gradeLetter: String = "A1",   // A1, B2, B3, C4, C5, C6, D7, E8, F9
    val remarks: String = "Excellent"
)

@Entity(tableName = "report_cards")
data class ReportCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val admissionNo: String,
    val className: String,
    val term: String = "1st Term",
    val session: String = "2025/2026",
    val totalScore: Double = 680.0,
    val maxPossibleScore: Double = 800.0,
    val averageScore: Double = 85.0,
    val classPosition: String = "2nd out of 32",
    val attendancePresent: Int = 63,
    val attendanceTotal: Int = 65,
    val classTeacherRemark: String = "An exemplary and highly disciplined student with great aptitude in Science and Mathematics.",
    val principalRemark: String = "Outstanding academic performance. Keep up the high standard of excellence.",
    val isPublishedByTeacher: Boolean = true,
    val teacherPublishedAtMillis: Long? = System.currentTimeMillis(),
    val isApprovedByAdmin: Boolean = true,
    val approvedAtMillis: Long? = System.currentTimeMillis()
)

@Entity(tableName = "school_profile")
data class SchoolProfile(
    @PrimaryKey val id: Long = 1,
    val schoolCode: String = "SCH-KINGSWAY-01",
    val schoolName: String = "Kingsway Model International College",
    val schoolMotto: String = "Excellence, Knowledge & Integrity",
    val schoolAddress: String = "Plot 12, Academic Avenue, Victoria Island, Lagos",
    val schoolEmail: String = "info@kingswaycollege.edu.ng",
    val schoolPhone: String = "+234 803 123 4567",
    val academicSession: String = "2025/2026",
    val currentTerm: String = "1st Term",
    val principalName: String = "Dr. C. Adebayo, Ph.D",
    val schoolLogoBadge: String = "KMIC"
)

@Entity(tableName = "teacher_attendance")
data class TeacherAttendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: String,
    val teacherName: String,
    val dateString: String, // e.g. "2026-08-25"
    val clockInTimeMillis: Long,
    val clockOutTimeMillis: Long? = null,
    val status: String = "CLOCKED_IN", // "CLOCKED_IN", "CLOCKED_OUT"
    val remarks: String = "On duty"
)

@Entity(tableName = "student_attendance")
data class StudentAttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val className: String,
    val dateString: String, // "2026-08-25"
    val status: String = "PRESENT", // "PRESENT", "ABSENT", "LATE"
    val markedByTeacherId: String = "",
    val markedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "school_announcements")
data class SchoolAnnouncement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val targetAudience: String, // "ALL", "STUDENT", "TEACHER", "PARENT"
    val senderName: String,
    val senderRole: String, // "ADMIN", "TEACHER"
    val category: String = "GENERAL", // "ACADEMIC", "EXAM", "FEES", "EVENT", "GENERAL"
    val isUrgent: Boolean = false,
    val postedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey val id: String, // e.g. "STAFF_GENERAL", "CLASS_SS2_GOLD", "STUDY_MATH_SS2"
    val title: String,
    val description: String = "",
    val topic: String = "General", // "Official Class", "Staff Only", "STEM Hub", "Peer Study", "Humanities"
    val allowedRoles: String = "ALL", // "ALL", "STAFF", "STUDENTS"
    val targetClass: String = "ALL", // "ALL", "SS 2 Gold", "SS 3 Science", etc.
    val isModerated: Boolean = true,
    val isMutedForStudents: Boolean = false, // When true, students are read-only; only teachers/admins can post
    val pinnedNotice: String? = null,
    val pinnedBy: String? = null,
    val createdBy: String = "System",
    val creatorRole: SchoolRole = SchoolRole.ADMIN,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val colorHex: String = "#1E40AF",
    val iconName: String = "Forum",
    val memberCount: Int = 32
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String, // "STAFF_GENERAL", "CLASS_SS2_GOLD", "CLASS_SS3_SCIENCE"
    val channelName: String, // "Staff General Room", "SS 2 Gold Class Chat"
    val senderId: String,
    val senderName: String,
    val senderRole: SchoolRole,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isModerated: Boolean = false,
    val moderationReason: String? = null, // "Off-topic chat", "Inappropriate language", "Exam malpractice", "Spam"
    val deletedBy: String? = null,
    val isPinned: Boolean = false,
    val replyToMessageId: Long? = null,
    val replyToSender: String? = null,
    val replyToText: String? = null,
    val senderAvatarColor: String = "#1E40AF",
    val senderPhotoUri: String? = null
)
