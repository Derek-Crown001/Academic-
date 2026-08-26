package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.SchoolRole
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatRoomScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChannelId by viewModel.activeChatChannelId.collectAsState()
    val messages by viewModel.currentChatMessages.collectAsState()

    val listState = rememberLazyListState()
    var messageInput by remember { mutableStateOf("") }

    val userRole = currentUser?.role ?: SchoolRole.STUDENT
    val canModerate = userRole == SchoolRole.ADMIN || userRole == SchoolRole.TEACHER

    // Channels available depending on role
    val availableChannels = when (userRole) {
        SchoolRole.ADMIN -> listOf(
            "STAFF_GENERAL" to "Staff General Room",
            "CLASS_SS2_GOLD" to "SS 2 Gold Class Room",
            "CLASS_SS3_SCIENCE" to "SS 3 Science Class Room"
        )
        SchoolRole.TEACHER -> listOf(
            "STAFF_GENERAL" to "Staff General Room",
            "CLASS_SS2_GOLD" to "SS 2 Gold Class Room"
        )
        SchoolRole.STUDENT -> listOf(
            "CLASS_SS2_GOLD" to "SS 2 Gold Class Room"
        )
        SchoolRole.PARENT -> listOf(
            "CLASS_SS2_GOLD" to "SS 2 Gold Class Room"
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Channel Selector Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableChannels.forEach { (channelId, channelTitle) ->
                        val isSelected = activeChannelId == channelId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setChatChannel(channelId) },
                            label = {
                                Text(
                                    text = channelTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (channelId.startsWith("STAFF")) Icons.Rounded.Groups else Icons.Rounded.Class,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                if (canModerate) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = AcademicEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Moderation Mode Active (${userRole.name}): You can moderate/remove messages.",
                            fontSize = 10.5.sp,
                            color = AcademicEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages yet in this room. Start the conversation!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(messages) { msg ->
                val isMyMessage = msg.senderId == currentUser?.id
                ChatMessageItem(
                    message = msg,
                    isMyMessage = isMyMessage,
                    canModerate = canModerate,
                    onModerate = { viewModel.moderateChatMessage(msg.id) },
                    onDeletePermanently = { viewModel.deleteChatMessagePermanently(msg.id) }
                )
            }
        }

        // Input Field Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Message room as ${currentUser?.name ?: "User"}...") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            viewModel.sendChatMessage(messageInput)
                            messageInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PrimaryLight)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isMyMessage: Boolean,
    canModerate: Boolean,
    onModerate: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val alignment = if (isMyMessage) Alignment.End else Alignment.Start
    val bgBubbleColor = when {
        message.isModerated -> MaterialTheme.colorScheme.surfaceVariant
        isMyMessage -> PrimaryLight
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        message.isModerated -> MaterialTheme.colorScheme.onSurfaceVariant
        isMyMessage -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = message.senderName,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            RoleBadge(role = message.senderRole)

            val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date(message.timestampMillis))
            Text(
                text = timeStr,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMyMessage) 16.dp else 4.dp,
                    bottomEnd = if (isMyMessage) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bgBubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clickable(enabled = canModerate) {
                        if (canModerate) showMenu = true
                    }
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (message.isModerated) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = AcademicRose,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Message removed by moderator (${message.deletedBy ?: "Moderator"})",
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.5.sp,
                                color = textColor
                            )
                        }
                    } else {
                        Text(
                            text = message.message,
                            fontSize = 13.5.sp,
                            color = textColor,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (canModerate) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!message.isModerated) {
                        DropdownMenuItem(
                            text = { Text("Moderate / Hide Message", color = AcademicRose) },
                            onClick = {
                                onModerate()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Block, contentDescription = null, tint = AcademicRose)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete Permanently") },
                        onClick = {
                            onDeletePermanently()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
