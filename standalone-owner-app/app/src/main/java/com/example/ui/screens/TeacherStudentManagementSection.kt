package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentManagementSection(
    viewModel: SchoolViewModel,
    currentUser: SchoolUser?,
    allStudents: List<SchoolUser>,
    allClasses: List<SchoolClass>,
    modifier: Modifier = Modifier
) {
    val teacherAssignedClass = currentUser?.className ?: "SS 2 Gold"
    var selectedClassFilter by remember { mutableStateOf(teacherAssignedClass) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<SchoolUser?>(null) }
    var studentToViewBio by remember { mutableStateOf<SchoolUser?>(null) }
    var studentToDelete by remember { mutableStateOf<SchoolUser?>(null) }

    val filteredStudents = allStudents.filter { student ->
        val matchesClass = if (selectedClassFilter == "All Classes") true else student.className.equals(selectedClassFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                student.name.contains(searchQuery, ignoreCase = true) ||
                student.id.contains(searchQuery, ignoreCase = true) ||
                student.guardianName.contains(searchQuery, ignoreCase = true)
        matchesClass && matchesSearch
    }

    val totalInSelectedClass = allStudents.count { it.className.equals(selectedClassFilter, ignoreCase = true) }
    val maleCount = filteredStudents.count { it.gender.equals("Male", ignoreCase = true) }
    val femaleCount = filteredStudents.count { it.gender.equals("Female", ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with Action to Add Student
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
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
                    Column {
                        Text(
                            text = "Class Student Profiles",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Class Teacher Register & Guardian Bio Records",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Button(
                        onClick = { showAddStudentDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcademicEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_student_button")
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Student", fontWeight = FontWeight.Bold)
                    }
                }

                // Quick Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Listed", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("${filteredStudents.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Male", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("$maleCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Female", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("$femaleCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF472B6))
                        }
                    }
                }
            }
        }

        // Class Selection Chips & Search Field
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, ID or guardian") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("student_search_input")
            )

            // Class Chips Row
            val classOptions = listOf("All Classes") + (if (allClasses.isNotEmpty()) allClasses.map { it.name } else listOf("SS 2 Gold", "SS 3 Science", "JSS 1 Blue", "JSS 3 Emerald"))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classOptions) { className ->
                    val isSelected = selectedClassFilter == className
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedClassFilter = className },
                        label = { Text(className, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Students List
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No students found in $selectedClassFilter",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap 'Add Student' above to register a student and upload their photo.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentItemCard(
                        student = student,
                        onViewBio = { studentToViewBio = student },
                        onEdit = { studentToEdit = student },
                        onDelete = { studentToDelete = student },
                        onPhotoChanged = { newPhotoUri ->
                            viewModel.updateUserPhoto(student.id, newPhotoUri)
                        }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Student Profile Dialog
    if (showAddStudentDialog) {
        StudentProfileEditorDialog(
            title = "Register New Student",
            initialClass = if (selectedClassFilter != "All Classes") selectedClassFilter else teacherAssignedClass,
            allClasses = allClasses,
            onDismiss = { showAddStudentDialog = false },
            onSave = { name, admNo, className, gender, dob, guardianName, guardianPhone, guardianEmail, address, bloodGroup, genotype, photoUri, avatarColor ->
                viewModel.addStudentProfile(
                    name = name,
                    admissionNo = admNo,
                    className = className,
                    gender = gender,
                    dateOfBirth = dob,
                    guardianName = guardianName,
                    guardianPhone = guardianPhone,
                    guardianEmail = guardianEmail,
                    residentialAddress = address,
                    bloodGroup = bloodGroup,
                    genotype = genotype,
                    photoUri = photoUri,
                    avatarColorHex = avatarColor
                )
                showAddStudentDialog = false
            }
        )
    }

    // 2. Edit Student Profile Dialog
    if (studentToEdit != null) {
        val student = studentToEdit!!
        StudentProfileEditorDialog(
            title = "Edit Student Profile",
            studentToEdit = student,
            initialClass = student.className,
            allClasses = allClasses,
            onDismiss = { studentToEdit = null },
            onSave = { name, admNo, className, gender, dob, guardianName, guardianPhone, guardianEmail, address, bloodGroup, genotype, photoUri, avatarColor ->
                viewModel.updateStudentProfile(
                    student.copy(
                        name = name,
                        className = className,
                        gender = gender,
                        dateOfBirth = dob,
                        guardianName = guardianName,
                        guardianPhone = guardianPhone,
                        guardianEmail = guardianEmail,
                        residentialAddress = address,
                        bloodGroup = bloodGroup,
                        genotype = genotype,
                        photoUri = photoUri ?: student.photoUri,
                        avatarColorHex = avatarColor
                    )
                )
                studentToEdit = null
            }
        )
    }

    // 3. View Full Bio Dialog
    if (studentToViewBio != null) {
        StudentBioDetailDialog(
            student = studentToViewBio!!,
            onDismiss = { studentToViewBio = null },
            onEdit = {
                val student = studentToViewBio
                studentToViewBio = null
                studentToEdit = student
            }
        )
    }

    // 4. Delete Confirmation Dialog
    if (studentToDelete != null) {
        val student = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = AcademicRose)
                    Text("Delete Student Profile", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Are you sure you want to remove '${student.name}' (${student.id}) from ${student.className}? This will remove them from the class register.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudentProfile(student)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicRose)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: SchoolUser,
    onViewBio: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPhotoChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoChanged(it.toString()) }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Photo / Avatar with tap to change
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .clickable { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    user = student,
                    size = 54.dp
                )
                // Small camera overlay icon
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, PrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        contentDescription = "Upload Photo",
                        tint = PrimaryLight,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Student Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewBio() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (student.gender.equals("Female", ignoreCase = true)) Color(0xFFFDF2F8) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = student.gender.take(1).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (student.gender.equals("Female", ignoreCase = true)) Color(0xFFDB2777) else Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "ID: ${student.id} • Class: ${student.className}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (student.guardianName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.FamilyRestroom,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Guardian: ${student.guardianName}",
                            fontSize = 11.sp,
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    onClick = onViewBio,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.Visibility,
                        contentDescription = "View Profile",
                        tint = PrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete Profile",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileEditorDialog(
    title: String,
    studentToEdit: SchoolUser? = null,
    initialClass: String,
    allClasses: List<SchoolClass>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        admNo: String,
        className: String,
        gender: String,
        dob: String,
        guardianName: String,
        guardianPhone: String,
        guardianEmail: String,
        address: String,
        bloodGroup: String,
        genotype: String,
        photoUri: String?,
        avatarColor: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var admNo by remember {
        mutableStateOf(
            studentToEdit?.id ?: "STU-2025-0${(45..99).random()}"
        )
    }
    var className by remember { mutableStateOf(studentToEdit?.className ?: initialClass) }
    var gender by remember { mutableStateOf(studentToEdit?.gender ?: "Female") }
    var dob by remember { mutableStateOf(studentToEdit?.dateOfBirth ?: "2009-05-14") }
    var guardianName by remember { mutableStateOf(studentToEdit?.guardianName ?: "") }
    var guardianPhone by remember { mutableStateOf(studentToEdit?.guardianPhone ?: "") }
    var guardianEmail by remember { mutableStateOf(studentToEdit?.guardianEmail ?: "") }
    var address by remember { mutableStateOf(studentToEdit?.residentialAddress ?: "") }
    var bloodGroup by remember { mutableStateOf(studentToEdit?.bloodGroup ?: "O+") }
    var genotype by remember { mutableStateOf(studentToEdit?.genotype ?: "AA") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(studentToEdit?.photoUri) }
    var selectedAvatarColor by remember { mutableStateOf(studentToEdit?.avatarColorHex ?: "#059669") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedPhotoUri = it.toString() }
    }

    val avatarColors = listOf("#059669", "#2563EB", "#7C3AED", "#D97706", "#DC2626", "#0D9488")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = PrimaryLight)
                Text(text = title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Photo Upload Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(selectedAvatarColor)).copy(alpha = 0.2f))
                                    .border(2.dp, PrimaryLight, CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!selectedPhotoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = selectedPhotoUri,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.AddPhotoAlternate,
                                        contentDescription = "Add Photo",
                                        tint = PrimaryLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedPhotoUri != null) "Photo Selected" else "Upload Profile Picture",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Tap to pick from phone file manager or storage",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Browse Files", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Student Identity Section
                item {
                    Text("Student Identity", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryLight)
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name (e.g. Adebayo Ogunlesi)") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("student_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = admNo,
                        onValueChange = { admNo = it },
                        label = { Text("Admission Number / ID") },
                        leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("student_adm_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Class (e.g. SS 2 Gold)") },
                            leadingIcon = { Icon(Icons.Rounded.Class, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Gender Toggle
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gender", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = gender == "Female",
                                    onClick = { gender = "Female" },
                                    label = { Text("Female", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = gender == "Male",
                                    onClick = { gender = "Male" },
                                    label = { Text("Male", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth") },
                            leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group") },
                            leadingIcon = { Icon(Icons.Rounded.Bloodtype, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Guardian & Parent Contacts
                item {
                    Text("Parent / Guardian Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7C3AED))
                }

                item {
                    OutlinedTextField(
                        value = guardianName,
                        onValueChange = { guardianName = it },
                        label = { Text("Guardian Full Name") },
                        leadingIcon = { Icon(Icons.Rounded.FamilyRestroom, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = guardianPhone,
                            onValueChange = { guardianPhone = it },
                            label = { Text("Guardian Phone") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = guardianEmail,
                            onValueChange = { guardianEmail = it },
                            label = { Text("Guardian Email") },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Avatar Color Selection
                item {
                    Text("Avatar Accent Theme", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        avatarColors.forEach { colorHex ->
                            val color = Color(android.graphics.Color.parseColor(colorHex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedAvatarColor = colorHex }
                                    .then(
                                        if (selectedAvatarColor == colorHex) {
                                            Modifier.border(2.dp, Color.Black, CircleShape)
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && admNo.isNotBlank()) {
                        onSave(
                            name,
                            admNo,
                            className,
                            gender,
                            dob,
                            guardianName,
                            guardianPhone,
                            guardianEmail,
                            address,
                            bloodGroup,
                            genotype,
                            selectedPhotoUri,
                            selectedAvatarColor
                        )
                    }
                },
                modifier = Modifier.testTag("save_student_profile_button")
            ) {
                Text("Save Profile")
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
fun StudentBioDetailDialog(
    student: SchoolUser,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserAvatar(user = student, size = 48.dp)
                Column {
                    Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "ID: ${student.id} • ${student.className}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Divider()

                // Basic Demographic Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gender", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(student.gender, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Date of Birth", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(student.dateOfBirth, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Blood Group", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(student.bloodGroup, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Genotype", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(student.genotype, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Divider()

                // Guardian Info
                Text("Guardian / Parent Information", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7C3AED))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Name: ${if (student.guardianName.isNotBlank()) student.guardianName else "Not recorded"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Phone: ${if (student.guardianPhone.isNotBlank()) student.guardianPhone else "Not recorded"}",
                        fontSize = 13.sp,
                        color = PrimaryLight
                    )
                    Text(
                        text = "Email: ${if (student.guardianEmail.isNotBlank()) student.guardianEmail else "Not recorded"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Address: ${if (student.residentialAddress.isNotBlank()) student.residentialAddress else "Lagos, Nigeria"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AcademicEmerald.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = AcademicEmerald, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Student is actively enrolled for 2024/2025 Academic Session with Continuous Assessment enabled.",
                            fontSize = 11.sp,
                            color = AcademicEmerald,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
