package com.example.player

import android.content.Context
import android.util.Log
import com.example.metadata.ItunesRepository
import com.example.metadata.MusicSearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Representa uma música no ranking semanal de votação por programa, horário, dia e data.
 */
data class ProgramSongVote(
    val id: String,
    val rank: Int = 1,
    val title: String,
    val artist: String,
    val album: String? = null,
    val artworkUrl: String? = null,
    val programTitle: String,
    val presenter: String,
    val timeSlot: String,
    val dayOfWeek: String, // "Sexta-feira", "Sábado", etc.
    val dayNumber: Int,    // 1 = Dom, 2 = Seg, ..., 6 = Sex (6º Dia da Semana), 7 = Sáb
    val dateFormatted: String, // "06/11/2026"
    val likes: Int,
    val dislikes: Int,
    val userVote: String? = null, // "like", "dislike", null
    val isSixthDayHighlight: Boolean = false
) {
    val totalVotes: Int get() = likes + dislikes
    val approvalPercentage: Int
        get() = if (totalVotes > 0) ((likes.toFloat() / totalVotes.toFloat()) * 100).toInt() else 100
    val score: Int get() = likes - dislikes
}

/**
 * Gerenciador inteligente dos resultados de votação das músicas mais votadas.
 * Inicia totalmente zerado para receber os primeiros votos válidos dos ouvintes cadastrados.
 */
object WeeklyVotingManager {
    private const val TAG = "WeeklyVotingManager"
    private const val PREFS_NAME = "studio_armix_weekly_votes_prefs"
    private const val KEY_WEEKLY_VOTES = "weekly_votes_json"
    private const val KEY_DEMO_PURGED = "demo_votes_zeroed_v4"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var appContext: Context? = null

    // Cache para evitar requisições repetidas da mesma capa
    private val artworkSearchAttempted = mutableSetOf<String>()

    // Votações iniciam 100% ZERADAS para receber o primeiro voto válido dos ouvintes cadastrados
    private val _weeklyVotes = MutableStateFlow<List<ProgramSongVote>>(emptyList())
    val weeklyVotes: StateFlow<List<ProgramSongVote>> = _weeklyVotes.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Limpa definitivamente as votações de demonstração anteriores
        if (!prefs.getBoolean(KEY_DEMO_PURGED, false)) {
            prefs.edit()
                .putString(KEY_WEEKLY_VOTES, "[]")
                .putBoolean(KEY_DEMO_PURGED, true)
                .apply()
            _weeklyVotes.value = emptyList()
            Log.d(TAG, "Votações de demonstração zeradas com sucesso. Pronto para os primeiros votos válidos!")
        } else {
            loadFromDisk()
        }
    }

    /**
     * Zera manualmente todas as votações a qualquer momento
     */
    fun clearAllVotes() {
        _weeklyVotes.value = emptyList()
        saveToDisk()
        Log.d(TAG, "Todas as votações foram zeradas manualmente.")
    }

    /**
     * Atualiza a capa de uma música e artista específicos em todo o ranking
     */
    fun updateTrackArtwork(title: String, artist: String, artworkUrl: String?) {
        if (artworkUrl.isNullOrBlank()) return

        val cleanTitle = normalizeString(title)
        val cleanArtist = normalizeString(artist)

        var changed = false
        val updated = _weeklyVotes.value.map { song ->
            val songTitleNorm = normalizeString(song.title)
            val songArtistNorm = normalizeString(song.artist)

            val matches = (songTitleNorm.contains(cleanTitle) || cleanTitle.contains(songTitleNorm)) ||
                    (songArtistNorm.contains(cleanArtist) || cleanArtist.contains(songArtistNorm))

            if (matches && (song.artworkUrl.isNullOrBlank() || song.artworkUrl != artworkUrl)) {
                changed = true
                Log.d(TAG, "Capa atualizada para: ${song.title} - ${song.artist} -> $artworkUrl")
                song.copy(artworkUrl = artworkUrl)
            } else song
        }

        if (changed) {
            _weeklyVotes.value = updated
            saveToDisk()
        }
    }

    /**
     * Garante que uma música tenha sua capa do artista carregada via iTunes/Deezer
     */
    fun ensureArtworkLoaded(songId: String, title: String, artist: String) {
        val cacheKey = "${normalizeString(artist)}_${normalizeString(title)}"
        if (artworkSearchAttempted.contains(cacheKey)) return
        artworkSearchAttempted.add(cacheKey)

        scope.launch {
            try {
                val query = "$artist $title"
                val itunesResult = ItunesRepository.searchTrack(query) ?: ItunesRepository.searchTrack(title)

                var foundArtwork: String? = itunesResult?.artworkUrl

                if (foundArtwork.isNullOrBlank()) {
                    val searchList = MusicSearchRepository.searchSongs("$artist $title")
                    val match = searchList.firstOrNull { !it.artworkUrl.isNullOrBlank() }
                    foundArtwork = match?.artworkUrl
                }

                if (!foundArtwork.isNullOrBlank()) {
                    Log.d(TAG, "Capa resolvida para $title ($artist): $foundArtwork")
                    val updated = _weeklyVotes.value.map { song ->
                        if (song.id == songId || (normalizeString(song.title) == normalizeString(title) && normalizeString(song.artist) == normalizeString(artist))) {
                            song.copy(artworkUrl = foundArtwork)
                        } else song
                    }
                    _weeklyVotes.value = updated
                    saveToDisk()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao resolver capa para $title - $artist", e)
            }
        }
    }

    /**
     * Retorna a lista de músicas do 6º dia da semana (Sexta-feira), ordenadas pelo total de votos
     */
    fun getSixthDayResults(): List<ProgramSongVote> {
        return _weeklyVotes.value
            .filter { it.dayNumber == 6 }
            .sortedByDescending { it.score }
            .mapIndexed { index, song -> song.copy(rank = index + 1) }
    }

    /**
     * Retorna a lista de músicas de um dia específico (1 a 7)
     */
    fun getResultsForDay(dayNumber: Int): List<ProgramSongVote> {
        return _weeklyVotes.value
            .filter { it.dayNumber == dayNumber }
            .sortedByDescending { it.score }
            .mapIndexed { index, song -> song.copy(rank = index + 1) }
    }

    /**
     * Retorna todas as músicas mais votadas da semana em ordem decrescente de pontuação
     */
    fun getAllWeekRanking(): List<ProgramSongVote> {
        return _weeklyVotes.value
            .sortedByDescending { it.score }
            .mapIndexed { index, song -> song.copy(rank = index + 1) }
    }

    /**
     * Registra o voto de um ouvinte autenticado em uma música do ranking semanal
     */
    fun voteOnProgramSong(songId: String, isLike: Boolean): Boolean {
        var didVote = false
        val updated = _weeklyVotes.value.map { song ->
            if (song.id == songId) {
                var newLikes = song.likes
                var newDislikes = song.dislikes
                var newVote: String?

                if (isLike) {
                    if (song.userVote == "like") {
                        newVote = null
                        newLikes = (newLikes - 1).coerceAtLeast(0)
                        didVote = false
                    } else {
                        if (song.userVote == "dislike") {
                            newDislikes = (newDislikes - 1).coerceAtLeast(0)
                        }
                        newLikes += 1
                        newVote = "like"
                        didVote = true
                    }
                } else {
                    if (song.userVote == "dislike") {
                        newVote = null
                        newDislikes = (newDislikes - 1).coerceAtLeast(0)
                        didVote = false
                    } else {
                        if (song.userVote == "like") {
                            newLikes = (newLikes - 1).coerceAtLeast(0)
                        }
                        newDislikes += 1
                        newVote = "dislike"
                        didVote = true
                    }
                }
                song.copy(likes = newLikes, dislikes = newDislikes, userVote = newVote)
            } else song
        }
        _weeklyVotes.value = updated
        saveToDisk()
        return didVote
    }

    /**
     * Registra o primeiro ou novo voto válido a partir da música ao vivo tocando no player
     */
    fun syncLiveVoteWithSchedule(
        title: String,
        artist: String,
        artworkUrl: String? = null,
        isLike: Boolean,
        programInfo: OnAirProgramInfo
    ) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Fortaleza"))
        val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Dom, 6 = Sex
        val dayName = when (dayOfWeekInt) {
            Calendar.SUNDAY -> "Domingo"
            Calendar.MONDAY -> "Segunda-feira"
            Calendar.TUESDAY -> "Terça-feira"
            Calendar.WEDNESDAY -> "Quarta-feira"
            Calendar.THURSDAY -> "Quinta-feira"
            Calendar.FRIDAY -> "Sexta-feira (6º Dia)"
            Calendar.SATURDAY -> "Sábado"
            else -> "Hoje"
        }
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val currentList = _weeklyVotes.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            normalizeString(it.title) == normalizeString(title) &&
                    normalizeString(it.artist) == normalizeString(artist) &&
                    it.dayNumber == dayOfWeekInt
        }

        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            val newLikes = if (isLike) existing.likes + 1 else existing.likes
            val newDislikes = if (!isLike) existing.dislikes + 1 else existing.dislikes
            val finalArtwork = if (!artworkUrl.isNullOrBlank()) artworkUrl else existing.artworkUrl
            currentList[existingIndex] = existing.copy(
                artworkUrl = finalArtwork,
                likes = newLikes,
                dislikes = newDislikes,
                userVote = if (isLike) "like" else "dislike"
            )
        } else {
            // Cria o primeiro voto válido no ranking
            val newEntry = ProgramSongVote(
                id = "live_${title.hashCode()}_${System.currentTimeMillis()}",
                rank = currentList.size + 1,
                title = title,
                artist = artist,
                album = "Studio Ar Mix Ao Vivo",
                artworkUrl = artworkUrl,
                programTitle = programInfo.title,
                presenter = programInfo.presenter,
                timeSlot = "$timeFormatted - ${programInfo.timeSlot}",
                dayOfWeek = dayName,
                dayNumber = dayOfWeekInt,
                dateFormatted = dateFormatted,
                likes = if (isLike) 1 else 0,
                dislikes = if (!isLike) 1 else 0,
                userVote = if (isLike) "like" else "dislike",
                isSixthDayHighlight = dayOfWeekInt == 6
            )
            currentList.add(0, newEntry)

            if (artworkUrl.isNullOrBlank()) {
                ensureArtworkLoaded(newEntry.id, title, artist)
            }
        }

        _weeklyVotes.value = currentList
        saveToDisk()
        Log.d(TAG, "Primeiro/Novo voto válido computado: $title ($artist) artwork=$artworkUrl durante ${programInfo.title}")
    }

    private fun normalizeString(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }

    private fun saveToDisk() {
        val ctx = appContext ?: return
        try {
            val jsonArray = JSONArray()
            for (item in _weeklyVotes.value.take(150)) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("artist", item.artist)
                    put("album", item.album ?: "")
                    put("artworkUrl", item.artworkUrl ?: "")
                    put("programTitle", item.programTitle)
                    put("presenter", item.presenter)
                    put("timeSlot", item.timeSlot)
                    put("dayOfWeek", item.dayOfWeek)
                    put("dayNumber", item.dayNumber)
                    put("dateFormatted", item.dateFormatted)
                    put("likes", item.likes)
                    put("dislikes", item.dislikes)
                    put("userVote", item.userVote ?: "")
                    put("isSixthDayHighlight", item.isSixthDayHighlight)
                }
                jsonArray.put(obj)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_WEEKLY_VOTES, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving weekly votes to disk", e)
        }
    }

    private fun loadFromDisk() {
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_WEEKLY_VOTES, null)
            if (!jsonStr.isNullOrEmpty() && jsonStr != "[]") {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<ProgramSongVote>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    val id = o.optString("id", "")
                    // Pula se for demonstração antiga
                    if (id.startsWith("w6_") || id.startsWith("w1_") || id.startsWith("w2_") || id.startsWith("w3_")) {
                        continue
                    }
                    val rawArtwork = o.optString("artworkUrl", "").ifEmpty { null }
                    list.add(
                        ProgramSongVote(
                            id = id.ifEmpty { "v_$i" },
                            title = o.optString("title", ""),
                            artist = o.optString("artist", ""),
                            album = o.optString("album", "").ifEmpty { null },
                            artworkUrl = rawArtwork,
                            programTitle = o.optString("programTitle", "Studio Ar Mix"),
                            presenter = o.optString("presenter", "Locutor de Plantão"),
                            timeSlot = o.optString("timeSlot", "Ao Vivo"),
                            dayOfWeek = o.optString("dayOfWeek", "Sexta-feira"),
                            dayNumber = o.optInt("dayNumber", 6),
                            dateFormatted = o.optString("dateFormatted", ""),
                            likes = o.optInt("likes", 0),
                            dislikes = o.optInt("dislikes", 0),
                            userVote = o.optString("userVote", "").ifEmpty { null },
                            isSixthDayHighlight = o.optBoolean("isSixthDayHighlight", false)
                        )
                    )
                }
                _weeklyVotes.value = list
            } else {
                _weeklyVotes.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading weekly votes from disk", e)
            _weeklyVotes.value = emptyList()
        }
    }
}
