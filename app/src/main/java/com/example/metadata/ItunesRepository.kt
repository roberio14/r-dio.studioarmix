package com.example.metadata

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class TrackMetadata(
    val title: String,
    val artist: String,
    val album: String? = null,
    val artworkUrl: String? = null
)

object ItunesRepository {
    private const val TAG = "ItunesRepository"
    private const val ITUNES_SEARCH_URL = "https://itunes.apple.com/search"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // In-memory cache to avoid redundant API queries for the same song
    private val cache = mutableMapOf<String, TrackMetadata>()

    suspend fun searchTrack(query: String): TrackMetadata? = withContext(Dispatchers.IO) {
        val cleanQuery = sanitizeQuery(query)
        if (cleanQuery.isBlank()) return@withContext null

        cache[cleanQuery]?.let {
            return@withContext it
        }

        try {
            val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
            val url = "$ITUNES_SEARCH_URL?term=$encodedQuery&media=music&entity=song&limit=1"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext null

            val json = JSONObject(bodyString)
            val resultCount = json.optInt("resultCount", 0)
            if (resultCount > 0) {
                val resultsArray = json.getJSONArray("results")
                val item = resultsArray.getJSONObject(0)

                val trackName = item.optString("trackName", "")
                val artistName = item.optString("artistName", "")
                val collectionName = item.optString("collectionName", "")
                val artwork100 = item.optString("artworkUrl100", "")

                // Convert 100x100 thumbnail to 600x600 high-res album art
                val highResArtwork = if (artwork100.isNotEmpty()) {
                    artwork100
                        .replace("100x100bb.jpg", "600x600bb.jpg")
                        .replace("100x100bb.png", "600x600bb.png")
                        .replace(Regex("/\\d+x\\d+bb\\."), "/600x600bb.")
                } else null

                val metadata = TrackMetadata(
                    title = if (trackName.isNotEmpty()) trackName else query,
                    artist = if (artistName.isNotEmpty()) artistName else "Rádio Studio Armix",
                    album = collectionName.ifEmpty { null },
                    artworkUrl = highResArtwork
                )

                cache[cleanQuery] = metadata
                Log.d(TAG, "iTunes matched: $metadata")
                return@withContext metadata
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching metadata from iTunes for query: $cleanQuery", e)
        }

        return@withContext null
    }

    private fun sanitizeQuery(raw: String): String {
        return raw
            .replace("_", " ")
            .replace("-", " ")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
