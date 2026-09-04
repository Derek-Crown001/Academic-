package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AssignmentWithCourse
import com.example.data.model.Assignment
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.parseHexColor
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    assignments: List<AssignmentWithCourse>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onAddAssignment: () -> Unit,
    onUpdateStatus: (Assignment, String, Double?) -> Unit,
    onDeleteAssignment: (Assignment) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterTabs = listOf("ALL" to "All Tasks", "PENDING" to "Pending", "IN_PROGRESS" to "In Progress", "COMPLETED" to "Completed", "GRADED" to "Graded")

    val filteredList = assignments.filter { item ->
        when (filter) {
            "ALL" -> true
            "PENDING" -> item.assignment.status == "PENDING"
            "IN_PROGRESS" -> item.assignment.status == "IN_PROGRESS"
            "COMPLETED" -> item.assignment.status == "COMPLETED"
            "GRADED" -> item.assignment.status == "GRADED"
            else -> true
        }
    }

    val dateFormat = SimpleDateFormat("EEE, MMM dd • h:mm a", Locale.getDefault())
    val now = System.currentTimeMillis()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAssignment,
                containerColor = PrimaryLight,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Task") },
                text = { Text("New Assignment", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assignments & Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${assignments.count { it.assignment.status != "COMPLETED" && it.assignment.status != "GRADED" }} Open • ${assignments.count { it.assignment.status == "COMPLETED" || it.assignment.status == "GRADED" }} Finished",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Filter Tabs Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTabs) { (key, label) ->
                        val isSelected = filter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(key) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 3. Assignment Cards
            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = AcademicEmerald
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tasks found in this view",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep up the momentum or create a new assignment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.assignment.id }) { itemWithCourse ->
                    val assignment = itemWithCourse.assignment
                    val courseColor = parseHexColor(itemWithCourse.course?.colorHex ?: "#2563EB")
                    val isOverdue = assignment.dueDateMillis < now && assignment.status != "COMPLETED" && assignment.status != "GRADED"
                    val isDone = assignment.status == "COMPLETED" || assignment.status == "GRADED"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Row 1: Course Pill + Priority Badge + Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = courseColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = itemWithCourse.course?.code ?: "COURSE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = courseColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    PriorityBadge(priority = assignment.priority)
                                }

                                StatusBadge(status = assignment.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Title & Description
                            Text(
                                text = assignment.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                            )

                            if (assignment.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = assignment.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Subtasks Checklist if present
                            if (assignment.subtasks.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val subtasks = assignment.subtasks.lines().filter { it.isNotBlank() }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    subtasks.forEach { sub ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isDone) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isDone) AcademicEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = sub,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Due Date & Score
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isOverdue) AcademicRose else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isOverdue) "Overdue: ${dateFormat.format(Date(assignment.dueDateMillis))}" else "Due: ${dateFormat.format(Date(assignment.dueDateMillis))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isOverdue) AcademicRose else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (assignment.scoreObtained != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AcademicEmerald.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Score: ${assignment.scoreObtained.toInt()}/${assignment.maxScore.toInt()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions Bar: Status Toggle & Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (assignment.status == "PENDING") {
                                        FilledTonalButton(
                                            onClick = { onUpdateStatus(assignment, "IN_PROGRESS", null) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Start Working", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    if (assignment.status != "COMPLETED" && assignment.status != "GRADED") {
                                        Button(
                                            onClick = { onUpdateStatus(assignment, "COMPLETED", null) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AcademicEmerald,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onUpdateStatus(assignment, "PENDING", null) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reopen", fontSize = 11.sp)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteAssignment(assignment) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
