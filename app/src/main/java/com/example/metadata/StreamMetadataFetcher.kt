package com.example.metadata

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object StreamMetadataFetcher {
    private const val TAG = "StreamMetadataFetcher"

    suspend fun fetchCurrentStreamTitle(streamUrl: String): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(streamUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Icy-MetaData", "1")
                setRequestProperty("User-Agent", "VLC/3.0.18")
                connectTimeout = 4000
                readTimeout = 4000
                instanceFollowRedirects = true
            }
            connection.connect()

            val metaint = connection.getHeaderField("icy-metaint")?.toIntOrNull() ?: 0
            if (metaint <= 0) {
                Log.d(TAG, "No icy-metaint header present on stream")
                return@withContext null
            }

            inputStream = connection.inputStream

            // Skip audio data block to reach metadata block
            var bytesToSkip = metaint.toLong()
            while (bytesToSkip > 0) {
                val skipped = inputStream.skip(bytesToSkip)
                if (skipped <= 0) {
                    val readByte = inputStream.read()
                    if (readByte == -1) break
                    bytesToSkip -= 1
                } else {
                    bytesToSkip -= skipped
                }
            }

            // Read metadata length byte (multiplied by 16)
            val metaLenByte = inputStream.read()
            if (metaLenByte <= 0) {
                return@withContext null
            }

            val metaSize = metaLenByte * 16
            val buffer = ByteArray(metaSize)
            var totalRead = 0
            while (totalRead < metaSize) {
                val read = inputStream.read(buffer, totalRead, metaSize - totalRead)
                if (read == -1) break
                totalRead += read
            }

            val metaString = String(buffer, 0, totalRead, Charsets.UTF_8)
            Log.d(TAG, "Raw ICY metadata string: $metaString")

            // Parse StreamTitle='...';
            val titleMarker = "StreamTitle='"
            val startIndex = metaString.indexOf(titleMarker)
            if (startIndex != -1) {
                val actualStart = startIndex + titleMarker.length
                val endIndex = metaString.indexOf("';", actualStart)
                if (endIndex != -1) {
                    return@withContext metaString.substring(actualStart, endIndex).trim()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching ICY metadata: ${e.message}")
        } finally {
            try {
                inputStream?.close()
                connection?.disconnect()
            } catch (_: Exception) {}
        }
        return@withContext null
    }
}
