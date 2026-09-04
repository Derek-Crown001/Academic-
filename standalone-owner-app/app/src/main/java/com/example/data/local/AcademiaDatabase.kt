package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Semester::class,
        Course::class,
        ScheduleSlot::class,
        Assignment::class,
        Exam::class,
        AttendanceLog::class,
        AcademicNote::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AcademiaDatabase : RoomDatabase() {
    abstract fun academiaDao(): AcademiaDao

    companion object {
        @Volatile
        private var INSTANCE: AcademiaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AcademiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcademiaDatabase::class.java,
                    "academia_track_db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.academiaDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: AcademiaDao) {
            // User Profile
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Alex Rivera",
                    studentId = "CS-2026-8942",
                    major = "Computer Science & Engineering",
                    university = "Stanford University",
                    currentCgpa = 3.84,
                    targetCgpa = 3.90,
                    earnedCredits = 74,
                    totalDegreeCredits = 120
                )
            )

            // Current Semester
            val sem1Id = dao.insertSemester(
                Semester(
                    name = "Fall 2026",
                    isCurrent = true,
                    targetGpa = 3.90,
                    startDate = "Aug 25, 2026",
                    endDate = "Dec 18, 2026"
                )
            )

            val semPastId = dao.insertSemester(
                Semester(
                    name = "Spring 2026",
                    isCurrent = false,
                    targetGpa = 3.80,
                    startDate = "Jan 12, 2026",
                    endDate = "May 22, 2026"
                )
            )

            // Courses for Fall 2026
            val cs301 = dao.insertCourse(
                Course(
                    semesterId = sem1Id,
                    name = "Algorithms & Complexity",
                    code = "CS 301",
                    instructor = "Dr. Elena Vance",
                    room = "Gates Hall 104",
                    credits = 4,
                    colorHex = "#2563EB", // Royal Blue
                    targetGrade = "A",
                    currentScore = 93.4,
                    maxScore = 100.0,
                    syllabusNotes = "Dynamic programming, graph theory, NP-completeness, randomized algorithms.",
                    attendanceThreshold = 75
                )
            )

            val math240 = dao.insertCourse(
                Course(
                    semesterId = sem1Id,
                    name = "Linear Algebra & Optimization",
                    code = "MATH 240",
                    instructor = "Prof. Marcus Brody",
                    room = "Science Ctr 302",
                    credits = 4,
                    colorHex = "#7C3AED", // Violet
                    targetGrade = "A",
                    currentScore = 88.5,
                    maxScore = 100.0,
                    syllabusNotes = "Vector spaces, eigen-decomposition, convex optimization, SVD factorization.",
                    attendanceThreshold = 80
                )
            )

            val cs350 = dao.insertCourse(
                Course(
                    semesterId = sem1Id,
                    name = "Cloud Systems & Architecture",
                    code = "CS 350",
                    instructor = "Dr. Sarah Chen",
                    room = "Turing Lab 201",
                    credits = 3,
                    colorHex = "#0D9488", // Teal
                    targetGrade = "A+",
                    currentScore = 96.0,
                    maxScore = 100.0,
                    syllabusNotes = "Distributed consensus, Raft, microservices orchestration, storage tiering.",
                    attendanceThreshold = 75
                )
            )

            val phys210 = dao.insertCourse(
                Course(
                    semesterId = sem1Id,
                    name = "Quantum Information & Waves",
                    code = "PHYS 210",
                    instructor = "Dr. Robert Sterling",
                    room = "Hansen Lab 110",
                    credits = 4,
                    colorHex = "#F59E0B", // Amber
                    targetGrade = "A-",
                    currentScore = 86.2,
                    maxScore = 100.0,
                    syllabusNotes = "Wave mechanics, quantum superposition, state vectors, qubits.",
                    attendanceThreshold = 75
                )
            )

            val eng205 = dao.insertCourse(
                Course(
                    semesterId = sem1Id,
                    name = "Technical Writing & Design",
                    code = "ENG 205",
                    instructor = "Prof. Clara Hughes",
                    room = "Hum Ctr 215",
                    credits = 3,
                    colorHex = "#EC4899", // Rose
                    targetGrade = "A",
                    currentScore = 92.0,
                    maxScore = 100.0,
                    syllabusNotes = "Engineering proposals, academic publishing, design critique presentations.",
                    attendanceThreshold = 75
                )
            )

            // Schedule Slots (Mon=1, Tue=2, Wed=3, Thu=4, Fri=5)
            // Monday
            dao.insertScheduleSlot(ScheduleSlot(courseId = cs301, dayOfWeek = 1, startTime = "09:00", endTime = "10:20", room = "Gates Hall 104", slotType = "Lecture"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = math240, dayOfWeek = 1, startTime = "11:00", endTime = "12:20", room = "Science Ctr 302", slotType = "Lecture"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = cs350, dayOfWeek = 1, startTime = "14:00", endTime = "15:30", room = "Turing Lab 201", slotType = "Lab"))

            // Tuesday
            dao.insertScheduleSlot(ScheduleSlot(courseId = phys210, dayOfWeek = 2, startTime = "10:00", endTime = "11:30", room = "Hansen Lab 110", slotType = "Lecture"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = eng205, dayOfWeek = 2, startTime = "13:00", endTime = "14:30", room = "Hum Ctr 215", slotType = "Seminar"))

            // Wednesday
            dao.insertScheduleSlot(ScheduleSlot(courseId = cs301, dayOfWeek = 3, startTime = "09:00", endTime = "10:20", room = "Gates Hall 104", slotType = "Lecture"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = math240, dayOfWeek = 3, startTime = "11:00", endTime = "12:20", room = "Science Ctr 302", slotType = "Tutorial"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = cs350, dayOfWeek = 3, startTime = "14:00", endTime = "15:30", room = "Turing Lab 201", slotType = "Lecture"))

            // Thursday
            dao.insertScheduleSlot(ScheduleSlot(courseId = phys210, dayOfWeek = 4, startTime = "10:00", endTime = "11:30", room = "Hansen Lab 110", slotType = "Lab"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = eng205, dayOfWeek = 4, startTime = "13:00", endTime = "14:30", room = "Hum Ctr 215", slotType = "Seminar"))

            // Friday
            dao.insertScheduleSlot(ScheduleSlot(courseId = cs301, dayOfWeek = 5, startTime = "10:00", endTime = "11:30", room = "Gates Hall 104", slotType = "Lab/Discussion"))
            dao.insertScheduleSlot(ScheduleSlot(courseId = math240, dayOfWeek = 5, startTime = "13:00", endTime = "14:00", room = "Science Ctr 302", slotType = "Office Hours"))

            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            // Assignments
            dao.insertAssignment(
                Assignment(
                    courseId = cs301,
                    title = "Dynamic Programming Problem Set 3",
                    description = "Implement Knapsack & Longest Common Subsequence in O(n*k) with memory optimization.",
                    dueDateMillis = now + (dayMillis * 2) + 7200000L,
                    priority = "HIGH",
                    status = "IN_PROGRESS",
                    maxScore = 100.0,
                    subtasks = "Define recurrence relations\nWrite memoized Kotlin/Python solution\nProve asymptotic time complexity\nBenchmark with test dataset"
                )
            )

            dao.insertAssignment(
                Assignment(
                    courseId = cs350,
                    title = "Raft Consensus Cluster Benchmark",
                    description = "Deploy a 3-node simulated cluster and test leader election under network partition.",
                    dueDateMillis = now + (dayMillis * 4) + 18000000L,
                    priority = "HIGH",
                    status = "PENDING",
                    maxScore = 100.0,
                    subtasks = "Implement Heartbeat RPC\nSimulate network delay\nCollect metrics on recovery time"
                )
            )

            dao.insertAssignment(
                Assignment(
                    courseId = math240,
                    title = "SVD Image Compression Report",
                    description = "Analyze rank-k approximations on high-resolution image matrices and calculate Frobenius norm error.",
                    dueDateMillis = now + (dayMillis * 6),
                    priority = "MEDIUM",
                    status = "PENDING",
                    maxScore = 50.0,
                    subtasks = "Compute eigenvalues and singular vectors\nPlot rank vs reconstruction error"
                )
            )

            dao.insertAssignment(
                Assignment(
                    courseId = eng205,
                    title = "Technical Specification Whitepaper Draft",
                    description = "Write a 5-page executive summary and architecture breakdown for the cloud storage protocol.",
                    dueDateMillis = now + (dayMillis * 8),
                    priority = "LOW",
                    status = "PENDING",
                    maxScore = 100.0,
                    subtasks = "Draft Executive Summary\nDesign system block diagrams\nPeer review with study group"
                )
            )

            dao.insertAssignment(
                Assignment(
                    courseId = phys210,
                    title = "Wave Interference Lab Report",
                    description = "Double slit laser wavelength precision measurements and statistical error propagation.",
                    dueDateMillis = now - (dayMillis * 2),
                    priority = "HIGH",
                    status = "GRADED",
                    scoreObtained = 94.0,
                    maxScore = 100.0,
                    subtasks = "Plot fringe intensity\nCalculate Chi-squared fit"
                )
            )

            // Exams
            dao.insertExam(
                Exam(
                    courseId = cs301,
                    title = "Midterm Examination",
                    examDateMillis = now + (dayMillis * 5) + 3600000L * 14,
                    durationMinutes = 90,
                    room = "Auditorium Hall B",
                    weightPercentage = 25.0,
                    targetScore = 92.0,
                    topics = "Divide & Conquer, Greedy Scheduling, Max Flow / Min Cut, DP Recurrences"
                )
            )

            dao.insertExam(
                Exam(
                    courseId = math240,
                    title = "Linear Systems & Matrix Test",
                    examDateMillis = now + (dayMillis * 11) + 3600000L * 10,
                    durationMinutes = 75,
                    room = "Science Ctr 302",
                    weightPercentage = 20.0,
                    targetScore = 90.0,
                    topics = "Vector Subspaces, Orthogonality, Gram-Schmidt, QR Decomposition"
                )
            )

            dao.insertExam(
                Exam(
                    courseId = phys210,
                    title = "Physics Mid-Semester Assessment",
                    examDateMillis = now + (dayMillis * 18),
                    durationMinutes = 120,
                    room = "Hansen Lab 110",
                    weightPercentage = 30.0,
                    targetScore = 88.0,
                    topics = "Electromagnetic Waves, Polarization, Quantum Wavepackets, Uncertainty Principle"
                )
            )

            // Attendance Logs (simulating high attendance)
            for (i in 1..18) {
                val pastDate = now - (i * dayMillis)
                dao.insertAttendanceLog(AttendanceLog(courseId = cs301, dateMillis = pastDate, status = "PRESENT"))
                dao.insertAttendanceLog(AttendanceLog(courseId = math240, dateMillis = pastDate, status = if (i == 4) "ABSENT" else "PRESENT"))
                dao.insertAttendanceLog(AttendanceLog(courseId = cs350, dateMillis = pastDate, status = "PRESENT"))
                dao.insertAttendanceLog(AttendanceLog(courseId = phys210, dateMillis = pastDate, status = if (i == 7 || i == 14) "ABSENT" else "PRESENT"))
                dao.insertAttendanceLog(AttendanceLog(courseId = eng205, dateMillis = pastDate, status = "PRESENT"))
            }

            // Academic Notes
            dao.insertNote(
                AcademicNote(
                    courseId = cs301,
                    title = "Bellman-Ford & Negative Cycle Detection",
                    content = "Key idea: Relax all |V|-1 edges. If an edge can still be relaxed in the |V|-th iteration, a negative weight cycle reachable from source exists. Used in distance-vector routing.",
                    tags = "Algorithms, Graph Theory, Routing",
                    isPinned = true
                )
            )

            dao.insertNote(
                AcademicNote(
                    courseId = cs350,
                    title = "CAP Theorem & PACELC Breakdown",
                    content = "PACELC extension: In case of Partition (P), choose Availability (A) or Consistency (C); Else (E), choose Latency (L) or Consistency (C). DynamoDB defaults to PA/EL.",
                    tags = "Distributed Systems, Database, Cloud",
                    isPinned = true
                )
            )

            dao.insertNote(
                AcademicNote(
                    courseId = math240,
                    title = "Singular Value Decomposition (SVD) Intuition",
                    content = "A = U * Sigma * V^T. Sigma holds singular values ordered descending. Useful for principal component analysis (PCA), compression, and pseudo-inverses.",
                    tags = "Math, Matrices, ML",
                    isPinned = false
                )
            )
        }
    }
}
