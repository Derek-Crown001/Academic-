package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.owner.ui.components.OwnerThemeColors
import com.example.ui.theme.*

/**
 * Dedicated Dialog & Distribution Hub for downloading the Platform Owner Master APK
 * and School Client APK separately from GitHub Releases.
 */
@Composable
fun OwnerApkDownloadDialog(
    onDismiss: () -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var repoOwnerAndName by remember { mutableStateOf("derekcrown001/AcademiaTrack") }
    var targetOwnerRepo by remember { mutableStateOf("derekcrown001/AcademiaTrack-Owner") }
    var isEditingRepo by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Download APKs, 1 = Push to Separate Repo

    val githubReleasesUrl = "https://github.com/${repoOwnerAndName.trim()}/releases"
    val githubLatestReleaseUrl = "https://github.com/${repoOwnerAndName.trim()}/releases/latest"
    val ownerApkDirectUrl = "https://github.com/${repoOwnerAndName.trim()}/releases/latest/download/AcademiaTrack-Owner-Master.apk"
    val schoolApkDirectUrl = "https://github.com/${repoOwnerAndName.trim()}/releases/latest/download/AcademiaTrack-School-Client.apk"
    val githubWorkflowUrl = "https://github.com/${repoOwnerAndName.trim()}/actions/workflows/push-owner-app.yml"
    val githubNewRepoUrl = "https://github.com/new"
    val cliPushCommand = "bash scripts/push-owner-repo.sh https://github.com/${targetOwnerRepo.trim()}.git"
    val cliSshPushCommand = "bash scripts/push-owner-repo.sh git@github.com:${targetOwnerRepo.trim()}.git"
    val cliExportOnlyCommand = "bash scripts/push-owner-repo.sh --export-only ./standalone-owner-app"

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onShowToast("Opening GitHub Releases...")
        } catch (e: Exception) {
            onShowToast("Could not open browser: ${e.message}")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Download APKs on GitHub",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Separate Owner & School Binaries",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dual Tab Navigation: Download APKs vs Push to Separate Repo
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFF59E0B),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFF59E0B)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Download APKs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        selectedContentColor = Color(0xFFF59E0B),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.CallSplit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Push to Separate Repo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        selectedContentColor = Color(0xFFF59E0B),
                        unselectedContentColor = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        // TAB 0: DOWNLOAD APKS
                        // Summary Notice
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Dual Standalone Android Distributions",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Text(
                                text = "AcademiaTrack produces two distinct APK packages: the Owner Master Console (for SaaS governance and remote control) and the School Client App (for Students, Teachers, and Admins). Both are published separately on GitHub.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Card 1: Platform Owner Master APK (Primary Target)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Owner Master APK",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "AcademiaTrack-Owner-Master.apk",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "SUPER-ADMIN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFBBF24),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Includes multi-tenant school onboarding, master killswitch, license key generator, subscription billing, and real-time remote telemetry switchboard.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )

                            Button(
                                onClick = { openUrl(githubReleasesUrl) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("download_owner_apk_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Download Owner APK on GitHub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Card 2: School Client APK
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.School,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "School Client APK",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "AcademiaTrack-School-Client.apk",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "CLIENT APP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Standard app for Principals, Teachers, Students, and Parents. Contains offline CBT exam player, report card generation, and grading.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )

                            OutlinedButton(
                                onClick = { openUrl(githubReleasesUrl) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("download_school_apk_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Download School Client APK",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }

                    // GitHub Repository & Release Options
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Code,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "GitHub Repository Releases",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }

                                TextButton(
                                    onClick = { isEditingRepo = !isEditingRepo },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isEditingRepo) "Done" else "Change Repo",
                                        fontSize = 11.sp,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }

                            if (isEditingRepo) {
                                OutlinedTextField(
                                    value = repoOwnerAndName,
                                    onValueChange = { repoOwnerAndName = it },
                                    label = { Text("GitHub Repo (owner/repository)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFF59E0B),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = githubReleasesUrl,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { openUrl(githubReleasesUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.OpenInBrowser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Releases", fontSize = 11.sp, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(githubReleasesUrl))
                                        onShowToast("GitHub Releases URL copied to clipboard!")
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy URL", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Automated CI/CD Workflow explanation
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "🤖 Automatic GitHub Actions Build",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Every push to main automatically triggers '.github/workflows/build-apk.yml', creating a GitHub Release containing both 'AcademiaTrack-Owner-Master.apk' and 'AcademiaTrack-School-Client.apk' ready for immediate over-the-air installation.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 14.sp
                            )
                        }
                    }
                    } else {
                        // TAB 1: PUSH TO SEPARATE REPOSITORY
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CallSplit,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Independent Owner Master Repository",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Extract and isolate the executive Platform Owner application into a standalone GitHub repository (e.g. 'AcademiaTrack-Owner'). Client schools only receive the school app, while you retain exclusive ownership of the master control plane.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Target Repository Input
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Target Repository on GitHub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )

                                OutlinedTextField(
                                    value = targetOwnerRepo,
                                    onValueChange = { targetOwnerRepo = it },
                                    label = { Text("Target GitHub Repo (owner/name)", fontSize = 11.sp) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFF59E0B),
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedLabelColor = Color(0xFFF59E0B),
                                        cursorColor = Color(0xFFF59E0B)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = { openUrl(githubNewRepoUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("1. Create Empty Target Repo on GitHub", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }

                        // Method 1: GitHub Actions CI/CD Workflow
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Text("Method 1: GitHub Actions Auto-Push (Zero CLI)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                }

                                Text(
                                    text = "This repository includes an automated workflow ('.github/workflows/push-owner-app.yml') that packages the standalone Owner app and pushes directly to your separate repository.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 15.sp
                                )

                                Button(
                                    onClick = { openUrl(githubWorkflowUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Trigger Push Workflow in GitHub Actions", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("PAT Secret Requirement", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF59E0B))
                                            Text("Set secret 'OWNER_REPO_PAT' (with 'repo' scope) in Settings -> Secrets -> Actions.", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                clipboard.setText(AnnotatedString(
                                                    "1. Go to https://github.com/settings/tokens (classic)\n" +
                                                    "2. Generate token with 'repo' scope\n" +
                                                    "3. In this repo -> Settings -> Secrets and variables -> Actions\n" +
                                                    "4. Add new secret named OWNER_REPO_PAT and paste token"
                                                ))
                                                onShowToast("Token guide copied to clipboard!")
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Copy Guide", fontSize = 10.sp, color = Color(0xFFF59E0B))
                                        }
                                    }
                                }
                            }
                        }

                        // Method 2: Terminal / CLI Script
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    Text("Method 2: One-Command Terminal Push", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                }

                                Text(
                                    text = "Run this single command in your project terminal to assemble the standalone Owner app and push immediately:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = cliPushCommand,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(cliPushCommand))
                                            onShowToast("Terminal HTTPS command copied!")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy HTTPS Command", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(cliSshPushCommand))
                                            onShowToast("Terminal SSH command copied!")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy SSH Command", fontSize = 11.sp, color = Color(0xFFF59E0B))
                                    }
                                }
                            }
                        }

                        // Method 3: Local Export Only
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Local Project Export (Offline / Inspection)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Export to a local standalone folder without pushing to git:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = cliExportOnlyCommand,
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(cliExportOnlyCommand))
                                        onShowToast("Export command copied!")
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Copy Export Command", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        // Specifications Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📋 Standalone Repository Specifications", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF38BDF8))
                                Text("• Application ID: com.aistudio.academiatrack.owner", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                                Text("• Launcher Activity: com.example.owner.OwnerMainActivity", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                                Text("• Remote Sync: OwnerRemoteSyncService & Cloud Firestore Listener", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                                Text("• Autonomous CI/CD: Builds and releases 'AcademiaTrack-Owner-Master.apk' automatically on every push to that repository.", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Dismiss Action
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Close Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}
