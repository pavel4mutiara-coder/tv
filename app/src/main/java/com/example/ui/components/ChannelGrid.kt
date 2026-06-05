package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.TvChannel
import kotlinx.coroutines.delay

sealed interface GridFetchState {
    object Loading : GridFetchState
    data class Success(val channels: List<TvChannel>) : GridFetchState
    data class Error(val message: String) : GridFetchState
}

@Composable
fun ChannelResponsiveGrid(
    channels: List<TvChannel>,
    selectedChannel: TvChannel?,
    onChannelSelect: (TvChannel) -> Unit,
    onToggleFavorite: (TvChannel) -> Unit,
    modifier: Modifier = Modifier,
    useSimulatedFetch: Boolean = true
) {
    var fetchState by remember(channels) {
        mutableStateOf<GridFetchState>(if (useSimulatedFetch) GridFetchState.Loading else GridFetchState.Success(channels))
    }

    if (useSimulatedFetch) {
        LaunchedEffect(channels) {
            fetchState = GridFetchState.Loading
            delay(900) // Beautiful simulated fetch delay to show shimmer cards
            fetchState = if (channels.isNotEmpty()) {
                GridFetchState.Success(channels)
            } else {
                GridFetchState.Success(emptyList())
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = fetchState,
            animationSpec = tween(durationMillis = 350),
            label = "GridFetchStateTransition"
        ) { state ->
            when (state) {
                is GridFetchState.Loading -> {
                    ShimmerChannelGrid()
                }
                is GridFetchState.Success -> {
                    if (state.channels.isEmpty()) {
                        EmptyGridState()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 152.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.channels, key = { it.id }) { ch ->
                                ChannelGridCard(
                                    channel = ch,
                                    isSelected = selectedChannel?.id == ch.id,
                                    onSelect = { onChannelSelect(ch) },
                                    onToggleFavorite = { onToggleFavorite(ch) }
                                )
                            }
                        }
                    }
                }
                is GridFetchState.Error -> {
                    ErrorGridState(message = state.message)
                }
            }
        }
    }
}

@Composable
fun ChannelGridCard(
    channel: TvChannel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var isFavoriteFocused by remember { mutableStateOf(false) }

    val pulseScale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "PulseScale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 2.dp,
        label = "ShadowElevation"
    )

    val containerBgColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> Color(0xFF1B1B22)
        },
        label = "ContainerBgColor"
    )

    val borderStroke = when {
        isFocused -> BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFF1C40F), Color(0xFFE67E22))))
        isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, Color(0xFF2C2C35))
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBgColor),
        border = borderStroke,
        modifier = modifier
            .aspectRatio(0.92f)
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER -> {
                            onSelect()
                            true
                        }
                        android.view.KeyEvent.KEYCODE_F,
                        android.view.KeyEvent.KEYCODE_BOOKMARK -> {
                            onToggleFavorite()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable { onSelect() }
            .clip(RoundedCornerShape(16.dp))
            .testTag("channel_grid_item_${channel.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Thumbnail Section
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF22222B), Color(0xFF14141A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (channel.logoUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = channel.logoUrl,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            error = {
                                GridLogoFallback(name = channel.name)
                            }
                        )
                    } else {
                        GridLogoFallback(name = channel.name)
                    }

                    // Selected Live indicator badge overlay on top left
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color.Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Text(
                                    text = "LIVE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Category badge overlay on top right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = channel.category.split(" ").firstOrNull() ?: channel.category,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom Content Section
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 28.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = channel.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = channel.country,
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Favorite Button overlaid on bottom-right of details
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                            .onFocusChanged { isFavoriteFocused = it.isFocused }
                            .background(
                                color = if (isFavoriteFocused) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .testTag("grid_favorite_${channel.id}")
                    ) {
                        Icon(
                            imageVector = if (channel.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (channel.isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridLogoFallback(name: String) {
    val char = if (name.isNotBlank()) name.first().uppercase() else "T"
    val hash = name.hashCode().coerceAtLeast(0)
    val grandGradient = when (hash % 4) {
        0 -> Brush.verticalGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))) // Blue-cyan
        1 -> Brush.verticalGradient(listOf(Color(0xFFF857A6), Color(0xFFFF5858))) // Pink-red
        2 -> Brush.verticalGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))) // Green
        else -> Brush.verticalGradient(listOf(Color(0xFF7F00FF), Color(0xFFE100FF))) // Purple-pink
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(grandGradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp
        )
    }
}

@Composable
fun ShimmerChannelGrid(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 152.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(8.dp),
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize()
    ) {
        items(8) {
            ShimmerGridItem()
        }
    }
}

@Composable
fun ShimmerGridItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
        border = BorderStroke(1.dp, Color(0xFF2C2C35)),
        modifier = Modifier
            .aspectRatio(0.92f)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = alphaAnim * 0.15f))
            )
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = alphaAnim * 0.15f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = alphaAnim * 0.1f))
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyGridState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.TvOff,
                contentDescription = "Empty channels icon",
                tint = Color.DarkGray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "কোনো লাইভ টিভি চ্যানেল পাওয়া যায়নি",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No active channels match your search query or filter category. Modify selection or try importing a playlist.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorGridState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error icon",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "চ্যানেল লোড করতে ব্যর্থ হয়েছে",
                color = MaterialTheme.colorScheme.error,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Error: $message\nPlease check your internet connection or reload.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
