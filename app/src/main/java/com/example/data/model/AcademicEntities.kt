package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isCurrent: Boolean = false,
    val targetGpa: Double = 3.8,
    val startDate: String = "",
    val endDate: String = ""
)

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("semesterId")]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val name: String,
    val code: String,
    val instructor: String = "",
    val room: String = "",
    val credits: Int = 3,
    val colorHex: String = "#2563EB",
    val targetGrade: String = "A",
    val currentScore: Double = 0.0,
    val maxScore: Double = 100.0,
    val syllabusNotes: String = "",
    val attendanceThreshold: Int = 75 // Target %
)

@Entity(
    tableName = "schedule_slots",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class ScheduleSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ... 7 = Sunday
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:30"
    val room: String = "",
    val slotType: String = "Lecture" // "Lecture", "Lab", "Seminar", "Tutorial"
)

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val status: String = "PENDING",   // "PENDING", "IN_PROGRESS", "COMPLETED", "GRADED"
    val scoreObtained: Double? = null,
    val maxScore: Double = 100.0,
    val subtasks: String = "" // Line-separated checklist
)

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val examDateMillis: Long,
    val durationMinutes: Int = 120,
    val room: String = "",
    val weightPercentage: Double = 30.0,
    val targetScore: Double = 90.0,
    val actualScore: Double? = null,
    val topics: String = ""
)

@Entity(
    tableName = "attendance_logs",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val dateMillis: Long,
    val status: String = "PRESENT", // "PRESENT", "ABSENT", "CANCELLED", "LATE"
    val note: String = ""
)

@Entity(tableName = "academic_notes")
data class AcademicNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long? = null,
    val title: String,
    val content: String,
    val tags: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1,
    val name: String = "Alex Rivera",
    val studentId: String = "ST-884920",
    val major: String = "Computer Science & Data",
    val university: String = "Apex Institute of Technology",
    val currentCgpa: Double = 3.82,
    val targetCgpa: Double = 3.90,
    val earnedCredits: Int = 78,
    val totalDegreeCredits: Int = 120
)
