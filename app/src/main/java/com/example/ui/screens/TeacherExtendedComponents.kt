package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TeacherClockInCard(
    teacherAttendance: TeacherAttendance?,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClockedIn = teacherAttendance?.status == "CLOCKED_IN"
    val isClockedOut = teacherAttendance?.status == "CLOCKED_OUT"
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
    val todayDateStr = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date())

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClockedIn) Color(0xFF064E3B) else Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isClockedIn) Color(0xFF10B981).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isClockedIn) Icons.Rounded.AlarmOn else Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = if (isClockedIn) Color(0xFF34D399) else Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "Teacher Daily Clock-In Register",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = todayDateStr,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isClockedIn -> Color(0xFF10B981)
                        isClockedOut -> Color(0xFF64748B)
                        else -> Color(0xFFF59E0B)
                    }
                ) {
                    Text(
                        text = when {
                            isClockedIn -> "ACTIVE ON DUTY"
                            isClockedOut -> "CLOCKED OUT"
                            else -> "NOT CLOCKED IN"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Attendance details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Clock-In Time", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = teacherAttendance?.clockInTimeMillis?.let { timeFormat.format(Date(it)) } ?: "--:--",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Clock-Out Time", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = teacherAttendance?.clockOutTimeMillis?.let { timeFormat.format(Date(it)) } ?: "--:--",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Admin Log", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "Synced",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Clock In / Clock Out Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isClockedIn) {
                    Button(
                        onClick = onClockIn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("teacher_clock_in_button")
                    ) {
                        Icon(Icons.Rounded.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clock In for Today", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onClockOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("teacher_clock_out_button")
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clock Out (End Shift)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherClassRegisterContent(
    viewModel: SchoolViewModel,
    students: List<SchoolUser>,
    classes: List<SchoolClass>
) {
    var selectedClass by remember { mutableStateOf(if (classes.isNotEmpty()) classes.first().name else "SS 2 Gold") }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val displayDateStr = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date()) }

    val classStudents = students.filter { it.className.equals(selectedClass, ignoreCase = true) }
    val attendanceState = remember(selectedClass, todayDateStr) {
        mutableStateMapOf<String, String>().apply {
            classStudents.forEach { student ->
                put(student.id, "PRESENT") // Default present
            }
        }
    }

    val presentCount = attendanceState.values.count { it == "PRESENT" }
    val lateCount = attendanceState.values.count { it == "LATE" }
    val absentCount = attendanceState.values.count { it == "ABSENT" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Student Attendance Register",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = displayDateStr,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Icon(
                            Icons.Rounded.HowToReg,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Mark student presence for automated attendance computation on the official terminal report card.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Class Selection Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Class Room:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val availableClasses = if (classes.isNotEmpty()) classes.map { it.name } else listOf("SS 1 Silver", "SS 2 Gold", "SS 3 Science")
                    items(availableClasses) { className ->
                        val isSelected = className.equals(selectedClass, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedClass = className
                                attendanceState.clear()
                                students.filter { it.className.equals(className, ignoreCase = true) }.forEach {
                                    attendanceState[it.id] = "PRESENT"
                                }
                            },
                            label = { Text(className, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Attendance Quick Counts & Batch Actions
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.15f)) {
                            Text(
                                text = "Present: $presentCount",
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF59E0B).copy(alpha = 0.15f)) {
                            Text(
                                text = "Late: $lateCount",
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEF4444).copy(alpha = 0.15f)) {
                            Text(
                                text = "Absent: $absentCount",
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = {
                                classStudents.forEach { attendanceState[it.id] = "PRESENT" }
                            }
                        ) {
                            Text("All Present", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Student Roll List
        items(classStudents) { student ->
            val currentStatus = attendanceState[student.id] ?: "PRESENT"

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.name.take(1),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryLight,
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                            Text(
                                text = "ID: ${student.id} • ${student.className}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Attendance Status Switcher
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Present Button
                        OutlinedIconToggleButton(
                            checked = currentStatus == "PRESENT",
                            onCheckedChange = { attendanceState[student.id] = "PRESENT" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                                checkedContainerColor = Color(0xFF10B981),
                                checkedContentColor = Color.White
                            )
                        ) {
                            Text("P", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Late Button
                        OutlinedIconToggleButton(
                            checked = currentStatus == "LATE",
                            onCheckedChange = { attendanceState[student.id] = "LATE" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                                checkedContainerColor = Color(0xFFF59E0B),
                                checkedContentColor = Color.White
                            )
                        ) {
                            Text("L", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Absent Button
                        OutlinedIconToggleButton(
                            checked = currentStatus == "ABSENT",
                            onCheckedChange = { attendanceState[student.id] = "ABSENT" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                                checkedContainerColor = Color(0xFFEF4444),
                                checkedContentColor = Color.White
                            )
                        ) {
                            Text("A", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Save Register Button
        item {
            Button(
                onClick = {
                    val records = classStudents.map { student ->
                        StudentAttendanceRecord(
                            studentId = student.id,
                            studentName = student.name,
                            className = selectedClass,
                            dateString = todayDateStr,
                            status = attendanceState[student.id] ?: "PRESENT",
                            markedByTeacherId = viewModel.currentUser.value?.id ?: "TCH-101"
                        )
                    }
                    viewModel.saveClassAttendanceRegister(selectedClass, todayDateStr, records)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_class_register_button")
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Submit Daily Register ($selectedClass)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun TeacherSubjectManagementContent(
    subjects: List<SchoolSubject>,
    teachers: List<SchoolUser>,
    onAddSubjectClick: () -> Unit,
    onDeleteSubject: (SchoolSubject) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subject Management (${subjects.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create, assign, and organize secondary school curriculum",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAddSubjectClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("teacher_add_subject_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Subject")
                    }
                }
            }
        }

        items(subjects) { subject ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val colorParsed = try {
                            Color(android.graphics.Color.parseColor(subject.colorHex))
                        } catch (e: Exception) {
                            PrimaryLight
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorParsed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subject.code.take(3),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Column {
                            Text(text = subject.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Code: ${subject.code} • Level: ${subject.classLevel}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = PrimaryLight
                                )
                                Text(
                                    text = "Assigned to: ${subject.teacherName}",
                                    fontSize = 11.5.sp,
                                    color = PrimaryLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { onDeleteSubject(subject) }
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Subject",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
