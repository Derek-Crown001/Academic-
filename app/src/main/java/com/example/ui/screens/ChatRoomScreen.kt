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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.SchoolRole
import com.example.data.model.SchoolUser
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChannelId by viewModel.activeChatChannelId.collectAsState()
    val allRooms by viewModel.allChatRooms.collectAsState()
    val activeRoom by viewModel.activeChatRoom.collectAsState()
    val rawMessages by viewModel.currentChatMessages.collectAsState()
    val replyingToMessage by viewModel.replyingToMessage.collectAsState()
    val searchQuery by viewModel.chatSearchQuery.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()
    var messageInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "CLASS", "STAFF", "STEM", "PEER"
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showRoomSettingsDialog by remember { mutableStateOf(false) }
    var showSearchField by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: SchoolRole.STUDENT
    val isStaff = userRole == SchoolRole.ADMIN || userRole == SchoolRole.TEACHER
    val isStudent = userRole == SchoolRole.STUDENT

    // Filter rooms by permissions and category
    val accessibleRooms = remember(allRooms, userRole, selectedCategory) {
        allRooms.filter { room ->
            val roleAllowed = when (room.allowedRoles) {
                "STAFF" -> isStaff
                "STUDENTS" -> isStudent || isStaff
                else -> true
            }
            val categoryMatches = when (selectedCategory) {
                "CLASS" -> room.topic == "Official Class"
                "STAFF" -> room.topic == "Staff Only"
                "STEM" -> room.topic == "STEM Hub" || room.topic == "Science & Lab"
                "PEER" -> room.topic == "Peer Study" || room.topic == "Humanities"
                else -> true
            }
            roleAllowed && categoryMatches
        }
    }

    // Filter messages by search query
    val filteredMessages = remember(rawMessages, searchQuery) {
        if (searchQuery.isBlank()) rawMessages
        else rawMessages.filter {
            it.message.contains(searchQuery, ignoreCase = true) ||
            it.senderName.contains(searchQuery, ignoreCase = true)
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. Top Channel & Management Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Room Header Info & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (activeRoom?.topic) {
                                        "Staff Only" -> Color(0xFF1E3A8A)
                                        "Official Class" -> Color(0xFF0F766E)
                                        "STEM Hub" -> Color(0xFFEA580C)
                                        "Peer Study" -> Color(0xFF7C3AED)
                                        else -> PrimaryLight
                                    }.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activeRoom?.topic) {
                                    "Staff Only" -> Icons.Rounded.Groups
                                    "Official Class" -> Icons.Rounded.Class
                                    "STEM Hub" -> Icons.Rounded.Calculate
                                    "Peer Study" -> Icons.Rounded.Lightbulb
                                    else -> Icons.Rounded.Forum
                                },
                                contentDescription = null,
                                tint = when (activeRoom?.topic) {
                                    "Staff Only" -> Color(0xFF1E3A8A)
                                    "Official Class" -> Color(0xFF0F766E)
                                    "STEM Hub" -> Color(0xFFEA580C)
                                    "Peer Study" -> Color(0xFF7C3AED)
                                    else -> PrimaryLight
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = activeRoom?.title ?: "Communication Hub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (activeRoom?.isMutedForStudents == true) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AcademicRose.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.VolumeOff,
                                                contentDescription = null,
                                                tint = AcademicRose,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = "Announcements Only",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AcademicRose
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "${activeRoom?.topic ?: "General"} • ${activeRoom?.memberCount ?: 32} Members • Firestore Live",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Top Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                showSearchField = !showSearchField
                                if (!showSearchField) viewModel.setChatSearchQuery("")
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (showSearchField) Icons.Rounded.SearchOff else Icons.Rounded.Search,
                                contentDescription = "Search messages",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isStaff) {
                            IconButton(
                                onClick = { showRoomSettingsDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Room Settings & Moderation",
                                    tint = PrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { showCreateRoomDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddComment,
                                    contentDescription = "Create Room",
                                    tint = PrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Expandable Message Search Bar
                AnimatedVisibility(
                    visible = showSearchField,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setChatSearchQuery(it) },
                        placeholder = { Text("Search messages or sender...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setChatSearchQuery("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf(
                        "ALL" to "All Rooms",
                        "CLASS" to "Classrooms",
                        "STAFF" to "Staff Room",
                        "STEM" to "STEM Hubs",
                        "PEER" to "Peer Study"
                    )

                    items(categories) { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catKey },
                            label = { Text(catLabel, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryLight.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryLight
                            )
                        )
                    }
                }

                // Room Switcher Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accessibleRooms) { room ->
                        val isSelected = activeChannelId == room.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) else null,
                            modifier = Modifier
                                .clickable { viewModel.setChatChannel(room.id) }
                                .testTag("room_chip_${room.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (room.topic) {
                                        "Staff Only" -> Icons.Rounded.Groups
                                        "Official Class" -> Icons.Rounded.Class
                                        "STEM Hub" -> Icons.Rounded.Calculate
                                        "Peer Study" -> Icons.Rounded.Lightbulb
                                        else -> Icons.Rounded.Forum
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = room.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (room.isMutedForStudents) {
                                    Icon(
                                        Icons.Rounded.VolumeOff,
                                        contentDescription = "Muted for students",
                                        tint = if (isSelected) Color.White.copy(alpha = 0.8f) else AcademicRose,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Pinned Official Notice Banner (if any) ---
        activeRoom?.pinnedNotice?.takeIf { it.isNotBlank() }?.let { notice ->
            Surface(
                color = Color(0xFFFEF3C7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = "Pinned Notice",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = notice,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E),
                            lineHeight = 15.sp
                        )
                        activeRoom?.pinnedBy?.let { author ->
                            Text(
                                text = "Pinned by: $author",
                                fontSize = 9.5.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    if (isStaff) {
                        IconButton(
                            onClick = {
                                activeRoom?.let { r -> viewModel.updateRoomPinnedNotice(r.id, null) }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Unpin notice",
                                tint = Color(0xFF92400E),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Messages List Area ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredMessages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No messages matching \"$searchQuery\"" else "No messages yet in this room",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Be the first to share an academic question, insight, or announcement!",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(filteredMessages, key = { it.id }) { msg ->
                val isMyMessage = msg.senderId == currentUser?.id
                ModeratedChatMessageCard(
                    message = msg,
                    isMyMessage = isMyMessage,
                    isStaff = isStaff,
                    isAdmin = userRole == SchoolRole.ADMIN,
                    onReply = { viewModel.setReplyingToMessage(msg) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Chat Message", msg.message)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onTogglePin = { viewModel.togglePinChatMessage(msg.id, !msg.isPinned) },
                    onModerateWithReason = { reason -> viewModel.moderateChatMessageWithReason(msg.id, reason) },
                    onUnmoderate = { viewModel.unmoderateChatMessage(msg.id) },
                    onDeletePermanently = { viewModel.deleteChatMessagePermanently(msg.id) }
                )
            }
        }

        // --- 4. Quoted Reply Snippet (if replying) ---
        replyingToMessage?.let { replyTarget ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Rounded.Reply,
                            contentDescription = null,
                            tint = PrimaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Replying to ${replyTarget.senderName}:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryLight
                            )
                            Text(
                                text = replyTarget.message,
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.setReplyingToMessage(null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Cancel reply",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // --- 5. Message Input & Controls Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            val isMutedForCurrentStudent = isStudent && activeRoom?.isMutedForStudents == true

            if (isMutedForCurrentStudent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.VolumeOff,
                        contentDescription = null,
                        tint = AcademicRose,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Announcement Mode: Only Teachers and Administrators can post in this room.",
                        fontSize = 11.5.sp,
                        color = AcademicRose,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
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
                        placeholder = {
                            Text(
                                text = "Message #${activeRoom?.title ?: "Room"} as ${currentUser?.name ?: "User"}...",
                                fontSize = 12.5.sp
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                viewModel.sendChatMessage(messageInput)
                                messageInput = ""
                            }
                        },
                        enabled = messageInput.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (messageInput.isNotBlank()) PrimaryLight else Color.Gray.copy(alpha = 0.3f))
                            .testTag("send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // --- Create Room Dialog ---
    if (showCreateRoomDialog && isStaff) {
        CreateChatRoomDialog(
            currentUser = currentUser,
            onDismiss = { showCreateRoomDialog = false },
            onCreateRoom = { title, description, topic, targetClass, allowedRoles, isModerated, isMutedForStudents, pinnedNotice ->
                viewModel.createChatRoom(
                    title = title,
                    description = description,
                    topic = topic,
                    targetClass = targetClass,
                    allowedRoles = allowedRoles,
                    isModerated = isModerated,
                    isMutedForStudents = isMutedForStudents,
                    pinnedNotice = pinnedNotice
                )
                showCreateRoomDialog = false
            }
        )
    }

    // --- Room Settings & Moderation Dialog ---
    if (showRoomSettingsDialog && isStaff && activeRoom != null) {
        RoomModerationSettingsDialog(
            room = activeRoom!!,
            isAdmin = userRole == SchoolRole.ADMIN,
            onDismiss = { showRoomSettingsDialog = false },
            onToggleStudentMute = { isMuted ->
                viewModel.toggleRoomStudentMute(activeRoom!!.id, isMuted)
            },
            onUpdatePinnedNotice = { notice ->
                viewModel.updateRoomPinnedNotice(activeRoom!!.id, notice)
            },
            onDeleteRoom = {
                viewModel.deleteChatRoom(activeRoom!!.id)
                showRoomSettingsDialog = false
            }
        )
    }
}

@Composable
fun ModeratedChatMessageCard(
    message: ChatMessage,
    isMyMessage: Boolean,
    isStaff: Boolean,
    isAdmin: Boolean,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    onModerateWithReason: (String) -> Unit,
    onUnmoderate: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReasonDialog by remember { mutableStateOf(false) }

    val alignment = if (isMyMessage) Alignment.End else Alignment.Start
    val bgBubbleColor = when {
        message.isModerated -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
        // Sender info & badges
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Sender Avatar Circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(message.senderAvatarColor))
                        } catch (e: Exception) {
                            PrimaryLight
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = message.senderName,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            RoleBadge(role = message.senderRole)

            if (message.isPinned) {
                Icon(
                    Icons.Rounded.PushPin,
                    contentDescription = "Pinned",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(12.dp)
                )
            }

            val timeStr = remember(message.timestampMillis) {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestampMillis))
            }
            Text(
                text = timeStr,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Bubble Box with Long-Click & Actions
        Box {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMyMessage) 16.dp else 4.dp,
                    bottomEnd = if (isMyMessage) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bgBubbleColor),
                border = if (!isMyMessage) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clickable { showMenu = true }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quoted message snippet if reply
                    if (message.replyToSender != null && message.replyToText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMyMessage) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(24.dp)
                                        .background(if (isMyMessage) Color.White else PrimaryLight)
                                )
                                Column {
                                    Text(
                                        text = message.replyToSender,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMyMessage) Color.White else PrimaryLight
                                    )
                                    Text(
                                        text = message.replyToText,
                                        fontSize = 9.5.sp,
                                        color = if (isMyMessage) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Message Content or Moderated Placeholder
                    if (message.isModerated) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                                    text = "Message removed by moderator",
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicRose
                                )
                            }
                            message.moderationReason?.let { reason ->
                                Text(
                                    text = "Reason: $reason",
                                    fontSize = 10.5.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isStaff) {
                                Text(
                                    text = "Original: \"${message.message}\"",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        SelectionContainer {
                            Text(
                                text = message.message,
                                fontSize = 13.sp,
                                color = textColor,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Message Context Dropdown Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Reply", fontSize = 13.sp) },
                    onClick = {
                        onReply()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )

                DropdownMenuItem(
                    text = { Text("Copy Text", fontSize = 13.sp) },
                    onClick = {
                        onCopy()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )

                if (isStaff) {
                    DropdownMenuItem(
                        text = { Text(if (message.isPinned) "Unpin Message" else "Pin to Top", fontSize = 13.sp) },
                        onClick = {
                            onTogglePin()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )

                    if (!message.isModerated) {
                        DropdownMenuItem(
                            text = { Text("Moderate / Hide Message", color = AcademicRose, fontSize = 13.sp) },
                            onClick = {
                                showReasonDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Block, contentDescription = null, tint = AcademicRose, modifier = Modifier.size(16.dp))
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Restore / Unmoderate Message", color = AcademicEmerald, fontSize = 13.sp) },
                            onClick = {
                                onUnmoderate()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Restore, contentDescription = null, tint = AcademicEmerald, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }

                if (isAdmin) {
                    DropdownMenuItem(
                        text = { Text("Delete Permanently", color = AcademicRose, fontSize = 13.sp) },
                        onClick = {
                            onDeletePermanently()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = AcademicRose, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }
    }

    // Moderation Reason Picker Dialog
    if (showReasonDialog) {
        AlertDialog(
            onDismissRequest = { showReasonDialog = false },
            icon = { Icon(Icons.Rounded.Shield, contentDescription = null, tint = AcademicRose) },
            title = { Text("Select Moderation Reason", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose the academic/disciplinary reason for hiding this message from the channel:", fontSize = 12.sp)

                    val reasons = listOf(
                        "Off-topic / Non-academic conversation",
                        "Inappropriate language or conduct",
                        "Exam or CBT assessment malpractice",
                        "Spamming or promotional content",
                        "Violating teacher classroom guidelines"
                    )

                    reasons.forEach { reason ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onModerateWithReason(reason)
                                    showReasonDialog = false
                                }
                        ) {
                            Text(
                                text = reason,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReasonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatRoomDialog(
    currentUser: SchoolUser?,
    onDismiss: () -> Unit,
    onCreateRoom: (
        title: String,
        description: String,
        topic: String,
        targetClass: String,
        allowedRoles: String,
        isModerated: Boolean,
        isMutedForStudents: Boolean,
        pinnedNotice: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("Official Class") }
    var targetClass by remember { mutableStateOf("SS 2 Gold") }
    var allowedRoles by remember { mutableStateOf("ALL") }
    var isModerated by remember { mutableStateOf(true) }
    var isMutedForStudents by remember { mutableStateOf(false) }
    var pinnedNotice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.AddComment, contentDescription = null, tint = PrimaryLight)
                Text("Create Moderated Chat Room", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Room Title", fontSize = 12.sp) },
                    placeholder = { Text("e.g. SS 2 Physics Lab & Tutorials") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Purpose / Description", fontSize = 12.sp) },
                    placeholder = { Text("Discussion forum for SS 2 kinematics...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Topic Selector
                Text("Room Topic / Category:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                val topics = listOf("Official Class", "Staff Only", "STEM Hub", "Peer Study", "Humanities")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(topics) { t ->
                        FilterChip(
                            selected = topic == t,
                            onClick = { topic = t },
                            label = { Text(t, fontSize = 10.5.sp) }
                        )
                    }
                }

                // Target Class
                OutlinedTextField(
                    value = targetClass,
                    onValueChange = { targetClass = it },
                    label = { Text("Target Class (or ALL)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Pinned Notice (Optional)
                OutlinedTextField(
                    value = pinnedNotice,
                    onValueChange = { pinnedNotice = it },
                    label = { Text("Initial Pinned Notice (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Weekly assignment submission deadline...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Student Mute Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Announcement Mode (Students Read-Only)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("When enabled, only staff can post messages", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isMutedForStudents,
                        onCheckedChange = { isMutedForStudents = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateRoom(
                            title,
                            description,
                            topic,
                            targetClass,
                            allowedRoles,
                            isModerated,
                            isMutedForStudents,
                            pinnedNotice.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RoomModerationSettingsDialog(
    room: ChatRoom,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onToggleStudentMute: (Boolean) -> Unit,
    onUpdatePinnedNotice: (String?) -> Unit,
    onDeleteRoom: () -> Unit
) {
    var isMuted by remember { mutableStateOf(room.isMutedForStudents) }
    var pinnedText by remember { mutableStateOf(room.pinnedNotice ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Tune, contentDescription = null, tint = PrimaryLight) },
        title = { Text("Room Moderation & Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Manage permissions, student posting access, and official notice for #${room.title}", fontSize = 12.sp)

                // Student Mute Switch
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Announcement Mode", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Text(
                                text = if (isMuted) "Students cannot post (Read-Only)" else "Students can chat & ask questions",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isMuted,
                            onCheckedChange = {
                                isMuted = it
                                onToggleStudentMute(it)
                            }
                        )
                    }
                }

                // Pinned Notice Field
                OutlinedTextField(
                    value = pinnedText,
                    onValueChange = { pinnedText = it },
                    label = { Text("Pinned Official Notice Banner", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Mid-term timetable released...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onUpdatePinnedNotice(pinnedText.takeIf { it.isNotBlank() })
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Update Pinned Notice")
                }

                // Delete Room Button
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AcademicRose),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AcademicRose.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Room")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Room #${room.title}?", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove this chat room and its message history from local storage and Firestore.", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRoom()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicRose)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
