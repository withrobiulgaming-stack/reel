package com.example.ui.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.NotificationEntity
import com.example.data.local.UserEntity
import com.example.data.repository.ShortlyRepository
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.ShortlyAccentGold
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class InboxTab {
    CHATS,
    NOTIFICATIONS
}

@Composable
fun InboxScreen(
    repository: ShortlyRepository,
    language: AppLanguage,
    onOpenChat: (user: UserEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(InboxTab.CHATS) }
    var searchQuery by remember { mutableStateOf("") }

    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val notifications by repository.getNotifications().collectAsState(initial = emptyList())

    // Chat contacts (all other users filtered by search query)
    val chatContacts = remember(allUsers, searchQuery) {
        allUsers.filter { it.id != repository.currentUserId }
            .filter {
                if (searchQuery.isBlank()) true
                else it.displayName.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        // Facebook Messenger Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "চ্যাটস" else "Chats",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Messenger active indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF31A24C).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${chatContacts.size} Online",
                        color = Color(0xFF31A24C),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Camera quick icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ShortlySurfaceLight, CircleShape)
                        .clickable {
                            if (chatContacts.isNotEmpty()) onOpenChat(chatContacts.first())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                // New Message icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ShortlySurfaceLight, CircleShape)
                        .clickable {
                            if (chatContacts.isNotEmpty()) onOpenChat(chatContacts.first())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Message", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Sub Tabs: [Chats | Notifications]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(ShortlySurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == InboxTab.CHATS) ShortlySurfaceLight else Color.Transparent)
                    .clickable { selectedTab = InboxTab.CHATS }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "মেসেঞ্জার চ্যাট" else "Direct Messages",
                    color = if (selectedTab == InboxTab.CHATS) Color.White else ShortlyTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (selectedTab == InboxTab.CHATS) FontWeight.Bold else FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == InboxTab.NOTIFICATIONS) ShortlySurfaceLight else Color.Transparent)
                    .clickable { selectedTab = InboxTab.NOTIFICATIONS }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.notifications(language),
                    color = if (selectedTab == InboxTab.NOTIFICATIONS) Color.White else ShortlyTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (selectedTab == InboxTab.NOTIFICATIONS) FontWeight.Bold else FontWeight.Medium
                )
            }
        }

        // Content
        when (selectedTab) {
            InboxTab.CHATS -> {
                // Messenger Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "মেসেঞ্জারে খুঁজুন..." else "Search Messenger...",
                            color = ShortlyTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ShortlyTextSecondary, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShortlySurfaceDark,
                        unfocusedContainerColor = ShortlySurfaceDark,
                        focusedBorderColor = Color(0xFF0084FF),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Facebook Messenger Active Friends / Stories Tray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // "Your Story" item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (chatContacts.isNotEmpty()) onOpenChat(chatContacts.first())
                        }
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(ShortlySurfaceLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Story", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Your Story", color = ShortlyTextSecondary, fontSize = 11.sp)
                    }

                    // Online friends list
                    chatContacts.forEach { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onOpenChat(user) }
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0084FF).copy(alpha = 0.3f))
                                        .padding(2.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(user.avatarUrl).crossfade(true).build(),
                                        contentDescription = user.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                // Facebook Messenger Green Active Status Dot
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(0xFF31A24C), CircleShape)
                                        .background(Color.Black, RoundedCornerShape(2.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.displayName.split(" ").firstOrNull() ?: user.displayName,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Messenger Chat Conversations List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatContacts) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShortlySurfaceDark)
                                .clickable { onOpenChat(user) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(user.avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = user.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                )
                                // Messenger Active status dot
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(0xFF31A24C), CircleShape)
                                        .background(Color.Black, RoundedCornerShape(2.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = user.displayName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Active now",
                                        color = Color(0xFF31A24C),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = user.bio,
                                    color = ShortlyTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = ShortlyTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            InboxTab.NOTIFICATIONS -> {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "কোনো নতুন নোটিফিকেশন নেই" else "No notifications yet",
                            color = ShortlyTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { notif ->
                            NotificationRow(notif = notif, language = language)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    notif: NotificationEntity,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    val typeIcon = when (notif.type) {
        "LIKE" -> Icons.Default.Favorite to ShortlyPrimary
        "FOLLOW" -> Icons.Default.PersonAdd to Color(0xFF25F4EE)
        "EARNING" -> Icons.Default.MonetizationOn to ShortlyAccentGold
        else -> Icons.Default.Notifications to Color.White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (notif.isRead) ShortlySurfaceDark else ShortlySurfaceLight)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(notif.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(typeIcon.second, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon.first,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notif.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notif.message,
                color = ShortlyTextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeFormat.format(Date(notif.createdAt)),
                color = ShortlyTextSecondary.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
