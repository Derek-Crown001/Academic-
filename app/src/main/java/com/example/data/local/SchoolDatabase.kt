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
        SchoolUser::class,
        SchoolClass::class,
        SchoolSubject::class,
        CbtExam::class,
        CbtQuestion::class,
        CbtSubmission::class,
        SchoolAssignment::class,
        AssignmentSubmission::class,
        StudentGrade::class,
        ReportCard::class,
        SchoolAnnouncement::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "secondary_school_portal_db"
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
                    populateInitialData(database.schoolDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: SchoolDao) {
            // 1. Initial Users (Admin, Teachers, Students, Parents)
            val users = listOf(
                SchoolUser(
                    id = "ADM-001",
                    name = "Dr. C. Adebayo (Principal)",
                    role = SchoolRole.ADMIN,
                    email = "principal@academiatrack.sch.ng",
                    phone = "+234 802 334 5678",
                    passcode = "admin123",
                    avatarColorHex = "#1E3A8A"
                ),
                SchoolUser(
                    id = "TCH-101",
                    name = "Mr. David Okon",
                    role = SchoolRole.TEACHER,
                    email = "d.okon@academiatrack.sch.ng",
                    phone = "+234 803 112 3344",
                    passcode = "teach123",
                    className = "SS 2 Gold",
                    assignedSubjects = "Mathematics, Physics",
                    avatarColorHex = "#2563EB"
                ),
                SchoolUser(
                    id = "TCH-102",
                    name = "Mrs. Amina Bello",
                    role = SchoolRole.TEACHER,
                    email = "a.bello@academiatrack.sch.ng",
                    phone = "+234 805 554 4433",
                    passcode = "teach123",
                    className = "SS 3 Science",
                    assignedSubjects = "English Language, Literature",
                    avatarColorHex = "#7C3AED"
                ),
                SchoolUser(
                    id = "TCH-103",
                    name = "Dr. Emeka Obi",
                    role = SchoolRole.TEACHER,
                    email = "e.obi@academiatrack.sch.ng",
                    phone = "+234 807 889 9001",
                    passcode = "teach123",
                    className = "JSS 3 Emerald",
                    assignedSubjects = "Chemistry, Biology",
                    avatarColorHex = "#0D9488"
                ),
                SchoolUser(
                    id = "STU-2025-042",
                    name = "Chidinma Nwosu",
                    role = SchoolRole.STUDENT,
                    email = "chidinma.nwosu@student.sch.ng",
                    phone = "+234 810 998 7766",
                    passcode = "1234",
                    className = "SS 2 Gold",
                    avatarColorHex = "#059669"
                ),
                SchoolUser(
                    id = "STU-2025-043",
                    name = "Tunde Bakare",
                    role = SchoolRole.STUDENT,
                    email = "tunde.b@student.sch.ng",
                    phone = "+234 811 223 4455",
                    passcode = "1234",
                    className = "SS 2 Gold",
                    avatarColorHex = "#D97706"
                ),
                SchoolUser(
                    id = "STU-2025-044",
                    name = "Fatima Mohammed",
                    role = SchoolRole.STUDENT,
                    email = "fatima.m@student.sch.ng",
                    phone = "+234 812 334 5566",
                    passcode = "1234",
                    className = "SS 2 Gold",
                    avatarColorHex = "#E11D48"
                ),
                SchoolUser(
                    id = "PAR-2025-042",
                    name = "Mrs. Ngozi Nwosu",
                    role = SchoolRole.PARENT,
                    email = "ngozi.nwosu@parent.com",
                    phone = "+234 803 777 8899",
                    passcode = "1234",
                    className = "SS 2 Gold",
                    studentChildId = "STU-2025-042",
                    studentChildName = "Chidinma Nwosu",
                    avatarColorHex = "#9333EA"
                )
            )
            dao.insertUsers(users)

            // 2. School Classes
            val classes = listOf(
                SchoolClass(id = 1, name = "SS 2 Gold", level = "SS 2", arm = "Gold", classTeacherId = "TCH-101", classTeacherName = "Mr. David Okon", studentCount = 32, room = "Block B, Rm 4"),
                SchoolClass(id = 2, name = "SS 3 Science", level = "SS 3", arm = "Science", classTeacherId = "TCH-102", classTeacherName = "Mrs. Amina Bello", studentCount = 28, room = "Block C, Rm 1"),
                SchoolClass(id = 3, name = "JSS 3 Emerald", level = "JSS 3", arm = "Emerald", classTeacherId = "TCH-103", classTeacherName = "Dr. Emeka Obi", studentCount = 35, room = "Block A, Rm 2"),
                SchoolClass(id = 4, name = "SS 1 Diamond", level = "SS 1", arm = "Diamond", classTeacherId = "TCH-101", classTeacherName = "Mr. David Okon", studentCount = 30, room = "Block B, Rm 1")
            )
            dao.insertClasses(classes)

            // 3. Subjects
            val subjects = listOf(
                SchoolSubject(id = 1, name = "Mathematics", code = "MTH 201", classLevel = "SS 2", teacherId = "TCH-101", teacherName = "Mr. David Okon", colorHex = "#2563EB", periodsPerWeek = 5),
                SchoolSubject(id = 2, name = "English Language", code = "ENG 201", classLevel = "SS 2", teacherId = "TCH-102", teacherName = "Mrs. Amina Bello", colorHex = "#7C3AED", periodsPerWeek = 5),
                SchoolSubject(id = 3, name = "Physics", code = "PHY 201", classLevel = "SS 2", teacherId = "TCH-101", teacherName = "Mr. David Okon", colorHex = "#0284C7", periodsPerWeek = 4),
                SchoolSubject(id = 4, name = "Chemistry", code = "CHM 201", classLevel = "SS 2", teacherId = "TCH-103", teacherName = "Dr. Emeka Obi", colorHex = "#059669", periodsPerWeek = 4),
                SchoolSubject(id = 5, name = "Biology", code = "BIO 201", classLevel = "SS 2", teacherId = "TCH-103", teacherName = "Dr. Emeka Obi", colorHex = "#10B981", periodsPerWeek = 4),
                SchoolSubject(id = 6, name = "Economics", code = "ECN 201", classLevel = "SS 2", teacherId = "TCH-102", teacherName = "Mrs. Amina Bello", colorHex = "#D97706", periodsPerWeek = 3),
                SchoolSubject(id = 7, name = "Civic Education", code = "CVE 201", classLevel = "SS 2", teacherId = "TCH-102", teacherName = "Mrs. Amina Bello", colorHex = "#E11D48", periodsPerWeek = 2),
                SchoolSubject(id = 8, name = "Computer Studies", code = "CMP 201", classLevel = "SS 2", teacherId = "TCH-101", teacherName = "Mr. David Okon", colorHex = "#4F46E5", periodsPerWeek = 3)
            )
            dao.insertSubjects(subjects)

            // 4. CBT Exams & Tests
            val exam1Id = dao.insertCbtExam(
                CbtExam(
                    title = "SS2 Mathematics First Term CBT Examination",
                    subjectName = "Mathematics",
                    className = "SS 2 Gold",
                    teacherId = "TCH-101",
                    teacherName = "Mr. David Okon",
                    examType = "EXAM",
                    durationMinutes = 25,
                    totalMarks = 50,
                    passMark = 25,
                    isPublished = true,
                    instructions = "Choose the most appropriate option for each question. Calculators are permitted. Submit before the timer runs out."
                )
            )

            val exam2Id = dao.insertCbtExam(
                CbtExam(
                    title = "Physics Mid-Term CBT Test (Mechanics & Waves)",
                    subjectName = "Physics",
                    className = "SS 2 Gold",
                    teacherId = "TCH-101",
                    teacherName = "Mr. David Okon",
                    examType = "TEST",
                    durationMinutes = 20,
                    totalMarks = 30,
                    passMark = 15,
                    isPublished = true,
                    instructions = "Answer all 6 objective questions. Assume g = 9.8 m/s² unless otherwise specified."
                )
            )

            val exam3Id = dao.insertCbtExam(
                CbtExam(
                    title = "English Language Lexis & Structure CBT Assessment",
                    subjectName = "English Language",
                    className = "SS 2 Gold",
                    teacherId = "TCH-102",
                    teacherName = "Mrs. Amina Bello",
                    examType = "TEST",
                    durationMinutes = 15,
                    totalMarks = 25,
                    passMark = 12,
                    isPublished = true,
                    instructions = "Carefully read each sentence and select the option nearest in meaning or grammatically correct."
                )
            )

            // 5. CBT Questions for Exam 1 (Mathematics)
            val mathQuestions = listOf(
                CbtQuestion(
                    examId = exam1Id,
                    questionNumber = 1,
                    questionText = "Solve the quadratic equation: 2x² - 5x - 3 = 0. Find the values of x.",
                    optionA = "x = 3 or x = -1/2",
                    optionB = "x = -3 or x = 1/2",
                    optionC = "x = 2 or x = -3/2",
                    optionD = "x = 1 or x = -3",
                    correctOption = "A",
                    explanation = "Factorization: (2x + 1)(x - 3) = 0 => x = 3 or x = -1/2.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam1Id,
                    questionNumber = 2,
                    questionText = "If log₁₀(2) = 0.3010 and log₁₀(3) = 0.4771, evaluate log₁₀(18).",
                    optionA = "0.7781",
                    optionB = "1.2552",
                    optionC = "1.0791",
                    optionD = "0.9542",
                    correctOption = "B",
                    explanation = "log(18) = log(2 * 3²) = log(2) + 2*log(3) = 0.3010 + 2*(0.4771) = 1.2552.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam1Id,
                    questionNumber = 3,
                    questionText = "Find the 10th term of the Arithmetic Progression (A.P.): 5, 9, 13, 17, ...",
                    optionA = "37",
                    optionB = "41",
                    optionC = "45",
                    optionD = "49",
                    correctOption = "B",
                    explanation = "T_n = a + (n - 1)d = 5 + (10 - 1)*4 = 5 + 36 = 41.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam1Id,
                    questionNumber = 4,
                    questionText = "A circle has a radius of 14 cm. Calculate its circumference (Take π = 22/7).",
                    optionA = "44 cm",
                    optionB = "88 cm",
                    optionC = "154 cm",
                    optionD = "616 cm",
                    correctOption = "B",
                    explanation = "Circumference = 2 * π * r = 2 * (22/7) * 14 = 88 cm.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam1Id,
                    questionNumber = 5,
                    questionText = "If sin(θ) = 3/5 for an acute angle θ, what is the value of cos(θ) + tan(θ)?",
                    optionA = "31/20",
                    optionB = "7/5",
                    optionC = "29/20",
                    optionD = "4/5",
                    correctOption = "A",
                    explanation = "cos(θ) = 4/5, tan(θ) = 3/4. Sum = 4/5 + 3/4 = 16/20 + 15/20 = 31/20.",
                    marks = 10
                )
            )
            dao.insertCbtQuestions(mathQuestions)

            // Questions for Exam 2 (Physics)
            val physicsQuestions = listOf(
                CbtQuestion(
                    examId = exam2Id,
                    questionNumber = 1,
                    questionText = "A body starts from rest and accelerates uniformly at 2.5 m/s² for 8 seconds. Calculate the velocity acquired.",
                    optionA = "10 m/s",
                    optionB = "15 m/s",
                    optionC = "20 m/s",
                    optionD = "25 m/s",
                    correctOption = "C",
                    explanation = "v = u + at = 0 + 2.5 * 8 = 20 m/s.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam2Id,
                    questionNumber = 2,
                    questionText = "Which of the following is a scalar quantity?",
                    optionA = "Electric Field Intensity",
                    optionB = "Magnetic Flux Density",
                    optionC = "Electric Potential",
                    optionD = "Angular Velocity",
                    correctOption = "C",
                    explanation = "Electric Potential has magnitude only and is a scalar quantity.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam2Id,
                    questionNumber = 3,
                    questionText = "Calculate the work done when a force of 50 N moves a crate through a displacement of 4 meters in the direction of the force.",
                    optionA = "200 Joules",
                    optionB = "12.5 Joules",
                    optionC = "54 Joules",
                    optionD = "100 Joules",
                    correctOption = "A",
                    explanation = "Work = Force * Distance = 50 * 4 = 200 J.",
                    marks = 10
                )
            )
            dao.insertCbtQuestions(physicsQuestions)

            // Questions for Exam 3 (English)
            val englishQuestions = listOf(
                CbtQuestion(
                    examId = exam3Id,
                    questionNumber = 1,
                    questionText = "Choose the word nearest in meaning to 'METICULOUS':",
                    optionA = "Careless",
                    optionB = "Painstaking and precise",
                    optionC = "Hastily performed",
                    optionD = "Aggressive",
                    correctOption = "B",
                    explanation = "'Meticulous' means showing great attention to detail; very careful and precise.",
                    marks = 10
                ),
                CbtQuestion(
                    examId = exam3Id,
                    questionNumber = 2,
                    questionText = "Neither the class teacher nor the students ______ present at the assembly yesterday.",
                    optionA = "was",
                    optionB = "is",
                    optionC = "were",
                    optionD = "are",
                    correctOption = "C",
                    explanation = "With 'neither... nor', the verb agrees with the closer subject ('the students' -> plural past 'were').",
                    marks = 15
                )
            )
            dao.insertCbtQuestions(englishQuestions)

            // 6. Student Grades for SS2 Gold (Chidinma Nwosu, Tunde Bakare)
            val gradesChidinma = listOf(
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Mathematics", test1Score = 14.0, test2Score = 14.5, midtermScore = 19.0, examScore = 46.5, totalScore = 94.0, gradeLetter = "A1", remarks = "Excellent"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "English Language", test1Score = 13.0, test2Score = 13.5, midtermScore = 18.0, examScore = 43.5, totalScore = 88.0, gradeLetter = "A1", remarks = "Distinction"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Physics", test1Score = 13.5, test2Score = 14.0, midtermScore = 18.5, examScore = 45.0, totalScore = 91.0, gradeLetter = "A1", remarks = "Outstanding"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Chemistry", test1Score = 12.5, test2Score = 13.0, midtermScore = 17.5, examScore = 42.0, totalScore = 85.0, gradeLetter = "A1", remarks = "Very Good"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Biology", test1Score = 12.0, test2Score = 13.0, midtermScore = 17.0, examScore = 40.0, totalScore = 82.0, gradeLetter = "A1", remarks = "Very Good"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Economics", test1Score = 11.5, test2Score = 12.0, midtermScore = 16.5, examScore = 38.0, totalScore = 78.0, gradeLetter = "A1", remarks = "Good Credit"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Civic Education", test1Score = 14.0, test2Score = 14.0, midtermScore = 19.0, examScore = 45.0, totalScore = 92.0, gradeLetter = "A1", remarks = "Excellent"),
                StudentGrade(studentId = "STU-2025-042", studentName = "Chidinma Nwosu", admissionNo = "ADM-SS2-042", className = "SS 2 Gold", subjectName = "Computer Studies", test1Score = 15.0, test2Score = 14.5, midtermScore = 19.5, examScore = 48.0, totalScore = 97.0, gradeLetter = "A1", remarks = "Brilliant")
            )
            dao.insertGrades(gradesChidinma)

            val gradesTunde = listOf(
                StudentGrade(studentId = "STU-2025-043", studentName = "Tunde Bakare", admissionNo = "ADM-SS2-043", className = "SS 2 Gold", subjectName = "Mathematics", test1Score = 11.0, test2Score = 12.0, midtermScore = 15.0, examScore = 37.0, totalScore = 75.0, gradeLetter = "B2", remarks = "Very Good"),
                StudentGrade(studentId = "STU-2025-043", studentName = "Tunde Bakare", admissionNo = "ADM-SS2-043", className = "SS 2 Gold", subjectName = "English Language", test1Score = 10.5, test2Score = 11.0, midtermScore = 14.0, examScore = 34.5, totalScore = 70.0, gradeLetter = "B2", remarks = "Good Credit"),
                StudentGrade(studentId = "STU-2025-043", studentName = "Tunde Bakare", admissionNo = "ADM-SS2-043", className = "SS 2 Gold", subjectName = "Physics", test1Score = 11.0, test2Score = 12.5, midtermScore = 16.0, examScore = 38.5, totalScore = 78.0, gradeLetter = "A1", remarks = "Very Good"),
                StudentGrade(studentId = "STU-2025-043", studentName = "Tunde Bakare", admissionNo = "ADM-SS2-043", className = "SS 2 Gold", subjectName = "Chemistry", test1Score = 10.0, test2Score = 11.0, midtermScore = 14.0, examScore = 35.0, totalScore = 70.0, gradeLetter = "B2", remarks = "Credit")
            )
            dao.insertGrades(gradesTunde)

            // 7. Report Cards
            val reportCardChidinma = ReportCard(
                studentId = "STU-2025-042",
                studentName = "Chidinma Nwosu",
                admissionNo = "ADM-SS2-042",
                className = "SS 2 Gold",
                term = "1st Term",
                session = "2025/2026",
                totalScore = 707.0,
                maxPossibleScore = 800.0,
                averageScore = 88.38,
                classPosition = "1st out of 32",
                attendancePresent = 64,
                attendanceTotal = 65,
                classTeacherRemark = "Chidinma is an exceptionally hardworking and polite student who consistently demonstrates leadership and academic excellence.",
                principalRemark = "A phenomenal terminal result. Awarded the Principal's First Class Academic Honors. Keep shining!",
                isApprovedByAdmin = true
            )
            val reportCardTunde = ReportCard(
                studentId = "STU-2025-043",
                studentName = "Tunde Bakare",
                admissionNo = "ADM-SS2-043",
                className = "SS 2 Gold",
                term = "1st Term",
                session = "2025/2026",
                totalScore = 585.0,
                maxPossibleScore = 800.0,
                averageScore = 73.13,
                classPosition = "4th out of 32",
                attendancePresent = 60,
                attendanceTotal = 65,
                classTeacherRemark = "Tunde has good analytical skills. He should focus more on continuous reading in English and Chemistry.",
                principalRemark = "A commendable result with room for even higher laurels. Well done.",
                isApprovedByAdmin = true
            )
            dao.insertReportCards(listOf(reportCardChidinma, reportCardTunde))

            // 8. Assignments
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val assignments = listOf(
                SchoolAssignment(
                    title = "SS2 Mathematics: Circle Geometry Proofs",
                    subjectName = "Mathematics",
                    className = "SS 2 Gold",
                    teacherName = "Mr. David Okon",
                    dueDateMillis = now + (dayMillis * 3),
                    maxMarks = 20,
                    description = "Prove Theorem 3: 'The angle subtended by an arc at the center of a circle is twice that subtended at any point on the remaining circumference'.",
                    instructions = "Write out full mathematical steps, draw labeled diagrams, and explain your logical deductions clearly."
                ),
                SchoolAssignment(
                    title = "Physics Lab Investigation: Simple Harmonic Motion (SHM)",
                    subjectName = "Physics",
                    className = "SS 2 Gold",
                    teacherName = "Mr. David Okon",
                    dueDateMillis = now + (dayMillis * 5),
                    maxMarks = 20,
                    description = "Analyze the relationship between the period of a simple pendulum T and length L. Plot T² against L.",
                    instructions = "Submit your tabular data with 5 length variations and calculate the experimental value of g."
                ),
                SchoolAssignment(
                    title = "English Essay: The Impact of Artificial Intelligence in Education",
                    subjectName = "English Language",
                    className = "SS 2 Gold",
                    teacherName = "Mrs. Amina Bello",
                    dueDateMillis = now + (dayMillis * 7),
                    maxMarks = 20,
                    description = "Write an expository essay of not less than 350 words discussing both the opportunities and ethical challenges of AI in secondary schools.",
                    instructions = "Adhere to proper paragraph structure, vocabulary variety, and correct punctuation."
                )
            )
            assignments.forEach { dao.insertAssignment(it) }

            // 9. Targeted Announcements
            val announcements = listOf(
                SchoolAnnouncement(
                    title = "1st Term Terminal CBT Examination Timetable Released",
                    content = "All students and teachers should take note that the Computer-Based Terminal Examinations will commence on Monday next week. Please review your CBT access codes.",
                    targetAudience = "ALL",
                    senderName = "Dr. C. Adebayo (Principal)",
                    senderRole = "ADMIN",
                    category = "EXAM",
                    isUrgent = true,
                    postedAtMillis = now - (dayMillis * 1)
                ),
                SchoolAnnouncement(
                    title = "Notice of Termly Parents & Teachers Association (PTA) General Meeting",
                    content = "Dear Parents and Guardians, you are cordially invited to our 1st Term PTA General Meeting on Saturday at 10:00 AM in the School Main Auditorium. Terminal report cards will be reviewed.",
                    targetAudience = "PARENT",
                    senderName = "School Administration",
                    senderRole = "ADMIN",
                    category = "EVENT",
                    isUrgent = false,
                    postedAtMillis = now - (dayMillis * 2)
                ),
                SchoolAnnouncement(
                    title = "Deadline for Continuous Assessment (CA) Score Submission",
                    content = "All Subject Teachers and Class Teachers are reminded to submit their Test 1, Test 2, and Midterm CA scores to the Admin Portal by 4:00 PM this Friday for report card compilation.",
                    targetAudience = "TEACHER",
                    senderName = "Academic Vice Principal",
                    senderRole = "ADMIN",
                    category = "ACADEMIC",
                    isUrgent = true,
                    postedAtMillis = now - (dayMillis * 3)
                ),
                SchoolAnnouncement(
                    title = "Inter-House Sports Competition Trials & Registration",
                    content = "Students interested in track and field events (100m, 200m, 4x100m relay, high jump) should report to the Sports Pavilion with their sports coordinators on Thursday at 3:30 PM.",
                    targetAudience = "STUDENT",
                    senderName = "Sports Director",
                    senderRole = "TEACHER",
                    category = "EVENT",
                    isUrgent = false,
                    postedAtMillis = now - (dayMillis * 4)
                )
            )
            announcements.forEach { dao.insertAnnouncement(it) }

            // 10. Chat Messages (Staff General Room & Class Rooms)
            val chatMessages = listOf(
                ChatMessage(
                    channelId = "STAFF_GENERAL",
                    channelName = "Staff General Room",
                    senderId = "ADM-001",
                    senderName = "Dr. C. Adebayo (Principal)",
                    senderRole = SchoolRole.ADMIN,
                    message = "Good morning esteemed colleagues. Please ensure all CBT test questions for SS1, SS2, and SS3 are uploaded by today.",
                    timestampMillis = now - 7200000L
                ),
                ChatMessage(
                    channelId = "STAFF_GENERAL",
                    channelName = "Staff General Room",
                    senderId = "TCH-101",
                    senderName = "Mr. David Okon",
                    senderRole = SchoolRole.TEACHER,
                    message = "Mathematics and Physics CBT questions for SS2 Gold have been uploaded and tested, Sir. Ready for moderation.",
                    timestampMillis = now - 5400000L
                ),
                ChatMessage(
                    channelId = "STAFF_GENERAL",
                    channelName = "Staff General Room",
                    senderId = "TCH-102",
                    senderName = "Mrs. Amina Bello",
                    senderRole = SchoolRole.TEACHER,
                    message = "English and Literature CA grades are compiled as well. The student performance is very encouraging.",
                    timestampMillis = now - 3600000L
                ),
                ChatMessage(
                    channelId = "CLASS_SS2_GOLD",
                    channelName = "SS 2 Gold Class Room",
                    senderId = "TCH-101",
                    senderName = "Mr. David Okon (Class Teacher)",
                    senderRole = SchoolRole.TEACHER,
                    message = "Welcome class! Remember our Mathematics Circle Geometry assignment is due on Friday. Submit your answers via the portal.",
                    timestampMillis = now - 10800000L
                ),
                ChatMessage(
                    channelId = "CLASS_SS2_GOLD",
                    channelName = "SS 2 Gold Class Room",
                    senderId = "STU-2025-042",
                    senderName = "Chidinma Nwosu",
                    senderRole = SchoolRole.STUDENT,
                    message = "Thank you, Sir! Will the CBT exam include theorem proofs as well?",
                    timestampMillis = now - 9000000L
                ),
                ChatMessage(
                    channelId = "CLASS_SS2_GOLD",
                    channelName = "SS 2 Gold Class Room",
                    senderId = "TCH-101",
                    senderName = "Mr. David Okon (Class Teacher)",
                    senderRole = SchoolRole.TEACHER,
                    message = "The CBT exam will feature multiple-choice questions testing both computational problems and geometric deductions.",
                    timestampMillis = now - 7200000L
                ),
                ChatMessage(
                    channelId = "CLASS_SS2_GOLD",
                    channelName = "SS 2 Gold Class Room",
                    senderId = "ADM-001",
                    senderName = "Dr. C. Adebayo (Principal - Moderator)",
                    senderRole = SchoolRole.ADMIN,
                    message = "Friendly reminder to maintain respectful discourse in this class room. Wishing SS2 Gold the best in your assessments!",
                    timestampMillis = now - 1800000L
                )
            )
            chatMessages.forEach { dao.insertChatMessage(it) }
        }
    }
}
