package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

enum class CbtTimerUrgency {
    NORMAL,
    CAUTION,
    URGENT,
    CRITICAL
}

/**
 * Utility to calculate urgency level based on remaining seconds.
 */
fun getTimerUrgency(remainingSeconds: Int): CbtTimerUrgency {
    return when {
        remainingSeconds <= 30 -> CbtTimerUrgency.CRITICAL
        remainingSeconds <= 120 -> CbtTimerUrgency.URGENT
        remainingSeconds <= 300 -> CbtTimerUrgency.CAUTION
        else -> CbtTimerUrgency.NORMAL
    }
}

/**
 * Polished, high-visibility countdown timer component for live CBT exams.
 * Features dynamic color shifts, circular countdown gauge, animated pulse on urgency,
 * and clear auto-submit countdown warnings.
 */
@Composable
fun CbtCountdownTimer(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val urgency = getTimerUrgency(remainingSeconds)

    val timerColor = when (urgency) {
        CbtTimerUrgency.CRITICAL -> AcademicRose
        CbtTimerUrgency.URGENT -> AcademicRose
        CbtTimerUrgency.CAUTION -> AcademicAmber
        CbtTimerUrgency.NORMAL -> PrimaryLight
    }

    val infiniteTransition = rememberInfiniteTransition(label = "TimerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (urgency == CbtTimerUrgency.CRITICAL) 1.08f else if (urgency == CbtTimerUrgency.URGENT) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (urgency == CbtTimerUrgency.CRITICAL) 500 else 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val timeString = if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    val progress = if (totalDurationSeconds > 0) {
        (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "TimerProgress"
    )

    if (compact) {
        // Compact Chip Style for top bars
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = timerColor.copy(alpha = if (urgency == CbtTimerUrgency.CRITICAL) 0.20f else 0.12f),
            border = BorderStroke(if (urgency != CbtTimerUrgency.NORMAL) 2.dp else 1.5.dp, timerColor),
            modifier = modifier
                .scale(if (urgency != CbtTimerUrgency.NORMAL) pulseScale else 1f)
                .testTag("cbt_countdown_timer")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(20.dp),
                        color = timerColor,
                        strokeWidth = 2.5.dp,
                        trackColor = timerColor.copy(alpha = 0.2f)
                    )
                    Icon(
                        imageVector = if (urgency == CbtTimerUrgency.CRITICAL) Icons.Rounded.Alarm else Icons.Rounded.Timer,
                        contentDescription = "Countdown Timer",
                        tint = timerColor,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = timeString,
                    color = timerColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                )
            }
        }
    } else {
        // Expanded Timer Card with visual status indicator
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = timerColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.5.dp, timerColor.copy(alpha = 0.6f)),
            modifier = modifier
                .fillMaxWidth()
                .testTag("cbt_countdown_timer_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(timerColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(38.dp),
                            color = timerColor,
                            strokeWidth = 3.dp,
                            trackColor = timerColor.copy(alpha = 0.25f)
                        )
                        Icon(
                            imageVector = when (urgency) {
                                CbtTimerUrgency.CRITICAL -> Icons.Rounded.Alarm
                                CbtTimerUrgency.URGENT -> Icons.Rounded.HourglassTop
                                CbtTimerUrgency.CAUTION -> Icons.Rounded.Timer
                                CbtTimerUrgency.NORMAL -> Icons.Rounded.Schedule
                            },
                            contentDescription = null,
                            tint = timerColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Time Remaining",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = timerColor
                        )
                    }
                }

                // Urgency status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = timerColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (urgency) {
                            CbtTimerUrgency.CRITICAL -> "🚨 Auto-Submit Soon!"
                            CbtTimerUrgency.URGENT -> "⚠️ < 2 Mins Left"
                            CbtTimerUrgency.CAUTION -> "⏳ 5 Mins Left"
                            CbtTimerUrgency.NORMAL -> "🟢 In Progress"
                        },
                        color = timerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Notice banner displayed during critical countdown window (<= 120s) to notify the student
 * that automatic submission will occur when time runs out.
 */
@Composable
fun CbtAutoSubmitNoticeBanner(
    remainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    if (remainingSeconds > 120 || remainingSeconds <= 0) return

    val isCritical = remainingSeconds <= 30
    val bannerColor = if (isCritical) AcademicRose else AcademicAmber

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bannerColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cbt_auto_submit_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isCritical) Icons.Rounded.AlarmOn else Icons.Rounded.Info,
                contentDescription = null,
                tint = bannerColor,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCritical) "Time almost up! Auto-submitting in ${remainingSeconds}s" else "CBT Auto-Submission Reminder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = bannerColor
                )
                Text(
                    text = "When timer hits 00:00, all answered questions will be submitted automatically.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
