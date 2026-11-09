package com.example.metadata

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchSongResult(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val artworkUrl: String? = null,
    val source: String // "iTunes" or "Deezer"
)

object MusicSearchRepository {
    private const val TAG = "MusicSearchRepository"
    private const val ITUNES_SEARCH_URL = "https://itunes.apple.com/search"
    private const val DEEZER_SEARCH_URL = "https://api.deezer.com/search"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // In-memory cache for fast search queries
    private val queryCache = mutableMapOf<String, List<SearchSongResult>>()

    suspend fun searchSongs(rawQuery: String): List<SearchSongResult> = withContext(Dispatchers.IO) {
        val cleanQuery = sanitize(rawQuery)
        if (cleanQuery.isBlank()) return@withContext emptyList()

        queryCache[cleanQuery]?.let { cached ->
            return@withContext cached
        }

        // 1. First priority: iTunes Search API
        val itunesList = searchItunes(cleanQuery)
        if (itunesList.isNotEmpty()) {
            queryCache[cleanQuery] = itunesList
            Log.d(TAG, "iTunes found ${itunesList.size} results for: $cleanQuery")
            return@withContext itunesList
        }

        // 2. Second priority / Fallback: Deezer Search API (or Spotify search fallback)
        val deezerList = searchDeezer(cleanQuery)
        if (deezerList.isNotEmpty()) {
            queryCache[cleanQuery] = deezerList
            Log.d(TAG, "Deezer found ${deezerList.size} results for: $cleanQuery")
            return@withContext deezerList
        }

        Log.d(TAG, "No results found for query: $cleanQuery on iTunes or Deezer")
        emptyList()
    }

    private fun searchItunes(query: String): List<SearchSongResult> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "$ITUNES_SEARCH_URL?term=$encoded&media=music&entity=song&limit=12"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val count = json.optInt("resultCount", 0)
            if (count <= 0) return emptyList()

            val resultsArray = json.getJSONArray("results")
            val list = mutableListOf<SearchSongResult>()

            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val trackName = item.optString("trackName", "").trim()
                val artistName = item.optString("artistName", "").trim()
                if (trackName.isEmpty() || artistName.isEmpty()) continue

                val collectionName = item.optString("collectionName", "").ifEmpty { null }
                val artwork100 = item.optString("artworkUrl100", "")
                val highResArtwork = if (artwork100.isNotEmpty()) {
                    artwork100
                        .replace("100x100bb.jpg", "600x600bb.jpg")
                        .replace("100x100bb.png", "600x600bb.png")
                        .replace(Regex("/\\d+x\\d+bb\\."), "/600x600bb.")
                } else null

                list.add(
                    SearchSongResult(
                        id = item.optString("trackId", "${trackName}_$i"),
                        title = trackName,
                        artist = artistName,
                        album = collectionName,
                        artworkUrl = highResArtwork,
                        source = "iTunes"
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Error querying iTunes for '$query'", e)
            emptyList()
        }
    }

    private fun searchDeezer(query: String): List<SearchSongResult> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "$DEEZER_SEARCH_URL?q=$encoded&limit=12"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return emptyList()

            val list = mutableListOf<SearchSongResult>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val title = item.optString("title", "").trim()
                val artistObj = item.optJSONObject("artist")
                val artistName = artistObj?.optString("name", "")?.trim() ?: ""

                if (title.isEmpty() || artistName.isEmpty()) continue

                val albumObj = item.optJSONObject("album")
                val albumTitle = albumObj?.optString("title", "")?.ifEmpty { null }
                val coverBig = albumObj?.optString("cover_big", "")
                val coverMed = albumObj?.optString("cover_medium", "")
                val artwork = if (!coverBig.isNullOrEmpty()) coverBig else coverMed

                list.add(
                    SearchSongResult(
                        id = item.optString("id", "${title}_$i"),
                        title = title,
                        artist = artistName,
                        album = albumTitle,
                        artworkUrl = artwork?.ifEmpty { null },
                        source = "Deezer"
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Error querying Deezer for '$query'", e)
            emptyList()
        }
    }

    private fun sanitize(raw: String): String {
        return raw
            .replace("_", " ")
            .replace("-", " ")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
