package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportCard
import com.example.data.model.SchoolAnnouncement
import com.example.data.model.StudentGrade
import com.example.ui.components.AnnouncementCard
import com.example.ui.components.GradeBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalTab
import com.example.ui.viewmodel.SchoolViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val allReportCards by viewModel.allReportCards.collectAsState()
    val announcements by viewModel.roleAnnouncements.collectAsState()

    // Chidinma Nwosu's data as default linked child
    val studentId = "STU-2025-042"
    val studentGrades = allGrades.filter { it.studentId == studentId }
    val studentReportCard = allReportCards.find { it.studentId == studentId } ?: allReportCards.firstOrNull()

    Column(modifier = modifier.fillMaxSize()) {
        // Parent Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = when (currentTab) {
                PortalTab.PARENT_CHILD_OVERVIEW -> 0
                PortalTab.PARENT_REPORT_CARD -> 1
                PortalTab.PARENT_ANNOUNCEMENTS -> 2
                else -> 0
            },
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF7C3AED)
        ) {
            Tab(
                selected = currentTab == PortalTab.PARENT_CHILD_OVERVIEW,
                onClick = { viewModel.selectTab(PortalTab.PARENT_CHILD_OVERVIEW) },
                text = { Text("Ward Overview", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.FamilyRestroom, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.PARENT_REPORT_CARD,
                onClick = { viewModel.selectTab(PortalTab.PARENT_REPORT_CARD) },
                text = { Text("Terminal Report Sheet", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Assessment, contentDescription = null) }
            )
            Tab(
                selected = currentTab == PortalTab.PARENT_ANNOUNCEMENTS,
                onClick = { viewModel.selectTab(PortalTab.PARENT_ANNOUNCEMENTS) },
                text = { Text("PTA & School Notices", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Campaign, contentDescription = null) }
            )
        }

        when (currentTab) {
            PortalTab.PARENT_CHILD_OVERVIEW -> {
                ParentChildOverviewContent(
                    reportCard = studentReportCard,
                    grades = studentGrades,
                    onExportPdf = { rc -> viewModel.exportReportCardPdf(context, rc) }
                )
            }
            PortalTab.PARENT_REPORT_CARD -> {
                StudentReportCardContent(
                    reportCard = studentReportCard,
                    grades = studentGrades,
                    onExportPdf = { rc -> viewModel.exportReportCardPdf(context, rc) }
                )
            }
            PortalTab.PARENT_ANNOUNCEMENTS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Parent & PTA Circulars (${announcements.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(announcements) { ann ->
                        AnnouncementCard(announcement = ann)
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ParentChildOverviewContent(
    reportCard: ReportCard?,
    grades: List<StudentGrade>,
    onExportPdf: (ReportCard) -> Unit
) {
    if (reportCard == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Child profile loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ward Profile Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5B21B6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ward: ${reportCard.studentName}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Class: ${reportCard.className} • Admission No: ${reportCard.admissionNo}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (reportCard.isApprovedByAdmin) Color(0xFF10B981) else Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = if (reportCard.isApprovedByAdmin) "OFFICIAL REPORT" else "PENDING APPROVAL",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (!reportCard.isApprovedByAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.PendingActions, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Class Teacher has prepared these results. Awaiting School Principal's official seal and release.",
                                    color = Color.White,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { onExportPdf(reportCard) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF5B21B6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_download_pdf_button")
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (reportCard.isApprovedByAdmin) "Download Official PDF Report Card" else "Preview Draft PDF Report Card",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Overall Average", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(Locale.US, "%.1f%%", reportCard.averageScore),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryLight
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Attendance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${reportCard.attendancePresent}/${reportCard.attendanceTotal} Days",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademicEmerald
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Course Performance & Grades (${grades.size} Subjects)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(grades) { grade ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = grade.subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Score: ${grade.totalScore}/100 • ${grade.remarks}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GradeBadge(grade = grade.gradeLetter)
                }
            }
        }
    }
}
