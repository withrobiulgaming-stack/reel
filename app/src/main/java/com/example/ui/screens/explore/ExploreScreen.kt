package com.example.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.local.UserEntity
import com.example.data.local.VideoEntity
import com.example.data.repository.ShortlyRepository
import com.example.data.sample.SampleData
import com.example.domain.AppLanguage
import com.example.domain.Strings
import androidx.compose.foundation.border
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    repository: ShortlyRepository,
    language: AppLanguage,
    onVideoClick: (videoId: String) -> Unit,
    onCreatorClick: (creatorId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val allVideos by repository.getApprovedVideos().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())

    // Search and filter logic
    val filteredVideos = remember(allVideos, searchQuery, selectedCategory) {
        allVideos.filter { video ->
            val matchesCategory = selectedCategory == "All" || video.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = if (searchQuery.isBlank()) true else {
                video.title.contains(searchQuery, ignoreCase = true) ||
                        video.caption.contains(searchQuery, ignoreCase = true) ||
                        video.hashtags.contains(searchQuery, ignoreCase = true) ||
                        video.creatorName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlassAtmosphereGradient)
            .statusBarsPadding()
    ) {
        // Search Bar with Frosted Glass styling
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = Strings.searchHint(language),
                    color = ShortlyTextSecondary,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ShortlyCyan
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x35FFFFFF),
                unfocusedContainerColor = Color(0x20FFFFFF),
                focusedBorderColor = ShortlyCyan,
                unfocusedBorderColor = Color(0x30FFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    label = if (language == AppLanguage.BANGLA) "সবগুলো" else "All",
                    isSelected = selectedCategory == "All",
                    onClick = { selectedCategory = "All" }
                )
            }
            items(SampleData.trendingCategories.drop(1)) { cat ->
                CategoryChip(
                    label = cat,
                    isSelected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        // Main Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Trending Hashtag Marquee
            item {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                    Text(
                        text = Strings.trendingHashtags(language),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(SampleData.trendingHashtags) { (tag, count) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(ShortlySurfaceLight, RoundedCornerShape(16.dp))
                                    .clickable { searchQuery = tag }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = ShortlyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tag,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$count ${Strings.views(language)}",
                                    color = ShortlyTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Top Creators Section
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = Strings.topCreators(language),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(allUsers) { user ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(76.dp)
                                    .clickable { onCreatorClick(user.id) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .background(
                                                Brush.linearGradient(listOf(ShortlyPrimary, ShortlyCyan)),
                                                CircleShape
                                            )
                                    )
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(user.avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = user.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.displayName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Grid of Discoverable Videos (2 columns)
            item {
                Text(
                    text = if (language == AppLanguage.BANGLA) "আবিষ্কার করুন" else "Discover Videos",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                )
            }

            // 2-Column Responsive Video Grid items
            val chunkedVideos = filteredVideos.chunked(2)
            items(chunkedVideos) { rowVideos ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (video in rowVideos) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
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
                            // Gradient shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            // Video Stats & Title Overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = video.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${video.viewsCount}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    if (rowVideos.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected) ShortlyPrimary else Color(0x30FFFFFF),
                RoundedCornerShape(18.dp)
            )
            .border(
                1.dp,
                if (isSelected) Color(0x80FFFFFF) else Color(0x25FFFFFF),
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
