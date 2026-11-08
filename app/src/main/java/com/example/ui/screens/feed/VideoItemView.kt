package com.example.ui.screens.feed

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.VideoEntity
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.components.HeartBurstAnimation
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.ShortlyAccentGold
import com.example.ui.theme.ShortlyPrimary

@Composable
fun VideoItemView(
    video: VideoEntity,
    isPlaying: Boolean,
    isLiked: Boolean,
    isBookmarked: Boolean,
    isFollowing: Boolean,
    language: AppLanguage,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit,
    onProfileClick: (creatorId: String) -> Unit,
    onHashtagClick: (hashtag: String) -> Unit,
    onVideoCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isUserPaused by remember { mutableStateOf(false) }
    var heartTrigger by remember { mutableStateOf(0L) }
    var isCaptionExpanded by remember { mutableStateOf(false) }

    // Vinyl Disc Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Video Player Surface
        VideoPlayerView(
            videoUrl = video.videoUrl,
            thumbnailUrl = video.thumbnailUrl,
            isPlaying = isPlaying && !isUserPaused,
            onSingleTap = { isUserPaused = !isUserPaused },
            onDoubleTap = {
                heartTrigger = System.currentTimeMillis()
                if (!isLiked) {
                    onLikeClick()
                }
            },
            onVideoCompleted = onVideoCompleted,
            modifier = Modifier.fillMaxSize()
        )

        // Heart Burst Animation on Double Tap
        HeartBurstAnimation(
            trigger = heartTrigger,
            modifier = Modifier.align(Alignment.Center)
        )

        // Right Action Sidebar (Avatar, Like, Comment, Bookmark, Share, Music Disc)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp)
        ) {
            // 1. Creator Profile Avatar with Follow Badge
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .size(54.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.creatorAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.creatorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick(video.creatorId) }
                )
                if (!isFollowing) {
                    Box(
                        modifier = Modifier
                            .offset(y = 6.dp)
                            .size(20.dp)
                            .background(ShortlyPrimary, CircleShape)
                            .clickable { onFollowClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = Strings.follow(language),
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 2. Like Button
            ActionIconButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                count = formatCount(video.likesCount),
                tint = if (isLiked) ShortlyPrimary else Color.White,
                onClick = onLikeClick
            )

            // 3. Comment Button
            ActionIconButton(
                icon = Icons.Default.Comment,
                count = formatCount(video.commentsCount),
                tint = Color.White,
                onClick = onCommentClick
            )

            // 4. Bookmark Button
            ActionIconButton(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                count = formatCount(video.bookmarksCount),
                tint = if (isBookmarked) ShortlyAccentGold else Color.White,
                onClick = onBookmarkClick
            )

            // 5. Share Button
            ActionIconButton(
                icon = Icons.Default.Share,
                count = formatCount(video.sharesCount),
                tint = Color.White,
                onClick = onShareClick
            )

            // 6. Rotating Music Disc
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2C2C2C), Color(0xFF111111))
                        ),
                        CircleShape
                    )
                    .rotate(if (isPlaying && !isUserPaused) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.creatorAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                )
            }
        }

        // Bottom Left Info Overlay (Creator Name, Caption, Audio Track)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            // Creator Name and Verified Check
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onProfileClick(video.creatorId) }
            ) {
                Text(
                    text = "@${video.creatorUsername}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (video.isVerifiedCreator) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF20D5EC),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Caption & Expandable text
            Text(
                text = video.caption,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                maxLines = if (isCaptionExpanded) 6 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isCaptionExpanded = !isCaptionExpanded }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sound Track Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${video.soundTitle} • ${video.soundArtist}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = count,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCount(num: Int): String {
    return when {
        num >= 1000000 -> String.format("%.1fM", num / 1000000.0)
        num >= 1000 -> String.format("%.1fK", num / 1000.0)
        else -> num.toString()
    }
}
