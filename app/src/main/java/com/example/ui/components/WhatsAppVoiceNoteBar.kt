package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.VoiceNoteRecorder
import kotlin.random.Random

/**
 * WhatsApp-Style Voice Note Input Bar with Live Waveform Animation & Recording State
 */
@Composable
fun WhatsAppVoiceNoteInputBar(
    userPrompt: String,
    onPromptChange: (String) -> Unit,
    isRecording: Boolean,
    recordingDurationSeconds: Int,
    amplitude: Float,
    isAiLoading: Boolean,
    accentColor: Color,
    placeholderText: String = "Ask anything (Math, Coding, Science, History)...",
    onStartRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSendVoiceNote: () -> Unit,
    onSendTextMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRecording) Color(0xFFEF4444).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        AnimatedContent(
            targetState = isRecording,
            transitionSpec = {
                fadeIn(tween(220)) togetherWith fadeOut(tween(180))
            },
            label = "voice_bar_transition"
        ) { recordingActive ->
            if (recordingActive) {
                // WhatsApp Active Recording Mode
                WhatsAppRecordingActiveBar(
                    durationSeconds = recordingDurationSeconds,
                    amplitude = amplitude,
                    accentColor = accentColor,
                    onCancel = onCancelRecording,
                    onSend = onSendVoiceNote
                )
            } else {
                // Standard Text + WhatsApp Mic Input Mode
                WhatsAppTextInputMode(
                    userPrompt = userPrompt,
                    onPromptChange = onPromptChange,
                    isAiLoading = isAiLoading,
                    accentColor = accentColor,
                    placeholderText = placeholderText,
                    onStartVoice = onStartRecording,
                    onSendText = onSendTextMessage
                )
            }
        }
    }
}

@Composable
private fun WhatsAppTextInputMode(
    userPrompt: String,
    onPromptChange: (String) -> Unit,
    isAiLoading: Boolean,
    accentColor: Color,
    placeholderText: String,
    onStartVoice: () -> Unit,
    onSendText: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Text Input Field
        TextField(
            value = userPrompt,
            onValueChange = onPromptChange,
            placeholder = {
                Text(
                    text = placeholderText,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            maxLines = 3,
            modifier = Modifier
                .weight(1f)
                .testTag("ai_prompt_text_field")
        )

        // If user is typing text, show Send Button; otherwise show WhatsApp Voice Note Mic Button
        if (userPrompt.isNotBlank()) {
            IconButton(
                onClick = onSendText,
                enabled = !isAiLoading,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (!isAiLoading) accentColor else Color.Gray.copy(alpha = 0.4f))
                    .testTag("send_ai_text_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // WhatsApp Voice Note Mic Button
            WhatsAppMicButton(
                accentColor = accentColor,
                isAiLoading = isAiLoading,
                onClick = onStartVoice
            )
        }
    }
}

@Composable
private fun WhatsAppMicButton(
    accentColor: Color,
    isAiLoading: Boolean,
    onClick: () -> Unit
) {
    val whatsAppGreen = Color(0xFF25D366)

    Surface(
        shape = CircleShape,
        color = whatsAppGreen,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = !isAiLoading, onClick = onClick)
            .testTag("whatsapp_voice_mic_btn")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Record WhatsApp Voice Note",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WhatsAppRecordingActiveBar(
    durationSeconds: Int,
    amplitude: Float,
    accentColor: Color,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val redDotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "red_dot"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Discard / Trash Button
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                .testTag("cancel_voice_recording_btn")
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Cancel & Discard",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
            )
        }

        // Live Pulsing Dot + Duration Timer + Waveform Visualizer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            // Pulsing Red Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(redDotScale)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            )

            // Duration Timer
            Text(
                text = VoiceNoteRecorder.formatDuration(durationSeconds),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )

            // Live Dancing Sound Waveform Bars
            VoiceWaveformVisualizer(
                amplitude = amplitude,
                barCount = 14,
                activeColor = Color(0xFF25D366),
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
            )
        }

        // Send Voice Note Button
        Surface(
            shape = CircleShape,
            color = Color(0xFF25D366),
            shadowElevation = 3.dp,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onSend)
                .testTag("send_voice_note_btn")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send Voice Note",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Animated Waveform Bar Visualizer simulating WhatsApp audio notes
 */
@Composable
fun VoiceWaveformVisualizer(
    amplitude: Float,
    barCount: Int = 16,
    activeColor: Color = Color(0xFF25D366),
    modifier: Modifier = Modifier
) {
    val randomFactors = remember {
        List(barCount) { 0.35f + Random.nextFloat() * 0.65f }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        randomFactors.forEach { factor ->
            val animatedHeight by animateFloatAsState(
                targetValue = (factor * amplitude * 24f).coerceIn(4f, 24f),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "bar_height"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(activeColor)
            )
        }
    }
}

/**
 * WhatsApp-Style Voice Note Bubble for Audio Messages
 */
@Composable
fun WhatsAppVoiceNoteBubble(
    isUser: Boolean,
    durationText: String = "0:08",
    transcribedText: String,
    roleThemeColor: Color,
    isPlaying: Boolean,
    playbackSpeed: Float = 1.0f,
    onTogglePlay: () -> Unit,
    onToggleSpeed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isUser) Color(0xFF075E54) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val accentWaveColor = if (isUser) Color(0xFF25D366) else roleThemeColor

    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        color = bubbleColor,
        border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null,
        modifier = modifier.widthIn(max = 320.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Audio Note Header with Play Button & Waveform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // WhatsApp Play / Pause Round Button
                Surface(
                    shape = CircleShape,
                    color = accentWaveColor,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePlay)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Waveform Visualizer
                VoiceWaveformVisualizer(
                    amplitude = if (isPlaying) 0.85f else 0.35f,
                    barCount = 16,
                    activeColor = if (isUser) Color.White.copy(alpha = 0.9f) else accentWaveColor,
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                )

                // Voice Note Duration & Speed Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = durationText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isUser) Color.White.copy(alpha = 0.2f) else accentWaveColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onToggleSpeed)
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isUser) Color.White else accentWaveColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Transcribed Text
            if (transcribedText.isNotBlank()) {
                HorizontalDivider(
                    color = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 0.8.dp
                )

                Text(
                    text = transcribedText,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
