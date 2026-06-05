package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TvChannel
import com.example.ui.components.ChannelListItem
import com.example.ui.components.IptvVideoPlayer
import com.example.ui.components.PlaylistImportSection
import com.example.ui.viewmodel.IptvViewModel

enum class TvTab(val titleBn: String, val titleEn: String, val icon: ImageVector) {
    LIVE_TV("সরাসরি টিভি", "Live TV", Icons.Filled.LiveTv),
    IMPORT("প্লেলিস্ট যোগ", "Import IPTV", Icons.Filled.CloudDownload),
    GUIDE("নির্দেশিকা", "User Guide", Icons.Filled.HelpOutline)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val channels by viewModel.filteredChannels.collectAsState()
    val rawChannels by viewModel.channels.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var currentTab by remember { mutableStateOf(TvTab.LIVE_TV) }

    // Categories calculation
    val categories = remember(rawChannels, favoriteIds) {
        val list = mutableListOf("All")
        if (favoriteIds.isNotEmpty()) {
            list.add("Favorites (পছন্দসই)")
        }
        val channelCats = rawChannels.map { it.category }.distinct().sorted()
        list.addAll(channelCats)
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LiveTv,
                            contentDescription = "Globus logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "GLOBUS TV",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "গ্লোবাল লাইভ আইপিটিভি প্ল্যাটফর্ম",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Small responsive status indicator
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(Color.Red.copy(alpha = 0.15f), shape = CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                        Text(
                            text = "${rawChannels.size} CH ACTIVE",
                            color = Color.Red,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F11),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Sleek material bottom navigation bar (only in portrait, landscape can render a side menu or compact dock)
            if (!isLandscape) {
                NavigationBar(
                    containerColor = Color(0xFF0F0F11),
                    tonalElevation = 8.dp
                ) {
                    TvTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon as ImageVector,
                                    contentDescription = tab.titleEn
                                )
                            },
                            label = {
                                Text(
                                    text = tab.titleBn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF070709),
        modifier = modifier
    ) { innerPadding ->
        if (isLandscape) {
            // TV/Tablet Layout: Multi-pane split screeen
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF070709))
            ) {
                // TV Side Tabs Selector (Side Navigation rail replacement)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(76.dp)
                        .background(Color(0xFF0F0F11))
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TvTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentTab = tab }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon as ImageVector,
                                contentDescription = tab.titleEn,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.titleBn,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Sub-screen section based on selected side tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentTab) {
                        TvTab.LIVE_TV -> {
                            // Split: Player on Left, Channels drawer on Right
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Player & Info block
                                Column(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .fillMaxHeight()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IptvVideoPlayer(
                                        channel = selectedChannel,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(Color.Black)
                                    )

                                    // Selected Channel Description Card
                                    selectedChannel?.let { ch ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.15f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(Color.Green, shape = CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = ch.name,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "ক্যাটাগরি: ${ch.category} • দেশ: ${ch.country}",
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = ch.description,
                                                        color = Color.LightGray,
                                                        fontSize = 11.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.toggleFavorite(ch) },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (ch.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                        contentDescription = "Fav",
                                                        tint = if (ch.isFavorite) Color.Red else Color.LightGray,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive Sidebar Channel List Drawer
                                VerticalChannelDrawer(
                                    channels = channels,
                                    categories = categories,
                                    selectedCategory = selectedCategory,
                                    selectedChannel = selectedChannel,
                                    searchQuery = searchQuery,
                                    onSearchChange = { viewModel.searchQuery.value = it },
                                    onCategorySelect = { viewModel.selectedCategory.value = it },
                                    onChannelSelect = { viewModel.selectChannel(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(Color(0xFF0C0C0F))
                                        .padding(8.dp)
                                )
                            }
                        }
                        TvTab.IMPORT -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item { PlaylistImportSection(viewModel = viewModel) }
                            }
                        }
                        TvTab.GUIDE -> {
                            UserGuideScreen()
                        }
                    }
                }
            }
        } else {
            // Mobile portrait layout: normal screens flow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF070709))
            ) {
                when (currentTab) {
                    TvTab.LIVE_TV -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Static Video Player
                            IptvVideoPlayer(channel = selectedChannel)

                            // Playback info bar
                            selectedChannel?.let { ch ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F0F11))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ch.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Category: ${ch.category} • Country: ${ch.country}",
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(ch) }
                                    ) {
                                        Icon(
                                            imageVector = if (ch.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Fav",
                                            tint = if (ch.isFavorite) Color.Red else Color.Gray
                                        )
                                    }
                                }
                            }

                            // Dynamic channels scroll section below player
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                // Search bar
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.searchQuery.value = it },
                                    placeholder = { Text("চ্যানেল খুঁজুন (Search Live TV...)", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.Gray)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .testTag("search_bar"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    singleLine = true
                                )

                                // Horizontal Category Carousel
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(categories) { cat ->
                                        val isSelected = selectedCategory == cat
                                        val chipColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray.copy(alpha = 0.5f)
                                        val textColor = if (isSelected) Color.White else Color.LightGray

                                        Surface(
                                            shape = MaterialTheme.shapes.medium,
                                            color = chipColor,
                                            modifier = Modifier.clickable {
                                                viewModel.selectedCategory.value = cat
                                            }
                                        ) {
                                            Text(
                                                text = cat,
                                                color = textColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Interactive Channel matches list
                                if (channels.isNotEmpty()) {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(channels) { ch ->
                                            ChannelListItem(
                                                channel = ch,
                                                isSelected = selectedChannel?.id == ch.id,
                                                onSelect = { viewModel.selectChannel(ch) },
                                                onToggleFavorite = { viewModel.toggleFavorite(ch) }
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Filled.TvOff,
                                                contentDescription = null,
                                                tint = Color.DarkGray,
                                                modifier = Modifier.size(52.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("কোনো লাইভ চ্যানেল খুঁজে পাওয়া যায়নি", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Try expanding filters or search query.", color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TvTab.IMPORT -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { PlaylistImportSection(viewModel = viewModel) }
                        }
                    }
                    TvTab.GUIDE -> {
                        UserGuideScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalChannelDrawer(
    channels: List<TvChannel>,
    categories: List<String>,
    selectedCategory: String,
    selectedChannel: TvChannel?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onChannelSelect: (TvChannel) -> Unit,
    onToggleFavorite: (TvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search TV channels...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.DarkGray
            ),
            singleLine = true
        )

        // Categories Header Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray.copy(alpha = 0.4f)
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = bgColor,
                    modifier = Modifier.clickable { onCategorySelect(cat) }
                ) {
                    Text(
                        text = cat,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Divider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))

        // Large Channels List
        if (channels.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels) { ch ->
                    ChannelListItem(
                        channel = ch,
                        isSelected = selectedChannel?.id == ch.id,
                        onSelect = { onChannelSelect(ch) },
                        onToggleFavorite = { onToggleFavorite(ch) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.TvOff, contentDescription = null, tint = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No channels match filter.", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun UserGuideScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ব্যবহারকারী গাইড ও নির্দেশনা (IPTV User Guidelines)",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Globus TV প্লেয়ারে কীভাবে যেকোনো ফ্রি এবং প্রিমিয়াম স্পোর্টস বা বৈশ্বিক চ্যানেল যুক্ত করে উপভোগ করবেন তা জানুন।",
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Help, contentDescription = null, tint = Color.Yellow)
                        Text("১. প্লেলিস্ট এবং কাস্টম চ্যানেল কী?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "গ্লোবাল বা প্রিমিয়াম পে-চ্যানেল (যেমন খেলাধুলার BeIN Sports, Star Sports, Sky Sports) সারা পৃথিবী জুড়ে বিনামূল্যে স্ট্রিম করার সুবিধার্থে বিভিন্ন অনলাইন ফোরামে ফ্রি IPTV লিঙ্ক পাওয়া যায়। এই লিঙ্কগুলো .m3u ফরমেটে ফাইল বা ইউআরএল হয়ে থাকে। আমদের বিল্ট-ইন প্লেলিস্ট ইম্পোর্টার এই লিঙ্কগুলো রিড করে সেকেন্ডের মধ্যে লোড করতে পারে।",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.FeaturedPlayList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("২. নতুন প্লেলিস্ট ইম্পোর্ট করবেন কীভাবে?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• নিচের ট্যাব থেকে 'প্লেলিস্ট যোগ' (Import IPTV) অপশনে যান।\n" +
                                "• নাম দিন (যেমন: 'Sports Pack' বা 'My Channels')।\n" +
                                "• ইন্টারনেট থেকে সংগৃহীত .m3u লিঙ্কটি পেস্ট করুন।\n" +
                                "• 'প্লেলিস্ট কানেক্ট করুন' (Load IPTV) বাটনে চাপ দিন।\n" +
                                "• মাত্র ১ ক্লিকে চ্যানেলগুলো ইম্পোর্ট করতে আমাদের প্রস্তাবিত ফ্রি প্যাক-গুলো ও ব্যবহার করতে পারেন!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.SettingsRemote, contentDescription = null, tint = Color.Green)
                        Text("৩. অ্যান্ড্রয়েড টিভি এবং কি-বোর্ড সামঞ্জস্যতা", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "এই অ্যাপ্লিকেশনটি সম্পূর্ণ রেসপন্সিভ এবং অ্যাডাপ্টিভ। যে কোনো অ্যান্ড্রয়েড টিভিতে ইন্সটল করলে রিমোট কন্ট্রোলারের D-pad বা তীরচিহ্ন সমূহের সাহায্যে অনায়াসে চ্যানেল সিলেক্ট এবং স্ক্রোল করতে পারবেন। স্ক্রিনের যেকোনো প্রান্ত থেকে প্রফেশনাল ও স্মুথ অভিজ্ঞতা পাবেন।",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Globus TV Live Player v1.0 • Built with 100% Security & Open Access",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
