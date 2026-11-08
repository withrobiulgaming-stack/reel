package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.repository.ShortlyRepository
import com.example.domain.AppLanguage
import com.example.domain.Strings
import com.example.ui.theme.ShortlyPrimary
import com.example.ui.theme.ShortlySurfaceDark
import com.example.ui.theme.ShortlySurfaceLight
import com.example.ui.theme.ShortlyTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchHistoryScreen(
    repository: ShortlyRepository,
    language: AppLanguage,
    onBack: () -> Unit,
    onVideoClick: (videoId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val history by repository.getWatchHistory().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.watchHistory(language),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                    if (history.isNotEmpty()) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                repository.clearWatchHistory()
                            }
                            Toast.makeText(context, Strings.historyCleared(language), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = Strings.clearHistory(language),
                                tint = Color(0xFFFF453A)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShortlySurfaceDark
                )
            )
        },
        containerColor = Color.Black,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ShortlyTextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Strings.noWatchHistory(language),
                        color = ShortlyTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ShortlySurfaceDark)
                            .clickable { onVideoClick(item.videoId) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ShortlySurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "@${item.creatorUsername}",
                                color = ShortlyPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${item.viewsCount} ${Strings.views(language)} • ${timeFormat.format(Date(item.watchedAt))}",
                                color = ShortlyTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
