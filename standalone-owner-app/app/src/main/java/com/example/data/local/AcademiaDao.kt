package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

data class CourseWithDetails(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val slots: List<ScheduleSlot>,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val assignments: List<Assignment>,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val exams: List<Exam>,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val attendanceLogs: List<AttendanceLog>
)

data class AssignmentWithCourse(
    @Embedded val assignment: Assignment,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: Course?
)

data class ExamWithCourse(
    @Embedded val exam: Exam,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: Course?
)

data class ScheduleSlotWithCourse(
    @Embedded val slot: ScheduleSlot,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "id"
    )
    val course: Course?
)

@Dao
interface AcademiaDao {
    // Semesters
    @Query("SELECT * FROM semesters ORDER BY id DESC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("SELECT * FROM semesters WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentSemester(): Flow<Semester?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester): Long

    @Update
    suspend fun updateSemester(semester: Semester)

    @Delete
    suspend fun deleteSemester(semester: Semester)

    // Courses
    @Query("SELECT * FROM courses WHERE semesterId = :semesterId ORDER BY code ASC")
    fun getCoursesForSemester(semesterId: Long): Flow<List<Course>>

    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: Long): Course?

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    fun getCourseWithDetails(id: Long): Flow<CourseWithDetails?>

    @Transaction
    @Query("SELECT * FROM courses WHERE semesterId = :semesterId")
    fun getCoursesWithDetailsForSemester(semesterId: Long): Flow<List<CourseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    // Schedule Slots
    @Transaction
    @Query("SELECT * FROM schedule_slots ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllScheduleSlotsWithCourse(): Flow<List<ScheduleSlotWithCourse>>

    @Transaction
    @Query("SELECT * FROM schedule_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getScheduleSlotsForDay(dayOfWeek: Int): Flow<List<ScheduleSlotWithCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleSlot(slot: ScheduleSlot): Long

    @Delete
    suspend fun deleteScheduleSlot(slot: ScheduleSlot)

    // Assignments
    @Transaction
    @Query("SELECT * FROM assignments ORDER BY dueDateMillis ASC")
    fun getAllAssignmentsWithCourse(): Flow<List<AssignmentWithCourse>>

    @Transaction
    @Query("SELECT * FROM assignments WHERE status != 'COMPLETED' AND status != 'GRADED' ORDER BY dueDateMillis ASC")
    fun getPendingAssignments(): Flow<List<AssignmentWithCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)

    // Exams
    @Transaction
    @Query("SELECT * FROM exams ORDER BY examDateMillis ASC")
    fun getAllExamsWithCourse(): Flow<List<ExamWithCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    // Attendance
    @Query("SELECT * FROM attendance_logs WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getAttendanceForCourse(courseId: Long): Flow<List<AttendanceLog>>

    @Query("SELECT * FROM attendance_logs ORDER BY dateMillis DESC")
    fun getAllAttendanceLogs(): Flow<List<AttendanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLog): Long

    @Delete
    suspend fun deleteAttendanceLog(log: AttendanceLog)

    // Notes
    @Query("SELECT * FROM academic_notes ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun getAllNotes(): Flow<List<AcademicNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AcademicNote): Long

    @Update
    suspend fun updateNote(note: AcademicNote)

    @Delete
    suspend fun deleteNote(note: AcademicNote)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)
}
