package com.example.ui.screens.feed

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AdEntity
import com.example.data.local.CommentEntity
import com.example.data.local.VideoEntity
import com.example.data.repository.ShortlyRepository
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.components.CommentsSheet
import com.example.ui.components.ShareSheet
import com.example.ui.theme.ShortlyAccentGold
import com.example.ui.theme.ShortlyCyan
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class FeedTab {
    FOR_YOU,
    FOLLOWING,
    TRENDING,
    LATEST
}

sealed class FeedItem {
    data class Video(val video: VideoEntity) : FeedItem()
    data class SponsoredAd(val ad: AdEntity) : FeedItem()
}

@Composable
fun FeedScreen(
    repository: ShortlyRepository,
    language: AppLanguage,
    onProfileClick: (creatorId: String) -> Unit,
    onRequestLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isLoggedIn by repository.isLoggedIn.collectAsState()
    var selectedTab by remember { mutableStateOf(FeedTab.FOR_YOU) }

    val rawVideos by repository.getApprovedVideos().collectAsState(initial = emptyList())
    val activeAds by repository.getActiveAds().collectAsState(initial = emptyList())
    val likedVideoIds by repository.getLikedVideoIds().collectAsState(initial = emptyList())
    val bookmarkedVideoIds by repository.getBookmarkedVideoIds().collectAsState(initial = emptyList())
    val followingIds by repository.getFollowingIds().collectAsState(initial = emptyList())

    // Active sheet states
    var activeCommentVideoId by remember { mutableStateOf<String?>(null) }
    var activeShareVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var completedVideoForAd by remember { mutableStateOf<VideoEntity?>(null) }

    val activeComments by if (activeCommentVideoId != null) {
        repository.getCommentsForVideo(activeCommentVideoId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<CommentEntity>()) }
    }

    // Dynamic feed ranking according to RecommendationEngine
    val displayVideos = remember(rawVideos, selectedTab, likedVideoIds, followingIds) {
        when (selectedTab) {
            FeedTab.FOR_YOU -> repository.recommendationEngine.rankPersonalizedFeed(
                videos = rawVideos,
                interactions = emptyMap(),
                likedVideoIds = likedVideoIds.toSet(),
                followedCreatorIds = followingIds.toSet()
            )
            FeedTab.FOLLOWING -> repository.recommendationEngine.rankFollowingFeed(
                videos = rawVideos,
                followedCreatorIds = followingIds.toSet()
            )
            FeedTab.TRENDING -> repository.recommendationEngine.rankTrendingFeed(rawVideos)
            FeedTab.LATEST -> repository.recommendationEngine.rankLatestFeed(rawVideos)
        }
    }

    // Filter ads for in-feed (shown every 5-8 videos)
    val inFeedAds = remember(activeAds) {
        activeAds.filter { it.placement == "IN_FEED" || it.placement == "BOTH" }
    }

    // Interleave ads every 6 videos (within 5-8 interval)
    val feedItems = remember(displayVideos, inFeedAds) {
        if (inFeedAds.isEmpty() || displayVideos.isEmpty()) {
            displayVideos.map { FeedItem.Video(it) }
        } else {
            val list = mutableListOf<FeedItem>()
            var adIdx = 0
            for (i in displayVideos.indices) {
                list.add(FeedItem.Video(displayVideos[i]))
                // Show an ad after every 5 videos (i.e. at 6th position, 12th position, etc.)
                if ((i + 1) % 5 == 0) {
                    val ad = inFeedAds[adIdx % inFeedAds.size]
                    list.add(FeedItem.SponsoredAd(ad))
                    adIdx++
                }
            }
            list
        }
    }

    val pagerState = rememberPagerState(pageCount = { feedItems.size })

    // Track views & ad impressions when scrolling
    LaunchedEffect(pagerState.currentPage, feedItems) {
        if (feedItems.isNotEmpty() && pagerState.currentPage < feedItems.size) {
            when (val currentItem = feedItems[pagerState.currentPage]) {
                is FeedItem.Video -> {
                    repository.recordView(currentItem.video, watchTimeMs = 3000L, completed = false)
                }
                is FeedItem.SponsoredAd -> {
                    repository.recordAdImpression(currentItem.ad.id)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (feedItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ShortlyPrimary)
            }
        } else {
            // Full Screen Vertical Pager
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (val item = feedItems[page]) {
                    is FeedItem.Video -> {
                        val video = item.video
                        val isLiked = if (isLoggedIn) likedVideoIds.contains(video.id) else false
                        val isBookmarked = if (isLoggedIn) bookmarkedVideoIds.contains(video.id) else false
                        val isFollowing = if (isLoggedIn) followingIds.contains(video.creatorId) else false

                        VideoItemView(
                            video = video,
                            isPlaying = pagerState.currentPage == page,
                            isLiked = isLiked,
                            isBookmarked = isBookmarked,
                            isFollowing = isFollowing,
                            language = language,
                            onLikeClick = {
                                if (!isLoggedIn) {
                                    onRequestLogin()
                                } else {
                                    coroutineScope.launch { repository.toggleLike(video.id) }
                                }
                            },
                            onCommentClick = {
                                if (!isLoggedIn) {
                                    onRequestLogin()
                                } else {
                                    activeCommentVideoId = video.id
                                }
                            },
                            onBookmarkClick = {
                                if (!isLoggedIn) {
                                    onRequestLogin()
                                } else {
                                    coroutineScope.launch { repository.toggleBookmark(video.id) }
                                }
                            },
                            onShareClick = {
                                activeShareVideo = video
                            },
                            onFollowClick = {
                                if (!isLoggedIn) {
                                    onRequestLogin()
                                } else {
                                    coroutineScope.launch { repository.toggleFollow(video.creatorId) }
                                }
                            },
                            onProfileClick = onProfileClick,
                            onHashtagClick = { /* Handle hashtag filter */ },
                            onVideoCompleted = {
                                coroutineScope.launch {
                                    repository.recordView(video, watchTimeMs = (video.durationSeconds * 1000).toLong(), completed = true)
                                }
                                // Show Post-Video Ad overlay!
                                val postVideoAds = activeAds.filter { it.placement == "POST_VIDEO" || it.placement == "BOTH" }
                                if (postVideoAds.isNotEmpty()) {
                                    completedVideoForAd = video
                                }
                            }
                        )
                    }

                    is FeedItem.SponsoredAd -> {
                        // Full Screen Sponsored Ad
                        SponsoredAdItemView(
                            ad = item.ad,
                            language = language,
                            onCtaClick = {
                                coroutineScope.launch {
                                    repository.recordAdClick(item.ad.id)
                                }
                                Toast.makeText(context, "Opening: ${item.ad.targetUrl}", Toast.LENGTH_SHORT).show()
                            },
                            onShareClick = {
                                Toast.makeText(context, "বিজ্ঞাপন শেয়ার করা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Top Navigation Feed Tabs (Following | For You | Trending | Latest)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedTabItem(
                title = Strings.following(language),
                isSelected = selectedTab == FeedTab.FOLLOWING,
                onClick = { selectedTab = FeedTab.FOLLOWING }
            )
            FeedTabItem(
                title = Strings.forYou(language),
                isSelected = selectedTab == FeedTab.FOR_YOU,
                onClick = { selectedTab = FeedTab.FOR_YOU }
            )
            FeedTabItem(
                title = Strings.trending(language),
                isSelected = selectedTab == FeedTab.TRENDING,
                onClick = { selectedTab = FeedTab.TRENDING }
            )
            FeedTabItem(
                title = Strings.latest(language),
                isSelected = selectedTab == FeedTab.LATEST,
                onClick = { selectedTab = FeedTab.LATEST }
            )
        }

        // Post-Video Ad Overlay (Triggered automatically when a video completes)
        if (completedVideoForAd != null) {
            val postVideoAds = activeAds.filter { it.placement == "POST_VIDEO" || it.placement == "BOTH" }
            if (postVideoAds.isNotEmpty()) {
                val ad = postVideoAds.first()
                PostVideoAdOverlay(
                    ad = ad,
                    videoTitle = completedVideoForAd!!.title,
                    language = language,
                    onDismiss = { completedVideoForAd = null },
                    onCtaClick = {
                        coroutineScope.launch {
                            repository.recordAdClick(ad.id)
                        }
                        completedVideoForAd = null
                        Toast.makeText(context, "অফারটি গ্রহণ করা হয়েছে: ${ad.ctaText}", Toast.LENGTH_SHORT).show()
                    },
                    onReplay = {
                        completedVideoForAd = null
                    }
                )
            }
        }

        // Comments Bottom Sheet
        if (activeCommentVideoId != null) {
            CommentsSheet(
                comments = activeComments,
                language = language,
                onDismiss = { activeCommentVideoId = null },
                onSendComment = { text, parentId ->
                    coroutineScope.launch {
                        repository.postComment(activeCommentVideoId!!, text, parentId)
                    }
                },
                onLikeComment = { commentId ->
                    coroutineScope.launch {
                        repository.likeComment(commentId)
                    }
                }
            )
        }

        // Share Bottom Sheet
        if (activeShareVideo != null) {
            ShareSheet(
                videoTitle = activeShareVideo!!.title,
                language = language,
                onDismiss = { activeShareVideo = null },
                onShareCompleted = {
                    coroutineScope.launch {
                        repository.recordShare(activeShareVideo!!.id)
                    }
                }
            )
        }
    }
}

@Composable
fun SponsoredAdItemView(
    ad: AdEntity,
    language: AppLanguage,
    onCtaClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(1420) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // High quality full-bleed Ad Media Banner
        AsyncImage(
            model = ImageRequest.Builder(context).data(ad.mediaUrl).crossfade(true).build(),
            contentDescription = ad.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Gradient for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top Sponsored Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 50.dp, start = 16.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = ShortlyAccentGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (language == AppLanguage.BANGLA) "স্পন্সরড বিজ্ঞাপন" else "Sponsored Ad",
                color = ShortlyAccentGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right Sidebar Actions (Like, Share, Info)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 40.dp)
        ) {
            // Brand Logo
            AsyncImage(
                model = ImageRequest.Builder(context).data(ad.brandLogoUrl).crossfade(true).build(),
                contentDescription = ad.brandName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            // Like Ad
            ActionIconButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                count = "$likesCount",
                tint = if (isLiked) ShortlyPrimary else Color.White,
                onClick = {
                    isLiked = !isLiked
                    likesCount += if (isLiked) 1 else -1
                }
            )

            // Share Ad
            ActionIconButton(
                icon = Icons.Default.Share,
                count = "Share",
                tint = Color.White,
                onClick = onShareClick
            )

            // Info Ad
            IconButton(
                onClick = {
                    Toast.makeText(context, "এই বিজ্ঞাপনটি আপনার পছন্দের ওপর ভিত্তি করে দেখানো হয়েছে", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Why this ad", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Bottom Content & Call to Action (CTA)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 32.dp)
        ) {
            // Brand Title Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ad.brandName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Sponsor",
                    tint = ShortlyCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Ad Title
            Text(
                text = ad.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ad Description
            Text(
                text = ad.description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Pulsating Call to Action (CTA) Button
            Button(
                onClick = onCtaClick,
                colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = ad.ctaText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PostVideoAdOverlay(
    ad: AdEntity,
    videoTitle: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onCtaClick: () -> Unit,
    onReplay: () -> Unit
) {
    val context = LocalContext.current
    var countdownSec by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (countdownSec > 0) {
            delay(1000L)
            countdownSec--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { /* prevent click through */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ShortlySurfaceDark),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = ShortlyAccentGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "ভিডিও শেষে বিজ্ঞাপন" else "Sponsored Ad",
                            color = ShortlyAccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Skip button / countdown
                    Text(
                        text = if (countdownSec > 0) "Skip in ${countdownSec}s" else "Skip Ad ✕",
                        color = if (countdownSec > 0) ShortlyTextSecondary else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ShortlySurfaceLight)
                            .clickable { onDismiss() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ad Thumbnail Banner
                AsyncImage(
                    model = ImageRequest.Builder(context).data(ad.mediaUrl).crossfade(true).build(),
                    contentDescription = ad.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(ad.brandLogoUrl).crossfade(true).build(),
                        contentDescription = ad.brandName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = ad.brandName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Sponsored Partner", color = ShortlyCyan, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ad.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ad.description,
                    color = ShortlyTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main CTA Button
                Button(
                    onClick = onCtaClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ShortlyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(text = ad.ctaText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Replay Video button
                OutlinedButton(
                    onClick = onReplay,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShortlySurfaceLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (language == AppLanguage.BANGLA) "ভিডিওটি পুনরায় দেখুন" else "Replay Video", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FeedTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(3.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }
    }
}
