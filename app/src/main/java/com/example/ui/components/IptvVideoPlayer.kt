package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.TvChannel
import kotlinx.coroutines.*

@OptIn(UnstableApi::class)
@Composable
fun IptvVideoPlayer(
    channel: TvChannel?,
    autoPlayEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    useController: Boolean = true,
    onStreamFinishedOrFailed: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var originalAspectMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    var isManualPlayTriggered by remember(channel) { mutableStateOf(false) }
    val shouldStream = autoPlayEnabled || isManualPlayTriggered

    // Initialize ExoPlayer with standard Web User-Agent to bypass provider blocks and enable HLS redirection
    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory))
            .build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    var playbackState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Listen for events
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                if (state == Player.STATE_READY) {
                    errorMessage = null
                } else if (state == Player.STATE_ENDED) {
                    onStreamFinishedOrFailed?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackState = Player.STATE_IDLE
                val details = error.localizedMessage ?: error.message ?: "No error details available"
                errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                        "নেটওয়ার্ক সংযোগ ব্যর্থ হয়েছে! আপনার ইন্টারনেট কনেকশন চেক করুন।\nNetwork disconnected. Please check internet connection.\n\nError details: $details (Code: ${error.errorCodeName})"
                    }
                    else -> {
                        "এই লাইভ স্ট্রিমটি বর্তমানে অফলাইন বা অনুপলব্ধ রয়েছে।\nThis channel stream is currently offline or unavailable.\n\nError details: $details (Code: ${error.errorCodeName})"
                    }
                }
                scope.launch {
                    delay(3000)
                    // Double check if error state is still active so we don't skip channels mid-seek
                    if (playbackState == Player.STATE_IDLE) {
                        onStreamFinishedOrFailed?.invoke()
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Set new stream when channel changes or when streaming becomes active
    LaunchedEffect(channel, shouldStream) {
        if (channel != null && shouldStream) {
            errorMessage = null
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            
            // Force HLS MimeType (APPLICATION_M3U8) so ExoPlayer knows to use HlsMediaSource immediately
            val mediaItem = MediaItem.Builder()
                .setUri(channel.streamUrl)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
                
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        if (channel != null && errorMessage == null) {
            if (shouldStream) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            this.useController = false // We'll render our own customized elegant overlays
                            resizeMode = originalAspectMode
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        view.resizeMode = originalAspectMode
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Dynamic Overlay controls (Fades after inactivity)
                var showControls by remember { mutableStateOf(true) }
                LaunchedEffect(showControls) {
                    if (showControls) {
                        delay(4000)
                        showControls = false
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showControls = !showControls }
                ) {
                    if (showControls) {
                        // Dark shading for overlay readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        )

                        // Top channel name badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.TopStart),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(8.dp)
                                        .background(Color.Red, shape = MaterialTheme.shapes.extraSmall)
                                )
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            // Aspect ratio badge button
                            Button(
                                onClick = {
                                    originalAspectMode = when (originalAspectMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.8f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = when (originalAspectMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> "Fit (১৬:৯)"
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Stretch (পূর্ণ)"
                                        else -> "Zoom (জুম)"
                                    },
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Centered Big Play/Pause Button
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.large)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Bottom stream parameters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.BottomStart),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Stream • ${channel.country}",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )

                            Text(
                                text = "1080p FHD HLS",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                // Focusable and click-triggerable standby UI - Saves huge bandwidth!
                var isStandbyFocused by remember { mutableStateOf(false) }
                val standbyBorderBrush = if (isStandbyFocused) {
                    Brush.horizontalGradient(listOf(Color(0xFFF1C40F), Color(0xFFE67E22)))
                } else {
                    null
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onFocusChanged { isStandbyFocused = it.isFocused }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                val keyCode = keyEvent.nativeKeyEvent.keyCode
                                if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                    keyCode == android.view.KeyEvent.KEYCODE_ENTER
                                ) {
                                    isManualPlayTriggered = true
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }
                        .then(
                            if (standbyBorderBrush != null) Modifier.border(2.dp, standbyBorderBrush, MaterialTheme.shapes.medium) else Modifier
                        )
                        .clickable { isManualPlayTriggered = true }
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F0F14), Color(0xFF1F1F2C))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Tap to Play",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = channel.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Auto-play is toggled off | Category: ${channel.category}",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { isManualPlayTriggered = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "স্ট্রিম চালু করুন (Click to Play)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "সংযুক্ত হতে সেন্টার বোতাম বা ক্লিক চাপুন\nPress D-pad Center or Click to stream live content",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Buffer Loading Overlay
        if (playbackState == Player.STATE_BUFFERING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "বাফারিং হচ্ছে... লোড করা হচ্ছে...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Buffering live broadcast...",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Empty / No selected channel placeholder
        if (channel == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Tv,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কোনো লাইভ চ্যানেল সিলেক্ট করা নেই",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Select a channel below to watch.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Error playback Screen
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = channel?.name ?: "Error",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (channel != null) {
                                errorMessage = null
                                exoPlayer.stop()
                                exoPlayer.clearMediaItems()
                                exoPlayer.setMediaItem(MediaItem.fromUri(channel.streamUrl))
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("পুনরায় চেষ্টা করুন (Retry Playback)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
