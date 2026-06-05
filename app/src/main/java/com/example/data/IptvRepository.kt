package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

class IptvRepository(private val dao: IptvDao) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    fun getFavorites(): Flow<List<FavoriteEntity>> = dao.getFavorites()

    suspend fun addFavorite(channelId: String) {
        dao.addFavorite(FavoriteEntity(channelId))
    }

    suspend fun removeFavorite(channelId: String) {
        dao.removeFavorite(channelId)
    }

    suspend fun isFavoriteSync(channelId: String): Boolean {
        return dao.isFavoriteSync(channelId)
    }

    fun getPlaylists(): Flow<List<PlaylistEntity>> = dao.getPlaylists()

    fun getCustomChannels(): Flow<List<CustomChannelEntity>> = dao.getCustomChannels()

    suspend fun deletePlaylist(playlistUrl: String) {
        dao.deletePlaylist(playlistUrl)
        dao.deleteCustomChannelsByPlaylist(playlistUrl)
    }

    suspend fun importM3uPlaylist(name: String, url: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))

                val reader = BufferedReader(InputStreamReader(body.byteStream()))
                var line: String?
                val list = mutableListOf<CustomChannelEntity>()

                var currentName = ""
                var currentLogo = ""
                var currentCategory = "General"
                var currentCountry = "Global"

                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.startsWith("#EXTINF:")) {
                        // Reset line details
                        currentName = ""
                        currentLogo = ""
                        currentCategory = "General"
                        currentCountry = "Global"

                        currentLogo = extractAttribute(trimmed, "tvg-logo") ?: extractAttribute(trimmed, "logo") ?: ""
                        currentCategory = extractAttribute(trimmed, "group-title") ?: extractAttribute(trimmed, "category") ?: "M3U Loaded"
                        currentCountry = extractAttribute(trimmed, "tvg-country") ?: extractAttribute(trimmed, "country") ?: "Global"

                        val commaIdx = trimmed.lastIndexOf(',')
                        if (commaIdx != -1 && commaIdx < trimmed.length - 1) {
                            currentName = trimmed.substring(commaIdx + 1).trim()
                        }
                    } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        val streamUrl = trimmed
                        if (currentName.isEmpty()) {
                            currentName = "Loaded Channel ${list.size + 1}"
                        }
                        list.add(
                            CustomChannelEntity(
                                streamUrl = streamUrl,
                                name = currentName,
                                logoUrl = currentLogo,
                                category = currentCategory,
                                country = currentCountry,
                                playlistUrl = url
                            )
                        )
                        currentName = ""
                    }
                }

                if (list.isNotEmpty()) {
                    dao.addPlaylist(PlaylistEntity(playlistUrl = url, playlistName = name))
                    dao.deleteCustomChannelsByPlaylist(url)
                    dao.addCustomChannels(list)
                    Result.success(list.size)
                } else {
                    Result.failure(Exception("No streaming channels found in this M3U file! Make sure it contains valid Stream URLs."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractAttribute(line: String, attrName: String): String? {
        val needleKey = "$attrName=\""
        val startIdx = line.indexOf(needleKey)
        if (startIdx == -1) return null
        val valStart = startIdx + needleKey.length
        val endIdx = line.indexOf('"', valStart)
        if (endIdx == -1) return null
        return line.substring(valStart, endIdx)
    }
}
