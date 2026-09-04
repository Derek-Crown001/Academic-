package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AppOwnerConsoleScreen
import com.example.ui.theme.AcademiaTrackTheme
import com.example.ui.viewmodel.SchoolViewModel

/**
 * Standalone App Owner Master Application.
 * Completely separated into an independent launcher activity for the platform owner/developer.
 * Manages remote school feature locks, license keys, broadcast invoices, and payment claims.
 */
class OwnerConsoleActivity : ComponentActivity() {
    private val viewModel: SchoolViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AcademiaTrackTheme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var pinInput by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf<String?>(null) }
                val licenseConfig by viewModel.licenseConfig.collectAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD97706)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Security,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "AcademiaTrack Owner Master",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                                            ) {
                                                Text(
                                                    text = "SAAS MASTER",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFFDE68A),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (isAuthenticated) "Controlling: ${licenseConfig.schoolName} (${if (licenseConfig.isAppLocked) "LOCKED" else "ACTIVE"})" else "Master Platform Control App",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (isAuthenticated) {
                                    // Quick Switch / Launch Main School App
                                    Button(
                                        onClick = {
                                            val intent = Intent(this@OwnerConsoleActivity, MainActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                            startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("launch_school_app_from_owner_header")
                                    ) {
                                        Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("School App", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Lock Console Button
                                    IconButton(
                                        onClick = {
                                            isAuthenticated = false
                                            pinInput = ""
                                        },
                                        modifier = Modifier.testTag("lock_owner_console_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = "Lock Console",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF0F172A)
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        if (!isAuthenticated) {
                            // Master PIN Gate
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                        )
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.6f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD97706).copy(alpha = 0.2f))
                                                .border(2.dp, Color(0xFFD97706), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "AcademiaTrack Owner Master",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "SaaS Platform Management & Remote Control",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF0F172A),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "CONNECTED TARGET SYSTEM",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFF59E0B)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = licenseConfig.schoolName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (licenseConfig.isAppLocked) Color(0xFFDC2626).copy(alpha = 0.3f) else Color(0xFF10B981).copy(alpha = 0.3f)
                                                    ) {
                                                        Text(
                                                            text = if (licenseConfig.isAppLocked) "LOCKED" else "ACTIVE",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (licenseConfig.isAppLocked) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = pinInput,
                                            onValueChange = {
                                                pinInput = it
                                                pinError = null
                                            },
                                            label = { Text("Master PIN (Default: 9999 or 0000)") },
                                            visualTransformation = PasswordVisualTransformation(),
                                            isError = pinError != null,
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color(0xFFD97706),
                                                unfocusedBorderColor = Color(0xFF475569),
                                                focusedLabelColor = Color(0xFFFBBF24),
                                                unfocusedLabelColor = Color(0xFF94A3B8)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        pinError?.let {
                                            Text(
                                                text = it,
                                                color = Color(0xFFEF4444),
                                                fontSize = 12.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (pinInput.trim() == "9999" || pinInput.trim() == "0000") {
                                                    isAuthenticated = true
                                                    pinError = null
                                                } else {
                                                    pinError = "Incorrect Master PIN. Access denied."
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Icon(Icons.Rounded.LockOpen, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Unlock Master Console", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }

                                        // Quick Link to Launch School App Directly
                                        TextButton(
                                            onClick = {
                                                val intent = Intent(this@OwnerConsoleActivity, MainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                                startActivity(intent)
                                            }
                                        ) {
                                            Icon(Icons.Rounded.School, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Switch to School App Client", color = Color(0xFF60A5FA), fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            AppOwnerConsoleScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

