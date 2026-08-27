package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolRole
import com.example.data.model.SchoolUser
import com.example.service.gemini.GeminiChatMessage
import com.example.service.gemini.GeminiChatModels
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleAiAssistantScreen(
    viewModel: SchoolViewModel,
    currentUser: SchoolUser?,
    currentRole: SchoolRole,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.aiChatHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val selectedModel by viewModel.selectedAiModel.collectAsState()
    val isSearchGroundingEnabled by viewModel.isSearchGroundingEnabled.collectAsState()

    var userPrompt by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val listState = rememberLazyListState()

    val roleTitle = when (currentRole) {
        SchoolRole.ADMIN -> "Admin Executive AI Advisor"
        SchoolRole.TEACHER -> "Teacher's Pedagogical AI Assistant"
        SchoolRole.STUDENT -> "24/7 Personal Study & CBT AI Tutor"
        SchoolRole.PARENT -> "Parent School Liaison AI Advisor"
    }

    val roleSubtitle = when (currentRole) {
        SchoolRole.ADMIN -> "Draft official circulars, timetables, commendation letters & policy memos"
        SchoolRole.TEACHER -> "Create lesson notes, CBT exam questions, report card remarks & worksheets"
        SchoolRole.STUDENT -> "Step-by-step problem solver, practice CBT quizzes & study timetables"
        SchoolRole.PARENT -> "Home study routines, report card interpretation & progress tracking"
    }

    val roleThemeColor = when (currentRole) {
        SchoolRole.ADMIN -> Color(0xFF1E3A8A)
        SchoolRole.TEACHER -> Color(0xFF0F766E)
        SchoolRole.STUDENT -> PrimaryLight
        SchoolRole.PARENT -> Color(0xFF7C3AED)
    }

    val quickPrompts = when (currentRole) {
        SchoolRole.ADMIN -> listOf(
            "📢 Draft Official Parent Circular on Mid-Term Exams & PTA Meeting",
            "🗓️ Generate Academic Term Operations Timetable & Calendar",
            "📜 Write a Student Commendation & Academic Merit Letter",
            "📊 Summarize Staff Performance & CA Submission Status",
            "⚖️ Draft School CBT Exam Rules & Anti-Malpractice Policy"
        )
        SchoolRole.TEACHER -> listOf(
            "📝 Generate Complete Lesson Plan for Quadratic Equations (SS 2)",
            "🎯 Create 5 WAEC-Standard CBT Questions for Physics (Kinematics)",
            "✍️ Write Personalized Report Card Remarks for High-Performing Student",
            "💡 Build a Remedial Revision Worksheet for Chemistry Stoichiometry",
            "📋 Prepare English Literature Essay Questions & Marking Scheme"
        )
        SchoolRole.STUDENT -> listOf(
            "🧠 Explain Quadratic Formula derivation with step-by-step examples",
            "⚡ Generate a 3-Question Practice Quiz on Biology (Cell Organelles)",
            "🗓️ Create a 7-Day Balanced Study Timetable for upcoming exams",
            "📚 Step-by-step solution for calculating Electric Resistance and Ohm's Law",
            "✍️ Essay outline for 'The Role of Youth in National Development'"
        )
        SchoolRole.PARENT -> listOf(
            "📖 How can I help my child prepare effectively for secondary school CBT exams?",
            "📊 How do I interpret the Continuous Assessment (CA) vs Exam breakdown?",
            "💡 Recommended daily home study routine for SS 2 science students",
            "🤝 Constructive questions to ask class teachers during PTA meetings"
        )
    }

    // Scroll to the bottom whenever a new message or loading state changes
    LaunchedEffect(chatHistory.size, isAiLoading) {
        if (chatHistory.isNotEmpty() || isAiLoading) {
            val targetIndex = (chatHistory.size + if (isAiLoading) 1 else 0) - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = roleThemeColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = roleTitle,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = roleSubtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }

                if (chatHistory.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAiChatHistory() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = "Clear Chat History",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Model Switcher & Google Search Grounding Control Bar
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Models Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AI Model Engine:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val models = listOf(
                            GeminiChatModels.GEMINI_3_5_FLASH to "3.5 Flash",
                            GeminiChatModels.GEMINI_3_1_PRO to "3.1 Pro (Complex)",
                            GeminiChatModels.GEMINI_3_1_FLASH_LITE to "Flash Lite"
                        )

                        models.forEach { (modelKey, label) ->
                            val isSelected = selectedModel == modelKey
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) roleThemeColor else MaterialTheme.colorScheme.surface,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                                modifier = Modifier.clickable {
                                    viewModel.selectAiModel(modelKey)
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Google Search Grounding Toggle Bar
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
                            Icons.Rounded.TravelExplore,
                            contentDescription = null,
                            tint = if (isSearchGroundingEnabled) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Google Search Grounding",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FilterChip(
                        selected = isSearchGroundingEnabled,
                        onClick = { viewModel.toggleSearchGrounding(!isSearchGroundingEnabled) },
                        label = {
                            Text(
                                text = if (isSearchGroundingEnabled) "Enabled (Live Web)" else "Disabled",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            if (isSearchGroundingEnabled) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF16A34A).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF15803D),
                            selectedLeadingIconColor = Color(0xFF15803D)
                        )
                    )
                }
            }
        }

        // Quick Prompt Suggestions (shown when empty or as ideas)
        if (chatHistory.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Starters & Prompts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPrompts) { promptText ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, roleThemeColor.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable {
                                userPrompt = promptText
                                viewModel.sendAiChatMessage(promptText, currentRole)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = roleThemeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = promptText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Multi-Turn Chat Conversation Area
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (chatHistory.isEmpty() && !isAiLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(roleThemeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = roleThemeColor
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Start a Multi-Turn Chat with Gemini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ask any academic question, request lesson plans, or analyze study topics. Gemini maintains conversation context throughout your session.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatHistory, key = { it.id }) { message ->
                            ChatBubbleItem(
                                message = message,
                                roleThemeColor = roleThemeColor,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("AI Message", message.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                onOpenUrl = { url ->
                                    try {
                                        uriHandler.openUri(url)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        if (isAiLoading) {
                            item {
                                ThinkingBubbleItem(
                                    roleThemeColor = roleThemeColor,
                                    isSearchGrounded = isSearchGroundingEnabled
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = {
                    Text(
                        when (currentRole) {
                            SchoolRole.ADMIN -> "e.g. Write mid-term memo for parents..."
                            SchoolRole.TEACHER -> "e.g. Generate 5 CBT questions on Physics kinematics..."
                            SchoolRole.STUDENT -> "e.g. How do I solve 2x^2 + 5x - 3 = 0?..."
                            SchoolRole.PARENT -> "e.g. How can I support my child in chemistry?..."
                        },
                        fontSize = 12.5.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                maxLines = 3,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input")
            )

            IconButton(
                onClick = {
                    if (userPrompt.isNotBlank() && !isAiLoading) {
                        val textToSend = userPrompt
                        userPrompt = ""
                        viewModel.sendAiChatMessage(textToSend, currentRole)
                    }
                },
                enabled = userPrompt.isNotBlank() && !isAiLoading,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (userPrompt.isNotBlank() && !isAiLoading) roleThemeColor else Color.Gray.copy(alpha = 0.3f))
                    .testTag("send_ai_prompt_button")
            ) {
                Icon(
                    Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: GeminiChatMessage,
    roleThemeColor: Color,
    onCopy: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val isUser = message.role == "user"
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestampMillis) { timeFormatter.format(Date(message.timestampMillis)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(roleThemeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = roleThemeColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 310.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Header / Metadata for Model Bubble
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 3.dp)
                ) {
                    Text(
                        text = "Gemini AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = roleThemeColor
                    )

                    if (message.modelUsed != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = message.modelUsed.replace("-preview", ""),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (message.isSearchGrounded) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Icon(Icons.Rounded.TravelExplore, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(10.dp))
                                Text(
                                    text = "Search Grounded",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }

            // Bubble body
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) roleThemeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)) else null
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Search Grounding Citations / Web References
                    if (!isUser && message.searchSources.isNotEmpty()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        Text(
                            text = "🌐 Verified Web Sources & Citations:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            message.searchSources.take(3).forEach { source ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenUrl(source.url) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.OpenInNew,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = source.title.ifBlank { source.url },
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E3A8A),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Timestamp & Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 9.5.sp,
                            color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        if (!isUser) {
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingBubbleItem(
    roleThemeColor: Color,
    isSearchGrounded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(roleThemeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = roleThemeColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = roleThemeColor,
                    strokeWidth = 2.dp
                )
                Text(
                    text = if (isSearchGrounded) "Gemini is searching Google & generating response..." else "Gemini is thinking...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
