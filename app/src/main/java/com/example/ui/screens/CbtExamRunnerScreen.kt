package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CbtQuestion
import com.example.ui.components.CbtAutoSubmitNoticeBanner
import com.example.ui.components.CbtCountdownTimer
import com.example.ui.theme.*
import com.example.ui.viewmodel.CbtRunnerState
import com.example.ui.viewmodel.SchoolViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtExamRunnerScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val runnerState by viewModel.cbtRunnerState.collectAsState()
    var showSubmitDialog by remember { mutableStateOf(false) }

    // If time has expired or is submitting, auto-dismiss any manual submit dialog
    LaunchedEffect(runnerState.remainingSeconds, runnerState.isSubmitting) {
        if (runnerState.remainingSeconds <= 0 || runnerState.isSubmitting) {
            showSubmitDialog = false
        }
    }

    if (runnerState.isSubmitted && runnerState.submissionResult != null) {
        // Result Screen
        CbtResultReviewScreen(
            runnerState = runnerState,
            onClose = { viewModel.exitCbtRunner() }
        )
        return
    }

    val exam = runnerState.exam ?: return
    val questions = runnerState.questions
    if (questions.isEmpty()) return

    val currentIdx = runnerState.currentQuestionIndex.coerceIn(0, questions.size - 1)
    val currentQuestion = questions[currentIdx]
    val selectedOption = runnerState.selectedAnswers[currentQuestion.id]
    val isFlagged = runnerState.flaggedQuestionIds.contains(currentQuestion.id)

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = exam.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${exam.subjectName} • ${exam.className}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Dedicated Live Countdown Timer Component
                        CbtCountdownTimer(
                            remainingSeconds = runnerState.remainingSeconds,
                            totalDurationSeconds = if (runnerState.totalDurationSeconds > 0) runnerState.totalDurationSeconds else exam.durationMinutes * 60,
                            compact = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    val answeredCount = runnerState.selectedAnswers.size
                    LinearProgressIndicator(
                        progress = { (answeredCount.toFloat() / questions.size.toFloat()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryLight,
                        trackColor = PrimaryLight.copy(alpha = 0.15f)
                    )
                }
            }
        },
        bottomBar = {
            val answeredCount = runnerState.selectedAnswers.size
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Question Palette Matrix
                    Text(
                        text = "Question Palette (${answeredCount}/${questions.size} answered)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(questions) { idx, q ->
                            val isAns = runnerState.selectedAnswers.containsKey(q.id)
                            val isFlg = runnerState.flaggedQuestionIds.contains(q.id)
                            val isCurrent = idx == currentIdx

                            val itemBg = when {
                                isCurrent -> PrimaryLight
                                isFlg -> AcademicViolet
                                isAns -> AcademicEmerald
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val itemText = when {
                                isCurrent || isFlg || isAns -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(itemBg)
                                    .clickable { viewModel.navigateToQuestion(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = itemText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.navigateToQuestion(currentIdx - 1) },
                            enabled = currentIdx > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("cbt_prev_button")
                        ) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev")
                        }

                        IconButton(
                            onClick = { viewModel.toggleFlagQuestion(currentQuestion.id) },
                            modifier = Modifier.testTag("cbt_flag_button")
                        ) {
                            Icon(
                                imageVector = if (isFlagged) Icons.Rounded.BookmarkRemove else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Flag",
                                tint = if (isFlagged) AcademicViolet else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentIdx < questions.size - 1) {
                            Button(
                                onClick = { viewModel.navigateToQuestion(currentIdx + 1) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("cbt_next_button")
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = { showSubmitDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AcademicEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("cbt_submit_button")
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit CBT", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auto-Submit Notice Banner during critical countdown
            item {
                CbtAutoSubmitNoticeBanner(remainingSeconds = runnerState.remainingSeconds)
            }

            // Question Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryLight.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Question ${currentIdx + 1} of ${questions.size}",
                            color = PrimaryLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "${currentQuestion.marks} Marks",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // Question Text Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // Options List
            val options = listOf(
                "A" to currentQuestion.optionA,
                "B" to currentQuestion.optionB,
                "C" to currentQuestion.optionC,
                "D" to currentQuestion.optionD
            )

            itemsIndexed(options) { _, (optLetter, optText) ->
                val isSelected = selectedOption == optLetter
                val optBorderColor = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.outlineVariant
                val optBgColor = if (isSelected) PrimaryLight.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = optBgColor),
                    border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, optBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectCbtOption(currentQuestion.id, optLetter) }
                        .testTag("cbt_option_${optLetter.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optLetter,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Auto-Submitting Progress Overlay Dialog
    if (runnerState.isSubmitting || (runnerState.remainingSeconds <= 0 && runnerState.isRunning)) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss during auto-submission */ },
            icon = {
                CircularProgressIndicator(
                    color = AcademicRose,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "⏰ Time Expired!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your assessment time has concluded. Auto-submitting and grading your answers now...",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Please hold on while your official score is registered.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Manual Submit Confirmation Dialog
    if (showSubmitDialog && !runnerState.isSubmitting && runnerState.remainingSeconds > 0) {
        val totalQ = questions.size
        val answeredQ = runnerState.selectedAnswers.size
        val unansweredQ = totalQ - answeredQ

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Submit CBT Assessment?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You have answered $answeredQ of $totalQ questions.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    if (unansweredQ > 0) {
                        Text(
                            text = "Warning: You have $unansweredQ unanswered question(s).",
                            color = AcademicRose,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "Once submitted, your answers will be finalized and graded automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitCbtExam(isAutoSubmit = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicEmerald),
                    modifier = Modifier.testTag("confirm_submit_cbt_button")
                ) {
                    Text("Confirm & Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Review More")
                }
            }
        )
    }
}

@Composable
fun CbtResultReviewScreen(
    runnerState: CbtRunnerState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val submission = runnerState.submissionResult ?: return
    val questions = runnerState.questions

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Summary Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (submission.isPassed) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (submission.isPassed) AcademicEmerald else AcademicRose
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (submission.isPassed) Icons.Rounded.EmojiEvents else Icons.Rounded.Info,
                        contentDescription = null,
                        tint = if (submission.isPassed) AcademicEmerald else AcademicRose,
                        modifier = Modifier.size(52.dp)
                    )

                    Text(
                        text = if (submission.isPassed) "CBT Assessment Passed!" else "Assessment Completed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (submission.isPassed) Color(0xFF166534) else Color(0xFF991B1B)
                    )

                    // Submission Mode Badge (Auto-Submit vs Manual)
                    if (runnerState.isAutoSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AcademicRose.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcademicRose.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = AcademicRose,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Auto-Submitted on Time Expiry",
                                    color = AcademicRose,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = submission.examTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    // Score Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${submission.score} / ${submission.totalMarks}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (submission.isPassed) AcademicEmerald else AcademicRose
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f%%", submission.percentage),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onClose,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exit_cbt_review_button")
                    ) {
                        Text("Return to Student Portal")
                    }
                }
            }
        }

        item {
            Text(
                text = "Detailed Answer Breakdown & Explanations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Questions Review List
        itemsIndexed(questions) { idx, q ->
            val studentAns = runnerState.selectedAnswers[q.id] ?: "Not answered"
            val isCorrect = studentAns.equals(q.correctOption, ignoreCase = true)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${idx + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCorrect) AcademicEmerald.copy(alpha = 0.15f) else AcademicRose.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Rounded.Check else Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) AcademicEmerald else AcademicRose,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isCorrect) "+${q.marks} Marks" else "0 Marks",
                                    color = if (isCorrect) AcademicEmerald else AcademicRose,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Your Answer: $studentAns",
                            color = if (isCorrect) AcademicEmerald else AcademicRose,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Correct Answer: ${q.correctOption}",
                            color = AcademicEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (q.explanation.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Explanation:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = q.explanation,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

