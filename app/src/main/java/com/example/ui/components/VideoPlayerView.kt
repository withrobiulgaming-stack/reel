package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.ShortlyPrimary

@Composable
fun VideoPlayerView(
    videoUrl: String,
    thumbnailUrl: String,
    isPlaying: Boolean,
    onDoubleTap: () -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier,
    onVideoCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isMuted by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surface by remember { mutableStateOf<Surface?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Lifecycle aware playback with defensive error suppression
    DisposableEffect(lifecycleOwner, videoUrl) {
        val player = MediaPlayer().apply {
            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                isLooping = true
                setOnPreparedListener { mp ->
                    isPrepared = true
                    isBuffering = false
                    hasError = false
                    try {
                        if (isPlaying) {
                            mp.start()
                        }
                    } catch (e: Exception) {
                        hasError = true
                    }
                }
                setOnBufferingUpdateListener { _, percent ->
                    if (percent >= 90) isBuffering = false
                }
                setOnCompletionListener {
                    onVideoCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    // Catch and suppress MediaPlayerNative errors (e.g. 1, -2147483648)
                    hasError = true
                    isBuffering = false
                    isPrepared = false
                    true // Return true to indicate error was handled safely
                }
            } catch (e: Exception) {
                hasError = true
                isBuffering = false
            }
        }
        mediaPlayer = player

        try {
            if (videoUrl.isNotBlank()) {
                player.setDataSource(context, Uri.parse(videoUrl))
                player.prepareAsync()
            } else {
                hasError = true
                isBuffering = false
            }
        } catch (e: Exception) {
            hasError = true
            isBuffering = false
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        if (isPrepared && !hasError && player.isPlaying) {
                            player.pause()
                        }
                    } catch (e: Exception) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        if (isPlaying && isPrepared && !hasError && !player.isPlaying) {
                            player.start()
                        }
                    } catch (e: Exception) {}
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    } catch (e: Exception) {}
                    try {
                        player.reset()
                        player.release()
                    } catch (e: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                surface?.release()
                surface = null
            } catch (e: Exception) {}
            try {
                if (player.isPlaying) {
                    player.pause()
                }
            } catch (e: Exception) {}
            try {
                player.reset()
                player.release()
            } catch (e: Exception) {}
            mediaPlayer = null
        }
    }

    // Play / Pause reactivity
    LaunchedEffect(isPlaying, isPrepared, hasError) {
        if (hasError) return@LaunchedEffect
        mediaPlayer?.let { player ->
            try {
                if (isPrepared) {
                    if (isPlaying) {
                        if (!player.isPlaying) {
                            player.start()
                        }
                    } else {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    }
                }
            } catch (e: Exception) {
                hasError = true
            }
        }
    }

    // Mute reactivity
    LaunchedEffect(isMuted, isPrepared, hasError) {
        if (hasError) return@LaunchedEffect
        mediaPlayer?.let { player ->
            try {
                if (isPrepared) {
                    val vol = if (isMuted) 0f else 1f
                    player.setVolume(vol, vol)
                }
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onTap = { onSingleTap() }
                )
            }
    ) {
        // TextureView for hardware video rendering (only rendered when no error)
        if (!hasError) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(st: android.graphics.SurfaceTexture, width: Int, height: Int) {
                                try {
                                    val surf = Surface(st)
                                    surface = surf
                                    if (mediaPlayer != null && !hasError) {
                                        mediaPlayer?.setSurface(surf)
                                    }
                                } catch (e: Exception) {}
                            }

                            override fun onSurfaceTextureSizeChanged(st: android.graphics.SurfaceTexture, width: Int, height: Int) {}

                            override fun onSurfaceTextureDestroyed(st: android.graphics.SurfaceTexture): Boolean {
                                try {
                                    surface?.release()
                                    surface = null
                                    mediaPlayer?.setSurface(null)
                                } catch (e: Exception) {}
                                return true
                            }

                            override fun onSurfaceTextureUpdated(st: android.graphics.SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Thumbnail underlay / graceful fallback on loading or network/codec error
        if (!isPrepared || isBuffering || hasError) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Ambient Dark Gradients (Top & Bottom for TikTok/Reels aesthetic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Buffering Indicator
        if (isBuffering && !hasError) {
            CircularProgressIndicator(
                color = ShortlyPrimary,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }

        // Pause Overlay Icon
        AnimatedVisibility(
            visible = !isPlaying || hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Mute / Unmute Button
        IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.4f), shape = androidx.compose.foundation.shape.CircleShape)
                .size(36.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
