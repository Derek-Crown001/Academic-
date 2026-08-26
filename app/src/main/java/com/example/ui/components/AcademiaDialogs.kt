package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Course
import com.example.data.model.Semester
import com.example.data.model.UserProfile
import com.example.data.repository.AcademiaRepository
import com.example.data.repository.CourseAttendanceStats
import com.example.ui.theme.*

val courseColorPalette = listOf(
    "#2563EB", // Royal Blue
    "#7C3AED", // Violet
    "#0D9488", // Teal
    "#F59E0B", // Amber
    "#EC4899", // Rose
    "#10B981", // Emerald
    "#6366F1", // Indigo
    "#0EA5E9"  // Sky
)

// 1. Add Course Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    semesters: List<Semester>,
    onDismiss: () -> Unit,
    onConfirm: (
        semesterId: Long,
        name: String,
        code: String,
        instructor: String,
        room: String,
        credits: Int,
        colorHex: String,
        targetGrade: String,
        currentScore: Double,
        syllabusNotes: String,
        attendanceThreshold: Int
    ) -> Unit
) {
    var selectedSemesterId by remember { mutableStateOf(semesters.find { it.isCurrent }?.id ?: semesters.firstOrNull()?.id ?: 1L) }
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var credits by remember { mutableIntStateOf(3) }
    var selectedColor by remember { mutableStateOf(courseColorPalette.first()) }
    var targetGrade by remember { mutableStateOf("A") }
    var currentScore by remember { mutableStateOf("90") }
    var syllabusNotes by remember { mutableStateOf("") }
    var attendanceThreshold by remember { mutableIntStateOf(75) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add New Course",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Course Code & Name
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code (e.g. CS 301)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name (e.g. Data Structures)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room/Venue") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Credits & Current Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Credits: $credits",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = credits.toFloat(),
                            onValueChange = { credits = it.toInt() },
                            valueRange = 1f..6f,
                            steps = 4
                        )
                    }

                    OutlinedTextField(
                        value = currentScore,
                        onValueChange = { currentScore = it },
                        label = { Text("Current Score %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Color Selection
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(courseColorPalette) { colorHex ->
                        val color = parseHexColor(colorHex)
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = syllabusNotes,
                    onValueChange = { syllabusNotes = it },
                    label = { Text("Syllabus / Key Topics") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (code.isNotBlank() && name.isNotBlank()) {
                                onConfirm(
                                    selectedSemesterId,
                                    name,
                                    code,
                                    instructor,
                                    room,
                                    credits,
                                    selectedColor,
                                    targetGrade,
                                    currentScore.toDoubleOrNull() ?: 90.0,
                                    syllabusNotes,
                                    attendanceThreshold
                                )
                                onDismiss()
                            }
                        },
                        enabled = code.isNotBlank() && name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Text("Add Course", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. Course Detail Bottom Sheet / Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailDialog(
    course: Course,
    attendanceStats: CourseAttendanceStats?,
    onDismiss: () -> Unit,
    onLogAttendance: (Long, String) -> Unit,
    onDeleteCourse: (Course) -> Unit
) {
    val color = parseHexColor(course.colorHex)
    val stats = attendanceStats
    val isSafe = stats?.isSafe ?: true

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = course.code,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Info Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${course.credits} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (course.room.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Room: ${course.room}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (course.instructor.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = course.instructor,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Grade & Score Status
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT STANDING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Text(
                                text = "${course.currentScore.toInt()}% • Letter ${AcademiaRepository.scoreToLetter(course.currentScore)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Text(
                                text = "Grade ${course.targetGrade}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Attendance Section & Bunk Analyzer
                Text(
                    text = "Attendance & Bunk Analysis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSafe) AcademicEmerald.copy(alpha = 0.1f) else AcademicRose.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Attendance: ${stats?.attendancePercentage?.toInt() ?: 100}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSafe) AcademicEmerald else AcademicRose
                            )
                            Text(
                                text = "${stats?.attendedClasses ?: 18}/${stats?.totalClasses ?: 18} Classes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSafe) "Safe! You can safely miss ${stats?.bunksAvailable ?: 3} more classes without falling below ${course.attendanceThreshold}%." else "Warning! You must attend ${stats?.recoveryNeeded ?: 2} consecutive classes to reach ${course.attendanceThreshold}%.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSafe) AcademicEmerald else AcademicRose
                        )
                    }
                }

                // Quick Attendance Log buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onLogAttendance(course.id, "PRESENT") },
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Log Present", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onLogAttendance(course.id, "ABSENT") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Log Absent", color = AcademicRose)
                    }
                }

                // Syllabus Notes
                if (course.syllabusNotes.isNotBlank()) {
                    Text(
                        text = "Syllabus & Core Concepts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = course.syllabusNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Delete Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDeleteCourse(course)
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = AcademicRose, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Course", color = AcademicRose)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. Add Assignment Dialog
@Composable
fun AddAssignmentDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (
        courseId: Long,
        title: String,
        description: String,
        dueDateMillis: Long,
        priority: String,
        maxScore: Double,
        subtasks: String
    ) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: 1L) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDaysFromNow by remember { mutableIntStateOf(3) }
    var priority by remember { mutableStateOf("MEDIUM") }
    var maxScore by remember { mutableStateOf("100") }
    var subtasks by remember { mutableStateOf("") }

    val priorities = listOf("HIGH" to "High 🔥", "MEDIUM" to "Medium ⚡", "LOW" to "Low 🌿")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "New Assignment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Course selector
                Text("Select Course", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(courses) { c ->
                        val isSelected = c.id == selectedCourseId
                        val color = parseHexColor(c.colorHex)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCourseId = c.id }
                        ) {
                            Text(
                                text = c.code,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Raft Consensus Lab)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Requirements") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Due in Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Due in $dueDaysFromNow days",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = dueDaysFromNow.toFloat(),
                            onValueChange = { dueDaysFromNow = it.toInt() },
                            valueRange = 1f..14f,
                            steps = 12
                        )
                    }

                    OutlinedTextField(
                        value = maxScore,
                        onValueChange = { maxScore = it },
                        label = { Text("Max Points") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Priority
                Text("Priority Level", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { (key, label) ->
                        val isSelected = priority == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { priority = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = subtasks,
                    onValueChange = { subtasks = it },
                    label = { Text("Subtasks (one item per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val dueMillis = System.currentTimeMillis() + (dueDaysFromNow * 86400000L)
                                onConfirm(
                                    selectedCourseId,
                                    title,
                                    description,
                                    dueMillis,
                                    priority,
                                    maxScore.toDoubleOrNull() ?: 100.0,
                                    subtasks
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Text("Create Assignment", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 4. Add Exam Dialog
@Composable
fun AddExamDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (
        courseId: Long,
        title: String,
        examDateMillis: Long,
        durationMinutes: Int,
        room: String,
        weightPercentage: Double,
        targetScore: Double,
        topics: String
    ) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: 1L) }
    var title by remember { mutableStateOf("") }
    var daysFromNow by remember { mutableIntStateOf(5) }
    var durationMinutes by remember { mutableIntStateOf(90) }
    var room by remember { mutableStateOf("") }
    var weightPercentage by remember { mutableStateOf("25") }
    var targetScore by remember { mutableStateOf("90") }
    var topics by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Upcoming Exam",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text("Course", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(courses) { c ->
                        val isSelected = c.id == selectedCourseId
                        val color = parseHexColor(c.colorHex)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCourseId = c.id }
                        ) {
                            Text(
                                text = c.code,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Exam Title (e.g. Midterm Test 1)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("In $daysFromNow Days", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Slider(
                            value = daysFromNow.toFloat(),
                            onValueChange = { daysFromNow = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28
                        )
                    }

                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room / Hall") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightPercentage,
                        onValueChange = { weightPercentage = it },
                        label = { Text("Weight %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = targetScore,
                        onValueChange = { targetScore = it },
                        label = { Text("Target %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("Exam Syllabus / Covered Chapters") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val examDate = System.currentTimeMillis() + (daysFromNow * 86400000L)
                                onConfirm(
                                    selectedCourseId,
                                    title,
                                    examDate,
                                    durationMinutes,
                                    room,
                                    weightPercentage.toDoubleOrNull() ?: 25.0,
                                    targetScore.toDoubleOrNull() ?: 90.0,
                                    topics
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Text("Add Exam", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 5. Add Schedule Slot Dialog
@Composable
fun AddScheduleSlotDialog(
    courses: List<Course>,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (courseId: Long, dayOfWeek: Int, startTime: String, endTime: String, room: String, slotType: String) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: 1L) }
    var selectedDay by remember { mutableIntStateOf(defaultDay) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var room by remember { mutableStateOf("") }
    var slotType by remember { mutableStateOf("Lecture") }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val types = listOf("Lecture", "Lab", "Seminar", "Tutorial", "Discussion")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Class Time Slot",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text("Course", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(courses) { c ->
                        val isSelected = c.id == selectedCourseId
                        val color = parseHexColor(c.colorHex)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCourseId = c.id }
                        ) {
                            Text(
                                text = c.code,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text("Day of Week", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEachIndexed { idx, d ->
                        val dayNum = idx + 1
                        val isSelected = selectedDay == dayNum
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedDay = dayNum }
                        ) {
                            Text(
                                text = d,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Location (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Session Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(types) { t ->
                        val isSelected = slotType == t
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { slotType = t }
                        ) {
                            Text(
                                text = t,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(selectedCourseId, selectedDay, startTime, endTime, room, slotType)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Text("Add to Timetable", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 6. User Profile Dialog
@Composable
fun ProfileDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (name: String, studentId: String, major: String, university: String, targetCgpa: Double) -> Unit
) {
    var name by remember { mutableStateOf(userProfile?.name ?: "Alex Rivera") }
    var studentId by remember { mutableStateOf(userProfile?.studentId ?: "CS-2026-8942") }
    var major by remember { mutableStateOf(userProfile?.major ?: "Computer Science & Engineering") }
    var university by remember { mutableStateOf(userProfile?.university ?: "Stanford University") }
    var targetCgpa by remember { mutableStateOf("${userProfile?.targetCgpa ?: 3.90}") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Student Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = major,
                    onValueChange = { major = it },
                    label = { Text("Academic Major") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = university,
                    onValueChange = { university = it },
                    label = { Text("University / College") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = targetCgpa,
                    onValueChange = { targetCgpa = it },
                    label = { Text("Target Cumulative GPA (e.g. 3.90)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(name, studentId, major, university, targetCgpa.toDoubleOrNull() ?: 3.90)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
