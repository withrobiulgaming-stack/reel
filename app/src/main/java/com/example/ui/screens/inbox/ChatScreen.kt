package com.example.ui.screens.inbox

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.MessageEntity
import com.example.data.local.UserEntity
import com.example.data.repository.ShortlyRepository
import com.example.domain.AppLanguage
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Facebook Messenger signature colors & gradients
val MessengerBlue = Color(0xFF0084FF)
val MessengerLightBlue = Color(0xFF00C6FF)
val MessengerPurple = Color(0xFFA824F4)
val MessengerPink = Color(0xFFFF4B91)
val MessengerBubbleGradient = Brush.linearGradient(listOf(MessengerBlue, MessengerPurple, MessengerPink))
val MessengerDarkBubble = Color(0xFF242526)
val MessengerActiveGreen = Color(0xFF31A24C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    recipientUser: UserEntity,
    repository: ShortlyRepository,
    language: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    val messages by repository.getChatMessages(recipientUser.id).collectAsState(initial = emptyList())

    // Messenger Calling state
    var activeCallType by remember { mutableStateOf<String?>(null) } // "AUDIO" or "VIDEO" or null
    var showInfoSheet by remember { mutableStateOf(false) }
    var showActionTray by remember { mutableStateOf(false) }
    var selectedMessageForReaction by remember { mutableStateOf<MessageEntity?>(null) }

    // Audio recording simulation state
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingTimerSec by remember { mutableStateOf(0) }

    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingTimerSec = 0
            while (isRecordingAudio) {
                delay(1000L)
                recordingTimerSec++
            }
        }
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showInfoSheet = true }
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(recipientUser.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = recipientUser.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            // Facebook Messenger Active Green Dot
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(MessengerActiveGreen, CircleShape)
                                    .background(Color.Black, RoundedCornerShape(2.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = recipientUser.displayName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MessengerActiveGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "এখন সক্রিয়" else "Active now",
                                    color = MessengerActiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Facebook Messenger Audio Call button
                    IconButton(onClick = { activeCallType = "AUDIO" }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Voice Call",
                            tint = MessengerBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Facebook Messenger Video Call button
                    IconButton(onClick = { activeCallType = "VIDEO" }) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = MessengerBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Messenger Info icon
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Conversation Details",
                            tint = MessengerBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShortlySurfaceDark)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShortlySurfaceDark)
                    .imePadding()
            ) {
                // Expandable Messenger Actions Tray
                AnimatedVisibility(visible = showActionTray) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MessengerActionPill(icon = Icons.Default.Image, label = "Photo", tint = Color(0xFF00E676)) {
                            coroutineScope.launch {
                                repository.sendMessage(
                                    otherUserId = recipientUser.id,
                                    text = "ছবি পাঠানো হয়েছে 📷",
                                    messageType = "IMAGE",
                                    mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80"
                                )
                            }
                            showActionTray = false
                            Toast.makeText(context, "ছবি শেয়ার করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                        MessengerActionPill(icon = Icons.Default.LocationOn, label = "Location", tint = Color(0xFFFF3B30)) {
                            coroutineScope.launch {
                                repository.sendMessage(
                                    otherUserId = recipientUser.id,
                                    text = "📍 ধানমন্ডি লেক, ঢাকা থেকে লাইভ লোকেশন শেয়ার করা হয়েছে।"
                                )
                            }
                            showActionTray = false
                        }
                        MessengerActionPill(icon = Icons.Default.CardGiftcard, label = "Gift", tint = ShortlyPrimary) {
                            coroutineScope.launch {
                                repository.sendMessage(
                                    otherUserId = recipientUser.id,
                                    text = "🎁 আপনার জন্য একটি ক্রিয়েটর গিফট বক্স পাঠানো হয়েছে!"
                                )
                            }
                            showActionTray = false
                        }
                        MessengerActionPill(icon = Icons.Default.Mic, label = "Voice Note", tint = MessengerPurple) {
                            isRecordingAudio = true
                            showActionTray = false
                        }
                    }
                }

                if (isRecordingAudio) {
                    // Audio Recording Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isRecordingAudio = false },
                            modifier = Modifier
                                .background(Color(0xFFFF3B30).copy(alpha = 0.2f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFFFF3B30), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFFF3B30), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ভয়েস রেকর্ড হচ্ছে... 0:${if (recordingTimerSec < 10) "0$recordingTimerSec" else "$recordingTimerSec"}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val duration = if (recordingTimerSec < 1) 3 else recordingTimerSec
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        otherUserId = recipientUser.id,
                                        text = "ভয়েস মেসেজ (${duration}s)",
                                        messageType = "VOICE",
                                        voiceDurationSec = duration
                                    )
                                }
                                isRecordingAudio = false
                                Toast.makeText(context, "ভয়েস মেসেজ পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .background(MessengerBlue, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    // Standard Facebook Messenger Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Messenger (+) Plus icon
                        IconButton(
                            onClick = { showActionTray = !showActionTray },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (showActionTray) Icons.Default.Close else Icons.Default.AddCircle,
                                contentDescription = "More actions",
                                tint = MessengerBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Camera icon
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        otherUserId = recipientUser.id,
                                        text = "📷 ইনস্ট্যান্ট ক্যামেরা ফটো",
                                        messageType = "IMAGE",
                                        mediaUrl = "https://images.unsplash.com/photo-1547153760-18fc86324498?w=800&auto=format&fit=crop&q=80"
                                    )
                                }
                                Toast.makeText(context, "ক্যামেরা ফটো পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = MessengerBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Gallery Image icon
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        otherUserId = recipientUser.id,
                                        text = "গ্যালারি ছবি 🌄",
                                        messageType = "IMAGE",
                                        mediaUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80"
                                    )
                                }
                                Toast.makeText(context, "ছবি পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photos",
                                tint = MessengerBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Messenger Rounded Input Pill
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "মেসেজ লিখুন..." else "Message...",
                                    color = ShortlyTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ShortlySurfaceLight,
                                unfocusedContainerColor = ShortlySurfaceLight,
                                focusedBorderColor = MessengerBlue,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 4
                        )

                        // Messenger Action: Thumbs Up (👍) or Send plane
                        if (messageInput.isBlank()) {
                            // Iconic Facebook Messenger Thumbs Up (👍)
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            otherUserId = recipientUser.id,
                                            text = "👍",
                                            messageType = "LIKE_STICKER"
                                        )
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text(
                                    text = "👍",
                                    fontSize = 24.sp,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            repository.sendMessage(
                                                otherUserId = recipientUser.id,
                                                text = "👍",
                                                messageType = "LIKE_STICKER"
                                            )
                                        }
                                    }
                                )
                            }
                        } else {
                            // Send Plane Button
                            IconButton(
                                onClick = {
                                    val text = messageInput.trim()
                                    if (text.isNotBlank()) {
                                        coroutineScope.launch {
                                            repository.sendMessage(
                                                otherUserId = recipientUser.id,
                                                text = text
                                            )
                                        }
                                        messageInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .background(MessengerBlue, CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Black,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Messenger Header Info Item inside chat
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(recipientUser.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = recipientUser.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recipientUser.displayName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${recipientUser.username}",
                            color = ShortlyTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "আপনি Shortly মেসেঞ্জারে সংযুক্ত আছেন" else "You're connected on Shortly Messenger",
                            color = ShortlyTextSecondary.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isMe = message.senderId == repository.currentUserId
                    val isLastMessage = messages.lastOrNull()?.id == message.id

                    MessengerMessageBubble(
                        message = message,
                        isMe = isMe,
                        recipientAvatar = recipientUser.avatarUrl,
                        isLastMessage = isLastMessage,
                        onLongClick = {
                            selectedMessageForReaction = message
                        },
                        onPlayVoice = {
                            Toast.makeText(context, "ভয়েস মেসেজ প্লে হচ্ছে... ▶", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Messenger Floating Emoji Reaction Bar
            if (selectedMessageForReaction != null) {
                MessengerReactionBar(
                    onSelectReaction = { emoji ->
                        coroutineScope.launch {
                            repository.reactToMessage(selectedMessageForReaction!!.id, emoji)
                        }
                        selectedMessageForReaction = null
                    },
                    onDismiss = { selectedMessageForReaction = null }
                )
            }
        }
    }

    // Messenger Calling Screen (Voice or Video Call)
    if (activeCallType != null) {
        MessengerCallDialog(
            recipientUser = recipientUser,
            callType = activeCallType!!,
            onEndCall = { activeCallType = null }
        )
    }

    // Messenger Conversation Info Sheet
    if (showInfoSheet) {
        MessengerInfoDialog(
            user = recipientUser,
            language = language,
            onDismiss = { showInfoSheet = false }
        )
    }
}

@Composable
fun MessengerMessageBubble(
    message: MessageEntity,
    isMe: Boolean,
    recipientAvatar: String,
    isLastMessage: Boolean,
    onLongClick: () -> Unit,
    onPlayVoice: () -> Unit
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isMe) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(recipientAvatar).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(end = 6.dp, bottom = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .then(
                        if (message.messageType == "LIKE_STICKER") {
                            Modifier.background(Color.Transparent)
                        } else if (isMe) {
                            Modifier.background(MessengerBubbleGradient)
                        } else {
                            Modifier.background(MessengerDarkBubble)
                        }
                    )
                    .clickable { onLongClick() }
                    .padding(
                        if (message.messageType == "LIKE_STICKER") PaddingValues(4.dp)
                        else PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    )
            ) {
                when (message.messageType) {
                    "LIKE_STICKER" -> {
                        // Iconic Large Facebook Messenger Like Thumbs Up
                        Text(
                            text = "👍",
                            fontSize = 42.sp
                        )
                    }
                    "IMAGE" -> {
                        Column {
                            if (message.mediaUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(message.mediaUrl).crossfade(true).build(),
                                    contentDescription = "Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text(text = message.text, color = Color.White, fontSize = 13.sp)
                        }
                    }
                    "VOICE" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPlayVoice,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Waveform bars simulation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val heights = listOf(10, 18, 24, 14, 22, 16, 26, 12, 20, 8, 14, 22)
                                heights.forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .background(Color.White, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "0:${if (message.voiceDurationSec < 10) "0${message.voiceDurationSec}" else "${message.voiceDurationSec}"}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Messenger Reaction Badge on Message Bubble
        if (!message.reaction.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .offset(x = if (isMe) (-12).dp else 36.dp, y = (-8).dp)
                    .background(MessengerDarkBubble, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = message.reaction, fontSize = 12.sp)
            }
        }

        // Timestamp & Facebook Messenger Delivered/Seen Checkmark
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp, end = if (isMe) 4.dp else 0.dp, start = if (!isMe) 34.dp else 0.dp)
        ) {
            Text(
                text = timeFormat.format(Date(message.createdAt)),
                color = ShortlyTextSecondary.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            if (isMe && isLastMessage) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Delivered",
                    tint = MessengerBlue,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun MessengerReactionBar(
    onSelectReaction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF242526), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val emojis = listOf("❤️", "😆", "😮", "😢", "😠", "👍")
            emojis.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable { onSelectReaction(emoji) }
                )
            }
        }
    }
}

@Composable
fun MessengerActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
fun MessengerCallDialog(
    recipientUser: UserEntity,
    callType: String,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isVideoCameraOn by remember { mutableStateOf(true) }
    var callDurationSec by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            callDurationSec++
        }
    }

    Dialog(onDismissRequest = onEndCall) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18191A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (callType == "VIDEO") "Facebook Messenger Video Call" else "Facebook Messenger Audio Call",
                    color = MessengerBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Pulsing Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(MessengerBlue.copy(alpha = 0.2f), CircleShape)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(recipientUser.avatarUrl).crossfade(true).build(),
                        contentDescription = recipientUser.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = recipientUser.displayName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "কথা হচ্ছে: 0:${if (callDurationSec < 10) "0$callDurationSec" else "$callDurationSec"}",
                    color = MessengerActiveGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(30.dp))

                // In-Call Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .background(if (isMuted) Color(0xFFFF3B30) else ShortlySurfaceLight, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
                    }

                    if (callType == "VIDEO") {
                        IconButton(
                            onClick = { isVideoCameraOn = !isVideoCameraOn },
                            modifier = Modifier
                                .background(if (!isVideoCameraOn) Color(0xFFFF3B30) else ShortlySurfaceLight, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(imageVector = if (isVideoCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff, contentDescription = "Camera", tint = Color.White)
                        }
                    }

                    IconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        modifier = Modifier
                            .background(if (isSpeakerOn) MessengerBlue else ShortlySurfaceLight, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White)
                    }

                    // End Call button
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .background(Color(0xFFFF3B30), CircleShape)
                            .size(54.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MessengerInfoDialog(
    user: UserEntity,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF242526)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(user.avatarUrl).crossfade(true).build(),
                    contentDescription = user.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = user.displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "@${user.username}", color = ShortlyTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                MessengerSettingsRow(icon = Icons.Default.ColorLens, title = "Theme: Messenger Gradient") {
                    Toast.makeText(context, "Theme: Classic Messenger Gradient", Toast.LENGTH_SHORT).show()
                }
                MessengerSettingsRow(icon = Icons.Default.ThumbUp, title = "Quick Emoji: 👍") {
                    Toast.makeText(context, "Emoji: 👍", Toast.LENGTH_SHORT).show()
                }
                MessengerSettingsRow(icon = Icons.Default.NotificationsOff, title = "Mute Notifications") {
                    Toast.makeText(context, "Notifications muted for 8 hours", Toast.LENGTH_SHORT).show()
                }
                MessengerSettingsRow(icon = Icons.Default.Block, title = "Block User", tint = Color(0xFFFF3B30)) {
                    Toast.makeText(context, "User blocked on Shortly", Toast.LENGTH_SHORT).show()
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MessengerBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessengerSettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, color = tint, fontSize = 13.sp)
    }
}
