package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.TvChannel

@Composable
fun ChannelListItem(
    channel: TvChannel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var isFavoriteButtonFocused by remember { mutableStateOf(false) }

    val containerBg by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else -> Color.DarkGray.copy(alpha = 0.2f)
        },
        label = "containerBg"
    )

    val borderBrush = when {
        isFocused -> Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFF1C40F), // Bright Yellow highlight for TV remote focus
                Color(0xFFE67E22)
            )
        )
        isSelected -> Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )
        else -> null
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerBg),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val keyCode = keyEvent.nativeKeyEvent.keyCode
                    when (keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER -> {
                            onSelect()
                            true
                        }
                        android.view.KeyEvent.KEYCODE_F,
                        android.view.KeyEvent.KEYCODE_5,
                        android.view.KeyEvent.KEYCODE_NUMPAD_5,
                        android.view.KeyEvent.KEYCODE_PROG_YELLOW,
                        android.view.KeyEvent.KEYCODE_BOOKMARK -> {
                            onToggleFavorite()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .clickable { onSelect() }
            .clip(MaterialTheme.shapes.medium)
            .testTag("channel_item_${channel.id}"),
        border = borderBrush?.let {
            androidx.compose.foundation.BorderStroke(2.dp, it)
        }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Fallback Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.DarkGray, Color.Black)
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
                            .padding(4.dp),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        error = {
                            LogoFallback(name = channel.name)
                        }
                    )
                } else {
                    LogoFallback(name = channel.name)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Channel Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Active badge
                    if (isSelected) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = Color.Red,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "সরাসরি লাইভ • LIVE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.DarkGray.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = channel.category,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = channel.country,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Toggle favorite (also focusable for D-pad spatial targeting)
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .testTag("favorite_button_${channel.id}")
                    .onFocusChanged { isFavoriteButtonFocused = it.isFocused }
                    .background(
                        color = if (isFavoriteButtonFocused) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (channel.isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LogoFallback(name: String) {
    val char = if (name.isNotBlank()) name.first().uppercase() else "T"
    val hash = name.hashCode().coerceAtLeast(0)
    val startColor = when (hash % 4) {
        0 -> Color(0xFF1565C0) // Blue
        1 -> Color(0xFFD84315) // Orange/Red
        2 -> Color(0xFF2E7D32) // Green
        else -> Color(0xFF6A1B9A) // Purple
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(startColor, startColor.copy(alpha = 0.6f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}
