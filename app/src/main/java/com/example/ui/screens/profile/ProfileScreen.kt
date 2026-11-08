package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.UserEntity
import com.example.data.repository.ShortlyRepository
import com.example.data.sample.SampleData
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ProfileTab {
    MY_VIDEOS,
    LIKED_VIDEOS,
    SAVED_VIDEOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    repository: ShortlyRepository,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenCreatorDashboard: () -> Unit,
    onOpenWatchHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenChat: (UserEntity) -> Unit = {},
    onRequestLogin: () -> Unit,
    onVideoClick: (videoId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isMyProfile = userId == repository.currentUserId
    val isLoggedIn by repository.isLoggedIn.collectAsState()

    val user by repository.getUser(userId).collectAsState(initial = SampleData.currentUser)
    val myVideos by repository.getVideosByCreator(userId).collectAsState(initial = emptyList())
    val allVideos by repository.getApprovedVideos().collectAsState(initial = emptyList())
    val likedVideoIds by repository.getLikedVideoIds().collectAsState(initial = emptyList())
    val bookmarkedVideoIds by repository.getBookmarkedVideoIds().collectAsState(initial = emptyList())
    val isFollowing by repository.isFollowingCreator(userId).collectAsState(initial = false)

    var selectedTab by remember { mutableStateOf(ProfileTab.MY_VIDEOS) }
    var showEditDialog by remember { mutableStateOf(false) }

    val activeUser = user ?: SampleData.currentUser

    val displayVideos = remember(selectedTab, myVideos, allVideos, likedVideoIds, bookmarkedVideoIds) {
        when (selectedTab) {
            ProfileTab.MY_VIDEOS -> myVideos
            ProfileTab.LIKED_VIDEOS -> allVideos.filter { likedVideoIds.contains(it.id) }
            ProfileTab.SAVED_VIDEOS -> allVideos.filter { bookmarkedVideoIds.contains(it.id) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlassAtmosphereGradient)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isMyProfile && !isLoggedIn) Strings.guestUser(language) else "@${activeUser.username}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            // Settings option only shows after user logs in with their ID
            if (isLoggedIn && isMyProfile) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = Strings.settings(language),
                        tint = Color.White
                    )
                }
            }
        }

        // If it's my profile and user is NOT logged in: Show Guest Screen
        if (isMyProfile && !isLoggedIn) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(ShortlySurfaceLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = null,
                        tint = ShortlyTextSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Strings.guestModeTitle(language),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = Strings.guestModeDesc(language),
                    color = ShortlyTextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Log in button
                Button(
                    onClick = onRequestLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.login(language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Watch History Button for Guest
                OutlinedButton(
                    onClick = onOpenWatchHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShortlySurfaceLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = ShortlyCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.watchHistory(language),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Profile Avatar & Verified
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(activeUser.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = activeUser.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .border(2.dp, ShortlyPrimary, CircleShape)
                    )
                    if (activeUser.isVerified) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(ShortlyCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeUser.displayName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activeUser.bio,
                    color = ShortlyTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Glassy Counters Container (Following, Followers, Likes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .glassmorphic(RoundedCornerShape(20.dp), backgroundColor = Color(0x30FFFFFF))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(count = "${activeUser.followingCount}", label = Strings.followingCount(language))
                ProfileStatItem(count = "${activeUser.followersCount}", label = Strings.followers(language))
                ProfileStatItem(count = "${activeUser.totalLikes}", label = Strings.likes(language))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Frosted Glass Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isMyProfile) {
                    // Edit Profile (Glass Button)
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x35FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = Strings.editProfile(language),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Creator Studio Shortcut (Glass Button)
                    Button(
                        onClick = onOpenCreatorDashboard,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ShortlyAccentGold.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShortlyAccentGold.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = ShortlyAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.creatorStudio(language),
                            color = ShortlyAccentGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Follow / Unfollow Button
                    Button(
                        onClick = {
                            if (!isLoggedIn) {
                                onRequestLogin()
                            } else {
                                coroutineScope.launch { repository.toggleFollow(userId) }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color(0x35FFFFFF) else ShortlyPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isFollowing) Strings.followingState(language) else Strings.follow(language),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Direct Chat / Message Button
                    Button(
                        onClick = {
                            if (!isLoggedIn) {
                                onRequestLogin()
                            } else {
                                onOpenChat(activeUser)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x35FFFFFF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "মেসেজ" else "Message",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isMyProfile && isLoggedIn) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenWatchHistory,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x20FFFFFF), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = ShortlyCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Strings.watchHistory(language), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x20FFFFFF), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Strings.settings(language), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Tabs (Glass Strip)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .glassmorphic(RoundedCornerShape(16.dp), backgroundColor = Color(0x30FFFFFF))
            ) {
                TabItem(
                    icon = Icons.Outlined.VideoLibrary,
                    isSelected = selectedTab == ProfileTab.MY_VIDEOS,
                    onClick = { selectedTab = ProfileTab.MY_VIDEOS },
                    modifier = Modifier.weight(1f)
                )
                if (isMyProfile) {
                    TabItem(
                        icon = Icons.Outlined.FavoriteBorder,
                        isSelected = selectedTab == ProfileTab.LIKED_VIDEOS,
                        onClick = { selectedTab = ProfileTab.LIKED_VIDEOS },
                        modifier = Modifier.weight(1f)
                    )
                    TabItem(
                        icon = Icons.Outlined.BookmarkBorder,
                        isSelected = selectedTab == ProfileTab.SAVED_VIDEOS,
                        onClick = { selectedTab = ProfileTab.SAVED_VIDEOS },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid of Videos
            if (displayVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো ভিডিও পাওয়া যায়নি" else "No videos here yet",
                        color = ShortlyTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(displayVideos, key = { it.id }) { video ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.75f)
                                .background(ShortlySurfaceDark)
                                .clickable { onVideoClick(video.id) }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(video.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${video.viewsCount}",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            initialDisplayName = activeUser.displayName,
            initialBio = activeUser.bio,
            language = language,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio ->
                coroutineScope.launch {
                    repository.updateUserProfile(name, bio)
                }
                showEditDialog = false
                Toast.makeText(context, Strings.success(language), Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = ShortlyTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else ShortlyTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(Color.White)
                )
            }
        }
    }
}
