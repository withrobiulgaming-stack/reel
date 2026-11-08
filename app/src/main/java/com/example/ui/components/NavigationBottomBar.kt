package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class NavDestination {
    HOME,
    EXPLORE,
    CREATE,
    INBOX,
    PROFILE
}

@Composable
fun NavigationBottomBar(
    currentDestination: NavDestination,
    language: AppLanguage,
    unreadNotificationCount: Int = 0,
    onNavigate: (NavDestination) -> Unit,
    onAdminSecretTrigger: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Floating Frosted Glass Capsule Bar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xD0171B2B), // Frosted dark acrylic top
                            Color(0xF00D0F1A)  // Frosted dark acrylic bottom
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0x60FFFFFF), // Specular light highlight
                            Color(0x18FFFFFF),
                            Color(0x05FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Home Item
            GlassNavItem(
                iconSelected = Icons.Filled.Home,
                iconUnselected = Icons.Outlined.Home,
                label = Strings.home(language),
                isSelected = currentDestination == NavDestination.HOME,
                onClick = { onNavigate(NavDestination.HOME) }
            )

            // 2. Explore Item
            GlassNavItem(
                iconSelected = Icons.Filled.Search,
                iconUnselected = Icons.Outlined.Search,
                label = Strings.explore(language),
                isSelected = currentDestination == NavDestination.EXPLORE,
                onClick = { onNavigate(NavDestination.EXPLORE) }
            )

            // 3. Create Button (TikTok & Liquid Glass Style with Glowing Cyan & Pink Aura)
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 34.dp)
                    .clickable { onNavigate(NavDestination.CREATE) },
                contentAlignment = Alignment.Center
            ) {
                // Glassy Cyan Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (-3).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShortlyCyan.copy(alpha = 0.9f))
                )
                // Glassy Magenta Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShortlyPrimary.copy(alpha = 0.9f))
                )
                // Center Crystal White Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFE8E8F0))
                            )
                        )
                        .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = Strings.create(language),
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 4. Inbox Item with Glassy Badge
            GlassNavItem(
                iconSelected = Icons.Filled.ChatBubble,
                iconUnselected = Icons.Outlined.ChatBubbleOutline,
                label = Strings.inbox(language),
                badgeCount = unreadNotificationCount,
                isSelected = currentDestination == NavDestination.INBOX,
                onClick = { onNavigate(NavDestination.INBOX) }
            )

            // 5. Profile Item with 10-Second Hold Secret Admin Trigger
            val coroutineScope = rememberCoroutineScope()
            var holdDurationSeconds by remember { mutableIntStateOf(0) }
            var isHoldingProfile by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onNavigate(NavDestination.PROFILE)
                            },
                            onPress = {
                                isHoldingProfile = true
                                holdDurationSeconds = 0
                                val job = coroutineScope.launch {
                                    for (sec in 1..10) {
                                        delay(1000L)
                                        holdDurationSeconds = sec
                                    }
                                    isHoldingProfile = false
                                    holdDurationSeconds = 0
                                    onAdminSecretTrigger()
                                }
                                tryAwaitRelease()
                                job.cancel()
                                isHoldingProfile = false
                                holdDurationSeconds = 0
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (currentDestination == NavDestination.PROFILE) {
                                Modifier
                                    .background(Color(0x35FFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            } else {
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            }
                        )
                ) {
                    BadgedBox(
                        badge = {
                            if (isHoldingProfile && holdDurationSeconds > 0) {
                                Badge(
                                    containerColor = ShortlyPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text("${10 - holdDurationSeconds}s", fontSize = 9.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentDestination == NavDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = Strings.profile(language),
                            tint = if (currentDestination == NavDestination.PROFILE) Color.White else ShortlyTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Strings.profile(language),
                        fontSize = 10.sp,
                        fontWeight = if (currentDestination == NavDestination.PROFILE) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentDestination == NavDestination.PROFILE) Color.White else ShortlyTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.GlassNavItem(
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelected) {
                        Modifier
                            .background(Color(0x30FFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    }
                )
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = ShortlyPrimary,
                            contentColor = Color.White
                        ) {
                            Text("$badgeCount", fontSize = 9.sp)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSelected) iconSelected else iconUnselected,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else ShortlyTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ShortlyTextSecondary
            )
        }
    }
}
