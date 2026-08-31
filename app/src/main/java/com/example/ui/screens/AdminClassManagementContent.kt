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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolUser
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassManagementContent(
    classes: List<SchoolClass>,
    teachers: List<SchoolUser>,
    students: List<SchoolUser>,
    onAddClass: (name: String, level: String, arm: String, teacherId: String, teacherName: String, capacity: Int, room: String) -> Unit,
    onUpdateClass: (SchoolClass) -> Unit,
    onDeleteClass: (SchoolClass) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf("ALL") } // ALL, SS, JSS, PRIMARY
    var showAddDialog by remember { mutableStateOf(false) }
    var classToEdit by remember { mutableStateOf<SchoolClass?>(null) }
    var classToViewRoster by remember { mutableStateOf<SchoolClass?>(null) }
    var classToDelete by remember { mutableStateOf<SchoolClass?>(null) }

    // Filter classes
    val filteredClasses = classes.filter { c ->
        val matchesQuery = c.name.contains(searchQuery, ignoreCase = true) ||
                c.classTeacherName.contains(searchQuery, ignoreCase = true) ||
                c.room.contains(searchQuery, ignoreCase = true) ||
                c.level.contains(searchQuery, ignoreCase = true) ||
                c.arm.contains(searchQuery, ignoreCase = true)

        val matchesTier = when (selectedTierFilter) {
            "SS" -> c.level.contains("SS", ignoreCase = true) || c.name.contains("SS", ignoreCase = true)
            "JSS" -> c.level.contains("JSS", ignoreCase = true) || c.name.contains("JSS", ignoreCase = true)
            "PRIMARY" -> c.level.contains("Primary", ignoreCase = true) || c.level.contains("Grade", ignoreCase = true)
            else -> true
        }

        matchesQuery && matchesTier
    }

    val totalEnrolledStudents = students.size
    val totalClassesCount = classes.size
    val avgClassSize = if (totalClassesCount > 0) totalEnrolledStudents / totalClassesCount else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Quick Stats
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MeetingRoom,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Classes & Arms Management",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Configure curriculum streams, form masters & classroom capacity",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ClassStatItem(label = "Total Classes", value = "$totalClassesCount Arms")
                        ClassStatItem(label = "Enrolled Students", value = "$totalEnrolledStudents Active")
                        ClassStatItem(label = "Avg. Class Size", value = "$avgClassSize / Class")
                    }
                }
            }
        }

        // Search & Add Class Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search class, arm, teacher or room...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    modifier = Modifier.testTag("add_new_class_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Class", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Tier Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tiers = listOf(
                    "ALL" to "All Classes (${classes.size})",
                    "SS" to "Senior Sec (SS)",
                    "JSS" to "Junior Sec (JSS)",
                    "PRIMARY" to "Primary / Basic"
                )
                items(tiers) { (key, label) ->
                    FilterChip(
                        selected = selectedTierFilter == key,
                        onClick = { selectedTierFilter = key },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedTierFilter == key) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Empty state
        if (filteredClasses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Classes Found",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap 'New Class' to create an academic class, assign a Form Teacher, and set student capacity.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Class Cards List
            items(filteredClasses, key = { it.id }) { schoolClass ->
                val classEnrolledCount = students.count { it.className.equals(schoolClass.name, ignoreCase = true) }
                val targetCapacity = if (schoolClass.studentCount > 0) schoolClass.studentCount else 35
                val capacityRatio = (classEnrolledCount.toFloat() / targetCapacity.toFloat()).coerceIn(0f, 1f)

                val tierColor = when {
                    schoolClass.level.contains("SS", ignoreCase = true) -> Color(0xFF2563EB)
                    schoolClass.level.contains("JSS", ignoreCase = true) -> Color(0xFF0D9488)
                    else -> Color(0xFF7C3AED)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_card_${schoolClass.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top row: Badges and Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = tierColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = schoolClass.level,
                                        color = tierColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AcademicAmber.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Arm: ${schoolClass.arm}",
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Edit / Delete action menu
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = { classToEdit = schoolClass },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit Class",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { classToDelete = schoolClass },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete Class",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        // Class Full Name
                        Text(
                            text = schoolClass.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Teacher & Room details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = PrimaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Class / Form Teacher",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = schoolClass.classTeacherName.ifBlank { "Unassigned" },
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = AcademicEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Location / Room",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = schoolClass.room.ifBlank { "Main Block" },
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Enrollment & Capacity bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Roster Enrollment",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$classEnrolledCount / $targetCapacity Students",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (classEnrolledCount > targetCapacity) Color(0xFFEF4444) else PrimaryLight
                                )
                            }
                            LinearProgressIndicator(
                                progress = { capacityRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (capacityRatio >= 1.0f) Color(0xFFEF4444) else tierColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Bottom Actions: View Roster
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session 2025/2026",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            OutlinedButton(
                                onClick = { classToViewRoster = schoolClass },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.People, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("View Roster ($classEnrolledCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Class Dialog
    if (showAddDialog) {
        AddEditClassDialog(
            teachers = teachers,
            existingClass = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, level, arm, teacherId, teacherName, capacity, room ->
                onAddClass(name, level, arm, teacherId, teacherName, capacity, room)
                showAddDialog = false
            }
        )
    }

    // 2. Edit Class Dialog
    classToEdit?.let { cls ->
        AddEditClassDialog(
            teachers = teachers,
            existingClass = cls,
            onDismiss = { classToEdit = null },
            onConfirm = { name, level, arm, teacherId, teacherName, capacity, room ->
                onUpdateClass(
                    cls.copy(
                        name = name,
                        level = level,
                        arm = arm,
                        classTeacherId = teacherId,
                        classTeacherName = teacherName,
                        studentCount = capacity,
                        room = room
                    )
                )
                classToEdit = null
            }
        )
    }

    // 3. Class Roster Dialog
    classToViewRoster?.let { cls ->
        val classStudents = students.filter { it.className.equals(cls.name, ignoreCase = true) }
        ClassRosterDialog(
            schoolClass = cls,
            students = classStudents,
            onDismiss = { classToViewRoster = null }
        )
    }

    // 4. Delete Confirmation Dialog
    classToDelete?.let { cls ->
        val enrolledCount = students.count { it.className.equals(cls.name, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { classToDelete = null },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("Delete Class Arm '${cls.name}'?") },
            text = {
                if (enrolledCount > 0) {
                    Text("Warning: There are currently $enrolledCount active student(s) enrolled in ${cls.name}. You must reassign or remove them before deleting this class arm.")
                } else {
                    Text("Are you sure you want to delete ${cls.name}? This will remove the class arm and room assignment from the portal.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClass(cls)
                        classToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Class", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ClassStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassDialog(
    teachers: List<SchoolUser>,
    existingClass: SchoolClass?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, level: String, arm: String, teacherId: String, teacherName: String, capacity: Int, room: String) -> Unit
) {
    val isEdit = existingClass != null

    var level by remember { mutableStateOf(existingClass?.level ?: "SS 2") }
    var arm by remember { mutableStateOf(existingClass?.arm ?: "Gold") }
    var customName by remember { mutableStateOf(existingClass?.name ?: "") }
    var autoGenerateName by remember { mutableStateOf(existingClass == null) }
    var selectedTeacher by remember {
        mutableStateOf(
            if (existingClass != null) teachers.find { it.id == existingClass.classTeacherId } ?: teachers.firstOrNull()
            else teachers.firstOrNull()
        )
    }
    var room by remember { mutableStateOf(existingClass?.room ?: "Block B, Room 4") }
    var capacityText by remember { mutableStateOf(existingClass?.studentCount?.toString() ?: "35") }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    val computedName = if (autoGenerateName) {
        if (level.isNotBlank() && arm.isNotBlank()) "$level $arm".trim() else level.trim()
    } else {
        customName
    }

    val levelPresets = listOf("JSS 1", "JSS 2", "JSS 3", "SS 1", "SS 2", "SS 3", "Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12")
    val armPresets = listOf("Gold", "Diamond", "Emerald", "Silver", "Ruby", "Science", "Arts", "Commercial", "Alpha", "Beta", "Blue", "Green", "Red")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isEdit) Icons.Rounded.Edit else Icons.Rounded.AddBusiness,
                    contentDescription = null,
                    tint = PrimaryLight
                )
                Text(if (isEdit) "Edit Class Arm" else "Add New Class Arm", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Level Presets
                item {
                    Text("1. Academic Level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryLight)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(levelPresets) { preset ->
                            SuggestionChip(
                                onClick = { level = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (level == preset) PrimaryLight.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Level / Grade") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }

                // Arm / Section Presets
                item {
                    Text("2. Class Arm / Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryLight)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(armPresets) { preset ->
                            SuggestionChip(
                                onClick = { arm = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (arm == preset) AcademicAmber.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = arm,
                        onValueChange = { arm = it },
                        label = { Text("Arm / Section Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }

                // Class Name Display
                item {
                    Text("3. Full Class Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryLight)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Preview Name:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = computedName.ifBlank { "e.g. SS 2 Gold" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryLight
                            )
                        }
                    }
                }

                // Form / Class Teacher
                item {
                    Text("4. Assigned Class / Form Teacher", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryLight)
                    ExposedDropdownMenuBox(
                        expanded = teacherDropdownExpanded,
                        onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedTeacher?.name ?: "Select Teacher",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = teacherDropdownExpanded,
                            onDismissRequest = { teacherDropdownExpanded = false }
                        ) {
                            teachers.forEach { teacher ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(teacher.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (teacher.assignedSubjects.isNotBlank()) {
                                                Text(teacher.assignedSubjects, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTeacher = teacher
                                        teacherDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Room & Max Capacity
                item {
                    Text("5. Room & Target Capacity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryLight)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = room,
                            onValueChange = { room = it },
                            label = { Text("Room / Location") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = capacityText,
                            onValueChange = { capacityText = it.filter { char -> char.isDigit() } },
                            label = { Text("Capacity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val capacity = capacityText.toIntOrNull() ?: 35
                    val finalName = computedName.ifBlank { "$level $arm".trim() }
                    onConfirm(
                        finalName,
                        level.trim(),
                        arm.trim(),
                        selectedTeacher?.id ?: "",
                        selectedTeacher?.name ?: "Unassigned",
                        capacity,
                        room.trim()
                    )
                },
                enabled = level.isNotBlank() && arm.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
            ) {
                Text(if (isEdit) "Save Changes" else "Create Class", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClassRosterDialog(
    schoolClass: SchoolClass,
    students: List<SchoolUser>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.People, contentDescription = null, tint = PrimaryLight)
                Column {
                    Text("Class Roster: ${schoolClass.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Form Teacher: ${schoolClass.classTeacherName} • ${students.size} Enrolled", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No students currently enrolled in ${schoolClass.name}.\nTeachers or Admins can register students into this class arm.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryLight.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryLight,
                                        fontSize = 13.sp
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Adm: ${student.id} • ${student.gender} • DOB: ${student.dateOfBirth}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (student.guardianPhone.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AcademicEmerald.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = student.guardianPhone,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicEmerald,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close Roster")
            }
        }
    )
}
