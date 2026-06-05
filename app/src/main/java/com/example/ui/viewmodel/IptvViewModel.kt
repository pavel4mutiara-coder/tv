package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        IptvDatabase::class.java,
        "globus_tv_db"
    ).build()

    private val repository = IptvRepository(db.iptvDao())

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    private val prefs = application.getSharedPreferences("globus_tv_settings", android.content.Context.MODE_PRIVATE)
    val autoPlayOnSelect = MutableStateFlow(prefs.getBoolean("autoplay_on_select", true))

    fun setAutoPlayOnSelect(enabled: Boolean) {
        autoPlayOnSelect.value = enabled
        prefs.edit().putBoolean("autoplay_on_select", enabled).apply()
    }

    val currentSortOrder = MutableStateFlow(
        try {
            SortOrder.valueOf(prefs.getString("sort_order", SortOrder.TRENDING.name) ?: SortOrder.TRENDING.name)
        } catch (e: Exception) {
            SortOrder.TRENDING
        }
    )

    fun setSortOrder(order: SortOrder) {
        currentSortOrder.value = order
        prefs.edit().putString("sort_order", order.name).apply()
    }

    val playCounts = MutableStateFlow<Map<String, Int>>(emptyMap())

    fun incrementPlayCount(channelId: String) {
        val currentMap = playCounts.value.toMutableMap()
        val nextVal = (currentMap[channelId] ?: 0) + 1
        currentMap[channelId] = nextVal
        playCounts.value = currentMap
        prefs.edit().putInt("play_count_$channelId", nextVal).apply()
    }

    val playlists: StateFlow<List<PlaylistEntity>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val favoriteEntities = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<Set<String>> = favoriteEntities
        .map { list -> list.map { it.channelId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val customChannels: Flow<List<CustomChannelEntity>> = repository.getCustomChannels()

    val channels: StateFlow<List<TvChannel>> = combine(
        customChannels,
        favoriteIds
    ) { customs, favs ->
        val list = mutableListOf<TvChannel>()
        list.addAll(DefaultChannels.list.map { it.toTvChannel(it.id in favs) })
        list.addAll(customs.map { entity ->
            TvChannel(
                id = entity.streamUrl,
                name = entity.name,
                logoUrl = entity.logoUrl,
                streamUrl = entity.streamUrl,
                category = entity.category,
                country = entity.country,
                isCustom = true,
                isFavorite = entity.streamUrl in favs,
                description = "Custom IPTV Channel | Category: ${entity.category}",
                playlistUrl = entity.playlistUrl
            )
        })
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredChannels: StateFlow<List<TvChannel>> = combine(
        channels,
        searchQuery,
        selectedCategory,
        currentSortOrder,
        playCounts
    ) { allChannels, query, category, sortOrder, counts ->
        val filtered = allChannels.filter { chan ->
            val matchQuery = query.isEmpty() ||
                    chan.name.contains(query, ignoreCase = true) ||
                    chan.category.contains(query, ignoreCase = true) ||
                    chan.country.contains(query, ignoreCase = true)

            val matchCategory = when (category) {
                "All" -> true
                "Favorites (পছন্দসই)" -> chan.isFavorite
                else -> chan.category == category
            }

            matchQuery && matchCategory
        }

        when (sortOrder) {
            SortOrder.TRENDING -> {
                filtered.sortedWith(
                    compareByDescending<TvChannel> { counts[it.id] ?: 0 }
                        .thenBy { it.name }
                )
            }
            SortOrder.ALPHABETICAL -> {
                filtered.sortedBy { it.name.lowercase() }
            }
            SortOrder.COUNTRY -> {
                filtered.sortedWith(
                    compareBy<TvChannel> { it.country.lowercase() }
                        .thenBy { it.name.lowercase() }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChannel = MutableStateFlow<TvChannel?>(null)
    val selectedChannel: StateFlow<TvChannel?> = _selectedChannel.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    init {
        // Load persistent play counts
        val loaded = mutableMapOf<String, Int>()
        try {
            prefs.all.forEach { (key, value) ->
                if (key.startsWith("play_count_") && value is Int) {
                    val channelId = key.removePrefix("play_count_")
                    loaded[channelId] = value
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        playCounts.value = loaded

        viewModelScope.launch {
            channels.collectLatest { list ->
                if (_selectedChannel.value == null && list.isNotEmpty()) {
                    _selectedChannel.value = list.first()
                }
            }
        }
    }

    fun selectChannel(channel: TvChannel) {
        _selectedChannel.value = channel
        incrementPlayCount(channel.id)
    }

    fun toggleFavorite(channel: TvChannel) {
        viewModelScope.launch {
            if (channel.isFavorite) {
                repository.removeFavorite(channel.id)
            } else {
                repository.addFavorite(channel.id)
            }
        }
    }

    fun importM3u(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) {
            _importState.value = ImportState.Error("Playlist name and url link cannot be empty!")
            return
        }
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.importM3uPlaylist(name, url)
            result.onSuccess { count ->
                _importState.value = ImportState.Success("$count channels imported successfully!")
                selectedCategory.value = "All"
            }.onFailure { err ->
                _importState.value = ImportState.Error(err.message ?: "Failed to load M3U file!")
            }
        }
    }

    fun deleteCustomPlaylist(url: String) {
        viewModelScope.launch {
            repository.deletePlaylist(url)
            _selectedChannel.value?.let { active ->
                if (active.playlistUrl == url) {
                    _selectedChannel.value = channels.value.firstOrNull { it.playlistUrl != url }
                }
            }
        }
    }

    fun clearImportState() {
        _importState.value = ImportState.Idle
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IptvViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return IptvViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed interface ImportState {
    object Idle : ImportState
    object Loading : ImportState
    data class Success(val message: String) : ImportState
    data class Error(val message: String) : ImportState
}

enum class SortOrder(val displayNameEn: String, val displayNameBn: String) {
    TRENDING("Trending", "ট্রেন্ডিং (জনপ্রিয়)"),
    ALPHABETICAL("Alphabetical", "বর্ণানুক্রমিক (A-Z)"),
    COUNTRY("Country", "দেশ অনুযায়ী")
}
