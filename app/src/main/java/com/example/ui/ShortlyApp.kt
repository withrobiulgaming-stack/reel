package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.repository.ShortlyRepository
import com.example.domain.AppLanguage
import com.example.domain.LocalizationManager
import com.example.domain.Strings
import com.example.ui.components.NavDestination
import com.example.ui.components.NavigationBottomBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminPasswordDialog
import com.example.ui.screens.auth.AuthDialog
import com.example.ui.screens.creator.CreatorDashboardScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.inbox.ChatScreen
import com.example.ui.screens.inbox.InboxScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.WatchHistoryScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.upload.UploadScreen

sealed class ScreenState {
    data object Main : ScreenState()
    data class Chat(val recipientUser: UserEntity) : ScreenState()
    data object CreatorDashboard : ScreenState()
    data object WatchHistory : ScreenState()
    data object Settings : ScreenState()
    data class CreatorProfile(val creatorId: String) : ScreenState()
    data object AdminPanel : ScreenState()
}

@Composable
fun ShortlyApp(
    repository: ShortlyRepository,
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val isLoggedIn by repository.isLoggedIn.collectAsState()

    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Main) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showAdminPasswordDialog by remember { mutableStateOf(false) }

    val notifications by repository.getNotifications().collectAsState(initial = emptyList())
    val unreadNotifications = remember(notifications) {
        if (isLoggedIn) notifications.count { !it.isRead } else 0
    }

    // Android Hardware / Predictive Back Handling
    BackHandler(enabled = currentScreen !is ScreenState.Main || currentDestination != NavDestination.HOME) {
        if (currentScreen !is ScreenState.Main) {
            currentScreen = ScreenState.Main
        } else if (currentDestination != NavDestination.HOME) {
            currentDestination = NavDestination.HOME
        }
    }

    when (val screen = currentScreen) {
        is ScreenState.Chat -> {
            ChatScreen(
                recipientUser = screen.recipientUser,
                repository = repository,
                language = currentLanguage,
                onBack = { currentScreen = ScreenState.Main }
            )
        }
        is ScreenState.CreatorDashboard -> {
            CreatorDashboardScreen(
                repository = repository,
                language = currentLanguage,
                onBack = { currentScreen = ScreenState.Main }
            )
        }
        is ScreenState.WatchHistory -> {
            WatchHistoryScreen(
                repository = repository,
                language = currentLanguage,
                onBack = { currentScreen = ScreenState.Main },
                onVideoClick = { videoId ->
                    currentScreen = ScreenState.Main
                    currentDestination = NavDestination.HOME
                }
            )
        }
        is ScreenState.Settings -> {
            SettingsScreen(
                repository = repository,
                language = currentLanguage,
                onLanguageChange = { localizationManager.setLanguage(it) },
                onOpenWatchHistory = { currentScreen = ScreenState.WatchHistory },
                onOpenCreatorDashboard = { currentScreen = ScreenState.CreatorDashboard },
                onRequestAdminAccess = { showAdminPasswordDialog = true },
                onRequestLogin = { showAuthDialog = true },
                onLogoutSuccess = {
                    currentScreen = ScreenState.Main
                    currentDestination = NavDestination.HOME
                },
                onBack = { currentScreen = ScreenState.Main }
            )
        }
        is ScreenState.AdminPanel -> {
            AdminDashboardScreen(
                repository = repository,
                language = currentLanguage,
                onBack = { currentScreen = ScreenState.Settings }
            )
        }
        is ScreenState.CreatorProfile -> {
            ProfileScreen(
                userId = screen.creatorId,
                repository = repository,
                language = currentLanguage,
                onLanguageChange = { localizationManager.setLanguage(it) },
                onOpenCreatorDashboard = { currentScreen = ScreenState.CreatorDashboard },
                onOpenWatchHistory = { currentScreen = ScreenState.WatchHistory },
                onOpenSettings = { currentScreen = ScreenState.Settings },
                onOpenChat = { user -> currentScreen = ScreenState.Chat(user) },
                onRequestLogin = { showAuthDialog = true },
                onVideoClick = {
                    currentScreen = ScreenState.Main
                    currentDestination = NavDestination.HOME
                }
            )
        }
        is ScreenState.Main -> {
            Scaffold(
                bottomBar = {
                    NavigationBottomBar(
                        currentDestination = currentDestination,
                        language = currentLanguage,
                        unreadNotificationCount = unreadNotifications,
                        onNavigate = { destination ->
                            if (destination == NavDestination.CREATE && !isLoggedIn) {
                                showAuthDialog = true
                            } else {
                                currentDestination = destination
                            }
                        },
                        onAdminSecretTrigger = {
                            showAdminPasswordDialog = true
                        }
                    )
                },
                containerColor = Color.Black,
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                ) {
                    when (currentDestination) {
                        NavDestination.HOME -> {
                            FeedScreen(
                                repository = repository,
                                language = currentLanguage,
                                onProfileClick = { creatorId ->
                                    if (creatorId == repository.currentUserId) {
                                        currentDestination = NavDestination.PROFILE
                                    } else {
                                        currentScreen = ScreenState.CreatorProfile(creatorId)
                                    }
                                },
                                onRequestLogin = {
                                    showAuthDialog = true
                                }
                            )
                        }
                        NavDestination.EXPLORE -> {
                            ExploreScreen(
                                repository = repository,
                                language = currentLanguage,
                                onVideoClick = {
                                    currentDestination = NavDestination.HOME
                                },
                                onCreatorClick = { creatorId ->
                                    currentScreen = ScreenState.CreatorProfile(creatorId)
                                }
                            )
                        }
                        NavDestination.CREATE -> {
                            UploadScreen(
                                repository = repository,
                                language = currentLanguage,
                                onUploadSuccess = {
                                    currentDestination = NavDestination.HOME
                                }
                            )
                        }
                        NavDestination.INBOX -> {
                            if (!isLoggedIn) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.foundation.layout.Column(
                                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = Strings.loginRequiredTitle(currentLanguage),
                                            color = Color.White,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        androidx.compose.material3.Text(
                                            text = Strings.loginRequiredDesc(currentLanguage),
                                            color = com.example.ui.theme.ShortlyTextSecondary,
                                            fontSize = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        androidx.compose.material3.Button(
                                            onClick = { showAuthDialog = true },
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = com.example.ui.theme.ShortlyPrimary
                                            )
                                        ) {
                                            androidx.compose.material3.Text(
                                                text = Strings.login(currentLanguage),
                                                color = Color.White,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                InboxScreen(
                                    repository = repository,
                                    language = currentLanguage,
                                    onOpenChat = { user ->
                                        currentScreen = ScreenState.Chat(user)
                                    }
                                )
                            }
                        }
                        NavDestination.PROFILE -> {
                            ProfileScreen(
                                userId = repository.currentUserId,
                                repository = repository,
                                language = currentLanguage,
                                onLanguageChange = { localizationManager.setLanguage(it) },
                                onOpenCreatorDashboard = { currentScreen = ScreenState.CreatorDashboard },
                                onOpenWatchHistory = { currentScreen = ScreenState.WatchHistory },
                                onOpenSettings = { currentScreen = ScreenState.Settings },
                                onRequestLogin = { showAuthDialog = true },
                                onVideoClick = {
                                    currentDestination = NavDestination.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Login Required Modal Bottom Sheet
    if (showAuthDialog) {
        AuthDialog(
            language = currentLanguage,
            repository = repository,
            onDismiss = { showAuthDialog = false },
            onLoginSuccess = {
                showAuthDialog = false
                Toast.makeText(
                    context,
                    if (currentLanguage == AppLanguage.BANGLA) "লগইন সফল হয়েছে!" else "Login successful!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Password Protected Admin Portal Access
    if (showAdminPasswordDialog) {
        AdminPasswordDialog(
            language = currentLanguage,
            onDismiss = { showAdminPasswordDialog = false },
            onSuccess = {
                showAdminPasswordDialog = false
                currentScreen = ScreenState.AdminPanel
            }
        )
    }
}
