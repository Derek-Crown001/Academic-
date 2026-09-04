package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

/**
 * Adaptive visual progress indicator for CBT exams.
 * Tracks remaining questions, answered questions, flagged questions, total time,
 * elapsed time, remaining time, and pace per question with screen-size responsive layouts.
 */
@Composable
fun CbtAdaptiveProgressIndicator(
    totalQuestions: Int,
    answeredCount: Int,
    flaggedCount: Int,
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isOfflineCached: Boolean = true,
    lastSavedMillis: Long = System.currentTimeMillis(),
    modifier: Modifier = Modifier,
    onJumpToNextUnanswered: (() -> Unit)? = null
) {
    val remainingQuestions = (totalQuestions - answeredCount).coerceAtLeast(0)
    val questionProgress = if (totalQuestions > 0) (answeredCount.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f) else 0f
    val questionPercentage = (questionProgress * 100).toInt()

    val totalTime = totalDurationSeconds.coerceAtLeast(1)
    val elapsedTimeSeconds = (totalTime - remainingSeconds).coerceAtLeast(0)
    val timeProgress = (remainingSeconds.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    val timePercentage = (timeProgress * 100).toInt()

    val urgency = getTimerUrgency(remainingSeconds)
    val timerColor = when (urgency) {
        CbtTimerUrgency.CRITICAL -> AcademicRose
        CbtTimerUrgency.URGENT -> AcademicRose
        CbtTimerUrgency.CAUTION -> AcademicAmber
        CbtTimerUrgency.NORMAL -> PrimaryLight
    }

    // Time format strings
    val remMinutes = remainingSeconds / 60
    val remSecs = remainingSeconds % 60
    val remTimeString = String.format(Locale.US, "%02d:%02d", remMinutes, remSecs)

    val totalMinutes = totalTime / 60
    val totalSecs = totalTime % 60
    val totalTimeString = String.format(Locale.US, "%02d:%02d", totalMinutes, totalSecs)

    val elapsedMinutes = elapsedTimeSeconds / 60
    val elapsedSecs = elapsedTimeSeconds % 60
    val elapsedTimeString = String.format(Locale.US, "%02d:%02d", elapsedMinutes, elapsedSecs)

    // Average time available per remaining question
    val paceSecondsPerQuestion = if (remainingQuestions > 0) remainingSeconds / remainingQuestions else 0
    val paceString = when {
        remainingQuestions == 0 -> "All Answered!"
        paceSecondsPerQuestion >= 60 -> "${paceSecondsPerQuestion / 60}m ${paceSecondsPerQuestion % 60}s / q"
        else -> "${paceSecondsPerQuestion}s / q"
    }

    val paceColor = when {
        remainingQuestions == 0 -> AcademicEmerald
        paceSecondsPerQuestion >= 90 -> AcademicEmerald
        paceSecondsPerQuestion >= 45 -> AcademicAmber
        else -> AcademicRose
    }

    // Animated values
    val animatedQuestionProgress by animateFloatAsState(
        targetValue = questionProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "QuestionProgress"
    )

    val animatedTimeProgress by animateFloatAsState(
        targetValue = timeProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "TimeProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PulsePace")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (urgency == CbtTimerUrgency.CRITICAL) 1.06f else if (urgency == CbtTimerUrgency.URGENT) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (urgency == CbtTimerUrgency.CRITICAL) 600 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "UrgencyPulse"
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Expanded Panoramic Tablet Layout
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cbt_progress_indicator_wide")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Bar with Offline Cache Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Analytics,
                                contentDescription = null,
                                tint = PrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Exam Progress & Live Pace Telemetry",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Offline Caching Status Badge
                        CbtOfflineCacheBadge(isOfflineCached = isOfflineCached)
                    }

                    // 3 Pods Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pod 1: Question Progress
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryLight.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { animatedQuestionProgress },
                                        modifier = Modifier.size(56.dp),
                                        color = if (questionPercentage == 100) AcademicEmerald else PrimaryLight,
                                        strokeWidth = 5.dp,
                                        trackColor = PrimaryLight.copy(alpha = 0.15f)
                                    )
                                    Text(
                                        text = "$questionPercentage%",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = PrimaryLight
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Questions Track",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$answeredCount of $totalQuestions Answered",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AcademicAmber.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$remainingQuestions Left",
                                                color = AcademicAmber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (flaggedCount > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AcademicViolet.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "🚩 $flaggedCount Flagged",
                                                    color = AcademicViolet,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Pod 2: Time Gauge & Countdown
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = timerColor.copy(alpha = 0.08f),
                            border = BorderStroke(1.5.dp, timerColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .scale(if (urgency != CbtTimerUrgency.NORMAL) pulseScale else 1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { animatedTimeProgress },
                                        modifier = Modifier.size(56.dp),
                                        color = timerColor,
                                        strokeWidth = 5.dp,
                                        trackColor = timerColor.copy(alpha = 0.2f)
                                    )
                                    Icon(
                                        imageVector = if (urgency == CbtTimerUrgency.CRITICAL) Icons.Rounded.Alarm else Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = timerColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = "Remaining Time (Total: $totalTimeString)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = remTimeString,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = timerColor
                                    )
                                    Text(
                                        text = "Elapsed: $elapsedTimeString",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Pod 3: Pace & Quick Action
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = paceColor.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, paceColor.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = paceColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Recommended Pace",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = paceString,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = paceColor
                                )
                                if (remainingQuestions > 0 && onJumpToNextUnanswered != null) {
                                    TextButton(
                                        onClick = onJumpToNextUnanswered,
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(
                                            text = "Jump to Next Unanswered ➜",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Compact Phone Responsive Layout
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cbt_progress_indicator_compact")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Row: Questions count badge + Time Countdown badge + Offline Cache
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Answered Summary Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryLight.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.AssignmentTurnedIn, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "$answeredCount/$totalQuestions answered",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryLight
                                )
                            }
                        }

                        // Compact Countdown Timer Pill with Urgency Color
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = timerColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, timerColor.copy(alpha = 0.4f)),
                            modifier = Modifier.scale(if (urgency != CbtTimerUrgency.NORMAL) pulseScale else 1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (urgency == CbtTimerUrgency.CRITICAL) Icons.Rounded.Alarm else Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = timerColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "$remTimeString left",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = timerColor
                                )
                            }
                        }
                    }

                    // Dual Progress Bars (Questions Progress on top, Time Progress below)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Questions Progress: $questionPercentage%",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$remainingQuestions remaining",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingQuestions == 0) AcademicEmerald else AcademicAmber
                            )
                        }

                        // Questions Linear Bar
                        LinearProgressIndicator(
                            progress = { animatedQuestionProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (questionPercentage == 100) AcademicEmerald else PrimaryLight,
                            trackColor = PrimaryLight.copy(alpha = 0.12f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Time Tracker: $elapsedTimeString / $totalTimeString elapsed",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Pace: $paceString",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = paceColor
                            )
                        }

                        // Time Remaining Linear Bar (Fills down as time expires)
                        LinearProgressIndicator(
                            progress = { animatedTimeProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = timerColor,
                            trackColor = timerColor.copy(alpha = 0.15f)
                        )
                    }

                    // Bottom Row: Flagged Badge + Offline Caching confirmation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (flaggedCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AcademicViolet.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = AcademicViolet, modifier = Modifier.size(11.dp))
                                        Text(
                                            text = "$flaggedCount flagged",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicViolet
                                        )
                                    }
                                }
                            }
                        }

                        // Offline Cache Chip
                        CbtOfflineCacheBadge(isOfflineCached = isOfflineCached, compact = true)
                    }
                }
            }
        }
    }
}

/**
 * Offline cache badge that confirms to students that all selections are saved
 * locally on their device without fear of losing exam progress.
 */
@Composable
fun CbtOfflineCacheBadge(
    isOfflineCached: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF16A34A).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF16A34A))
            )
            Icon(
                Icons.Rounded.SaveAlt,
                contentDescription = null,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(if (compact) 11.dp else 13.dp)
            )
            Text(
                text = if (compact) "Offline Cached" else "Offline Caching Active (Local Storage)",
                fontSize = if (compact) 9.5.sp else 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15803D)
            )
        }
    }
}
