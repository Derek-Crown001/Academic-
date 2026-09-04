package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AcademicViolet
import com.example.ui.theme.PrimaryLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    var currentLoadingStage by remember { mutableStateOf("Initializing Academic Portal...") }
    var progressValue by remember { mutableStateOf(0f) }

    // Pulsing animation for outer logo rings
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    LaunchedEffect(Unit) {
        startAnimation = true

        delay(400)
        progressValue = 0.35f
        currentLoadingStage = "Connecting Gemini AI Assistant & CBT Engine..."

        delay(600)
        progressValue = 0.75f
        currentLoadingStage = "Syncing Student Gradebook & Class Register..."

        delay(600)
        progressValue = 1.0f
        currentLoadingStage = "Welcome to AcademiaTrack"

        delay(500)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Deep Slate Navy
                        Color(0xFF1E1B4B), // Midnight Indigo
                        Color(0xFF0F172A)
                    )
                )
            )
            .clickable {
                // Allow user to tap and instantly proceed
                onSplashFinished()
            }
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background ambient circles
        Box(
            modifier = Modifier
                .size(360.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AcademicViolet.copy(alpha = glowAlpha * 0.4f),
                            PrimaryLight.copy(alpha = glowAlpha * 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Pill Tag
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { -40 })
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.10f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Next-Gen Smart School Management",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    }
                }
            }

            // Center Hero Logo & Title
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(900)) + scaleIn(initialScale = 0.8f, animationSpec = tween(900))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo Box with Glowing Rings
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        // Outer Pulsing Ring
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .scale(pulseScale)
                                .clip(RoundedCornerShape(36.dp))
                                .background(PrimaryLight.copy(alpha = glowAlpha * 0.35f))
                        )

                        // Core Logo Emblem
                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xFF38BDF8), AcademicViolet, Color(0xFF34D399))
                                )
                            ),
                            shadowElevation = 16.dp,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF1E3A8A),
                                                Color(0xFF312E81)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = "AcademiaTrack Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "AcademiaTrack",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Smart School ERP • CBT • Gemini AI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Feature Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val portalBadges = listOf("Admin", "Teacher", "Student", "Parent")
                        portalBadges.forEach { portal ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                Text(
                                    text = portal,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Loading Bar & Version Tag
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Loading Status Message
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = currentLoadingStage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "v2.5.0 • Powered by Google Gemini AI • Offline CBT Ready",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
