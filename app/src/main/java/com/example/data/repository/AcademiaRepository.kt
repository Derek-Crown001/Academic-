package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class CourseAttendanceStats(
    val courseId: Long,
    val totalClasses: Int,
    val attendedClasses: Int,
    val absentClasses: Int,
    val attendancePercentage: Double,
    val threshold: Int,
    val isSafe: Boolean,
    val bunksAvailable: Int,      // Can safely miss X more classes while keeping >= threshold
    val recoveryNeeded: Int       // Need to attend X consecutive classes to reach threshold
)

data class AcademicOverview(
    val userProfile: UserProfile?,
    val currentSemester: Semester?,
    val courses: List<Course>,
    val totalCredits: Int,
    val currentSgpa: Double,
    val overallAttendancePercent: Double,
    val pendingAssignmentsCount: Int,
    val upcomingExamsCount: Int,
    val nextClassSlot: ScheduleSlotWithCourse?
)

class AcademiaRepository(private val dao: AcademiaDao) {

    // Semesters
    val allSemesters: Flow<List<Semester>> = dao.getAllSemesters()
    val currentSemester: Flow<Semester?> = dao.getCurrentSemester()

    suspend fun insertSemester(semester: Semester): Long = dao.insertSemester(semester)
    suspend fun updateSemester(semester: Semester) = dao.updateSemester(semester)
    suspend fun deleteSemester(semester: Semester) = dao.deleteSemester(semester)

    // Courses
    val allCourses: Flow<List<Course>> = dao.getAllCourses()
    fun getCoursesForSemester(semesterId: Long): Flow<List<Course>> = dao.getCoursesForSemester(semesterId)
    fun getCourseWithDetails(courseId: Long): Flow<CourseWithDetails?> = dao.getCourseWithDetails(courseId)
    fun getCoursesWithDetailsForSemester(semesterId: Long): Flow<List<CourseWithDetails>> = dao.getCoursesWithDetailsForSemester(semesterId)

    suspend fun insertCourse(course: Course): Long = dao.insertCourse(course)
    suspend fun updateCourse(course: Course) = dao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)

    // Schedule
    val allScheduleSlots: Flow<List<ScheduleSlotWithCourse>> = dao.getAllScheduleSlotsWithCourse()
    fun getScheduleForDay(dayOfWeek: Int): Flow<List<ScheduleSlotWithCourse>> = dao.getScheduleSlotsForDay(dayOfWeek)
    suspend fun insertScheduleSlot(slot: ScheduleSlot): Long = dao.insertScheduleSlot(slot)
    suspend fun deleteScheduleSlot(slot: ScheduleSlot) = dao.deleteScheduleSlot(slot)

    // Assignments
    val allAssignments: Flow<List<AssignmentWithCourse>> = dao.getAllAssignmentsWithCourse()
    val pendingAssignments: Flow<List<AssignmentWithCourse>> = dao.getPendingAssignments()
    suspend fun insertAssignment(assignment: Assignment): Long = dao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: Assignment) = dao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: Assignment) = dao.deleteAssignment(assignment)

    // Exams
    val allExams: Flow<List<ExamWithCourse>> = dao.getAllExamsWithCourse()
    suspend fun insertExam(exam: Exam): Long = dao.insertExam(exam)
    suspend fun updateExam(exam: Exam) = dao.updateExam(exam)
    suspend fun deleteExam(exam: Exam) = dao.deleteExam(exam)

    // Attendance
    val allAttendanceLogs: Flow<List<AttendanceLog>> = dao.getAllAttendanceLogs()
    fun getAttendanceForCourse(courseId: Long): Flow<List<AttendanceLog>> = dao.getAttendanceForCourse(courseId)
    suspend fun insertAttendanceLog(log: AttendanceLog): Long = dao.insertAttendanceLog(log)
    suspend fun deleteAttendanceLog(log: AttendanceLog) = dao.deleteAttendanceLog(log)

    // Notes
    val allNotes: Flow<List<AcademicNote>> = dao.getAllNotes()
    suspend fun insertNote(note: AcademicNote): Long = dao.insertNote(note)
    suspend fun updateNote(note: AcademicNote) = dao.updateNote(note)
    suspend fun deleteNote(note: AcademicNote) = dao.deleteNote(note)

    // User Profile
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    suspend fun updateUserProfile(profile: UserProfile) = dao.updateUserProfile(profile)

    // Dynamic Attendance Stats for a Course
    fun getAttendanceStatsForCourse(courseId: Long, threshold: Int): Flow<CourseAttendanceStats> {
        return dao.getAttendanceForCourse(courseId).map { logs ->
            calculateAttendanceStats(courseId, logs, threshold)
        }
    }

    // All Courses Attendance Stats
    fun getAllCoursesAttendanceStats(): Flow<Map<Long, CourseAttendanceStats>> {
        return combine(dao.getAllCourses(), dao.getAllAttendanceLogs()) { courses, logs ->
            val logsByCourse = logs.groupBy { it.courseId }
            courses.associate { course ->
                val courseLogs = logsByCourse[course.id] ?: emptyList()
                course.id to calculateAttendanceStats(course.id, courseLogs, course.attendanceThreshold)
            }
        }
    }

    private fun calculateAttendanceStats(courseId: Long, logs: List<AttendanceLog>, threshold: Int): CourseAttendanceStats {
        val total = logs.count { it.status != "CANCELLED" }
        val attended = logs.count { it.status == "PRESENT" || it.status == "LATE" }
        val absent = logs.count { it.status == "ABSENT" }
        val percentage = if (total == 0) 100.0 else (attended.toDouble() / total.toDouble()) * 100.0
        val tDec = threshold.toDouble() / 100.0

        val isSafe = percentage >= threshold.toDouble()

        // Bunk calculation: (attended) / (total + x) >= tDec => x <= (attended / tDec) - total
        val bunksAvailable = if (total == 0 || !isSafe) 0 else {
            val maxAllowedTotal = floor(attended.toDouble() / tDec).toInt()
            max(0, maxAllowedTotal - total)
        }

        // Recovery calculation: (attended + r) / (total + r) >= tDec => r * (1 - tDec) >= (tDec * total - attended) => r = ceil((tDec * total - attended) / (1 - tDec))
        val recoveryNeeded = if (isSafe || total == 0) 0 else {
            val needed = ceil((tDec * total - attended) / (1.0 - tDec)).toInt()
            max(0, needed)
        }

        return CourseAttendanceStats(
            courseId = courseId,
            totalClasses = total,
            attendedClasses = attended,
            absentClasses = absent,
            attendancePercentage = percentage,
            threshold = threshold,
            isSafe = isSafe,
            bunksAvailable = bunksAvailable,
            recoveryNeeded = recoveryNeeded
        )
    }

    // Academic Overview Stream
    val academicOverview: Flow<AcademicOverview> = combine(
        combine(dao.getUserProfile(), dao.getCurrentSemester(), dao.getAllCourses()) { p, s, c -> Triple(p, s, c) },
        combine(dao.getAllAssignmentsWithCourse(), dao.getAllExamsWithCourse()) { a, e -> Pair(a, e) },
        combine(dao.getAllAttendanceLogs(), dao.getAllScheduleSlotsWithCourse()) { l, sl -> Pair(l, sl) }
    ) { (profile, currentSem, courses), (assignments, exams), (logs, slots) ->
        val totalCredits = courses.sumOf { it.credits }
        val sgpa = calculateGpa(courses)

        val totalClasses = logs.count { it.status != "CANCELLED" }
        val totalAttended = logs.count { it.status == "PRESENT" || it.status == "LATE" }
        val overallAttendance = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0

        val pendingAssignmentsCount = assignments.count { it.assignment.status != "COMPLETED" && it.assignment.status != "GRADED" }
        val now = System.currentTimeMillis()
        val upcomingExamsCount = exams.count { it.exam.examDateMillis >= now }

        // Find next class today
        val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        // Calendar.MONDAY = 2, convert to 1=Mon...7=Sun
        val dayOfWeek = if (currentDay == java.util.Calendar.SUNDAY) 7 else currentDay - 1
        val todaySlots = slots.filter { it.slot.dayOfWeek == dayOfWeek }.sortedBy { it.slot.startTime }
        val nextSlot = todaySlots.firstOrNull()

        AcademicOverview(
            userProfile = profile,
            currentSemester = currentSem,
            courses = courses,
            totalCredits = totalCredits,
            currentSgpa = sgpa,
            overallAttendancePercent = overallAttendance,
            pendingAssignmentsCount = pendingAssignmentsCount,
            upcomingExamsCount = upcomingExamsCount,
            nextClassSlot = nextSlot
        )
    }

    companion object {
        fun calculateGpa(courses: List<Course>): Double {
            if (courses.isEmpty()) return 4.00
            var totalPoints = 0.0
            var totalCredits = 0
            for (course in courses) {
                val gradePoint = gradeToPoint(course.currentScore)
                totalPoints += gradePoint * course.credits
                totalCredits += course.credits
            }
            return if (totalCredits == 0) 4.00 else (totalPoints / totalCredits)
        }

        fun gradeToPoint(score: Double): Double {
            return when {
                score >= 93.0 -> 4.00 // A
                score >= 90.0 -> 3.70 // A-
                score >= 87.0 -> 3.30 // B+
                score >= 83.0 -> 3.00 // B
                score >= 80.0 -> 2.70 // B-
                score >= 77.0 -> 2.30 // C+
                score >= 73.0 -> 2.00 // C
                score >= 70.0 -> 1.70 // C-
                score >= 65.0 -> 1.30 // D+
                score >= 60.0 -> 1.00 // D
                else -> 0.00          // F
            }
        }

        fun scoreToLetter(score: Double): String {
            return when {
                score >= 93.0 -> "A"
                score >= 90.0 -> "A-"
                score >= 87.0 -> "B+"
                score >= 83.0 -> "B"
                score >= 80.0 -> "B-"
                score >= 77.0 -> "C+"
                score >= 73.0 -> "C"
                score >= 70.0 -> "C-"
                score >= 65.0 -> "D+"
                score >= 60.0 -> "D"
                else -> "F"
            }
        }
    }
}
