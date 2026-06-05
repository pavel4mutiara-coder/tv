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
        favoriteIds
    ) { allChannels, query, category, favs ->
        allChannels.filter { chan ->
            val matchQuery = query.isEmpty() ||
                    chan.name.contains(query, ignoreCase = true) ||
                    chan.category.contains(query, ignoreCase = true) ||
                    chan.country.contains(query, ignoreCase = true)

            val matchCategory = when (category) {
                "All" -> true
                "Favorites (পছন্দসই)" -> chan.id in favs
                else -> chan.category == category
            }

            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChannel = MutableStateFlow<TvChannel?>(null)
    val selectedChannel: StateFlow<TvChannel?> = _selectedChannel.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    init {
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
