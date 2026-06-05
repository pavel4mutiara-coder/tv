package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlaylistEntity
import com.example.ui.viewmodel.ImportState
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun PlaylistImportSection(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val importState by viewModel.importState.collectAsState()

    var playlistName by remember { mutableStateOf("") }
    var playlistUrl by remember { mutableStateOf("") }

    val suggestedLists = listOf(
        SuggestedUrl(
            title = "বাংলাদেশ টিভি চ্যানেল সমূহ (Bangladesh FTA)",
            description = "All free television broadcasts of Bangladesh",
            url = "https://iptv-org.github.io/iptv/countries/bd.m3u",
            name = "Bangladesh FTA TV"
        ),
        SuggestedUrl(
            title = "গ্লোবাল স্পোর্টস লাইভ (Global Live Sports)",
            description = "Sports broadcasts and event channels worldwide",
            url = "https://iptv-org.github.io/iptv/categories/sports.m3u",
            name = "Global Sports Pack"
        ),
        SuggestedUrl(
            title = "গ্লোবাল ক্যাটাগরি প্যাক (Global General TV)",
            description = "Comprehensive list of news, moves, entertainment",
            url = "https://iptv-org.github.io/iptv/index.m3u",
            name = "Global All-in-One IPTV"
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "কাস্টম IPTV প্লেলিস্ট যোগ করুন (Import M3U Playlist)",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // Text Fields
        OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text("প্লেলিস্টের নাম (e.g. BD Free TV)") },
            leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = playlistUrl,
            onValueChange = { playlistUrl = it },
            label = { Text("এম৩ইউ লিঙ্ক (M3U Website Link / URL)") },
            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true
        )

        // Action Buttons & loading indicators
        when (importState) {
            is ImportState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(
                            text = "প্লেলিস্ট থেকে চ্যানেল পার্স করে নামানো হচ্ছে... একটু অপেক্ষা করুন (IPTV Analyzing & Loading...)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            else -> {
                Button(
                    onClick = {
                        viewModel.importM3u(playlistName, playlistUrl)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("প্লেলিস্ট কানেক্ট করুন (Load IPTV)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Import Status States
        AnimatedVisibility(visible = importState is ImportState.Success || importState is ImportState.Error) {
            val bgColor = if (importState is ImportState.Success) Color(0xFF1B5E20) else Color(0xFFB71C1C)
            val icon = if (importState is ImportState.Success) Icons.Filled.CheckCircle else Icons.Filled.Error

            val text = when (importState) {
                is ImportState.Success -> (importState as ImportState.Success).message
                is ImportState.Error -> (importState as ImportState.Error).message
                else -> ""
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(bgColor.copy(alpha = 0.2f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = if (importState is ImportState.Success) Color.Green else Color.Red)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = { viewModel.clearImportState() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        Divider(color = Color.DarkGray, thickness = 1.dp)

        // Presets card
        Text(
            text = "১-ক্লিক প্রস্তাবিত ফ্রি প্যাক (One-Click Dynamic Suggested Presets):",
            fontSize = 13.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold
        )

        suggestedLists.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        playlistName = item.name
                        playlistUrl = item.url
                    }
                    .clip(MaterialTheme.shapes.medium)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddToQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(item.description, color = Color.Gray, fontSize = 10.sp)
                        Text(item.url, color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, maxLines = 1)
                    }
                }
            }
        }

        // Active Playlists List
        if (playlists.isNotEmpty()) {
            Divider(color = Color.DarkGray, thickness = 1.dp)
            Text(
                text = "সংযুক্ত প্লেলিস্টসমূহ (Connected Playlists):",
                fontSize = 13.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )

            playlists.forEach { pl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.DarkGray.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(pl.playlistName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(pl.playlistUrl, color = Color.Gray, fontSize = 9.sp, maxLines = 1)
                        }
                    }

                    IconButton(onClick = { viewModel.deleteCustomPlaylist(pl.playlistUrl) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

data class SuggestedUrl(
    val title: String,
    val description: String,
    val url: String,
    val name: String
)
