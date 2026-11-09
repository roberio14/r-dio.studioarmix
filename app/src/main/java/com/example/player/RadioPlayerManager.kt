package com.example.player

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.R
import com.example.metadata.ItunesRepository
import com.example.metadata.StreamMetadataFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object RadioPlayerManager {
    private const val TAG = "RadioPlayerManager"
    const val STREAM_URL = "https://straemg.radiostudioarmix.com.br/listen/radio_studioarmx/radio.mp3"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var sleepTimerJob: Job? = null
    private var metadataObserverJob: Job? = null
    private var lastRawStreamTitle: String = ""

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow("I Wanna Love You")
    val currentTrackTitle: StateFlow<String> = _currentTrackTitle.asStateFlow()

    private val _currentArtist = MutableStateFlow("Akon")
    val currentArtist: StateFlow<String> = _currentArtist.asStateFlow()

    private val _currentSubtitle = MutableStateFlow("Prog. Musical")
    val currentSubtitle: StateFlow<String> = _currentSubtitle.asStateFlow()

    // iTunes Artwork URL (High Resolution 600x600). Null during commercials or when unmatched.
    private val _currentArtworkUrl = MutableStateFlow<String?>(null)
    val currentArtworkUrl: StateFlow<String?> = _currentArtworkUrl.asStateFlow()

    // Indicator if currently in commercial break / station interval
    private val _isCommercialBreak = MutableStateFlow(false)
    val isCommercialBreak: StateFlow<Boolean> = _isCommercialBreak.asStateFlow()

    // Start likes and dislikes completely zeroed out
    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount.asStateFlow()

    private val _dislikesCount = MutableStateFlow(0)
    val dislikesCount: StateFlow<Int> = _dislikesCount.asStateFlow()

    private val _userVote = MutableStateFlow<String?>(null)
    val userVote: StateFlow<String?> = _userVote.asStateFlow()

    // Starts completely empty: fictitious/mock votes deleted as requested
    private val _votedSongsList = MutableStateFlow<List<SongVoteItem>>(emptyList())
    val votedSongsList: StateFlow<List<SongVoteItem>> = _votedSongsList.asStateFlow()

    // Sleep Timer remaining in seconds, null if inactive
    private val _sleepTimerSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerSeconds: StateFlow<Int?> = _sleepTimerSeconds.asStateFlow()

    // Notifications toggle
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // Registered listener music requests for WhatsApp (+5585992788437)
    const val WHATSAPP_PHONE = "5585992788437"
    private var appContext: Context? = null
    private val _registeredMusicRequests = MutableStateFlow<List<RegisteredMusicRequest>>(emptyList())
    val registeredMusicRequests: StateFlow<List<RegisteredMusicRequest>> = _registeredMusicRequests.asStateFlow()

    // Alert when a requested song is playing on air now
    private val _songAlertMessage = MutableStateFlow<String?>(null)
    val songAlertMessage: StateFlow<String?> = _songAlertMessage.asStateFlow()

    // Alert when a requested song just finished playing and was removed
    private val _justFinishedSongAlert = MutableStateFlow<String?>(null)
    val justFinishedSongAlert: StateFlow<String?> = _justFinishedSongAlert.asStateFlow()

    fun dismissSongAlerts() {
        _songAlertMessage.value = null
        _justFinishedSongAlert.value = null
    }

    // Monitor de status detalhado do player
    val playerStatus: StateFlow<PlayerStatus> = PlayerStatusMonitor.status

    init {
        scope.launch {
            PlayerStatusMonitor.status.collect { status ->
                _currentTrackTitle.value = status.title
                _currentArtist.value = status.artist
                _currentSubtitle.value = status.subtitle
                _currentArtworkUrl.value = status.artworkUrl
                _isCommercialBreak.value = status.isCommercialBreak

                if (!status.artworkUrl.isNullOrBlank()) {
                    WeeklyVotingManager.updateTrackArtwork(status.title, status.artist, status.artworkUrl)
                    _votedSongsList.value = _votedSongsList.value.map { song ->
                        if (song.isLive || (song.title.equals(status.title, ignoreCase = true) && song.artist.equals(status.artist, ignoreCase = true))) {
                            if (song.artworkUrl != status.artworkUrl) song.copy(artworkUrl = status.artworkUrl) else song
                        } else song
                    }
                }
            }
        }
        startMetadataPolling()
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun isCommercialOrBreak(rawTitle: String): Boolean {
        return PlayerStatusMonitor.isCommercialOrBreak(rawTitle)
    }

    fun voteLike(): Boolean {
        return voteCurrentTrack(isLike = true)
    }

    fun voteDislike(): Boolean {
        return voteCurrentTrack(isLike = false)
    }

    private fun voteCurrentTrack(isLike: Boolean): Boolean {
        // If in commercial break, voting on commercial is prevented or marked for the station
        val currentTitle = _currentTrackTitle.value
        val currentArtist = _currentArtist.value
        val currentArtwork = _currentArtworkUrl.value
        val currentSub = _currentSubtitle.value

        val currentList = _votedSongsList.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.title.equals(currentTitle, ignoreCase = true) &&
                    it.artist.equals(currentArtist, ignoreCase = true)
        }

        return if (existingIndex == -1) {
            val initialLikes = if (isLike) 1 else 0
            val initialDislikes = if (!isLike) 1 else 0
            val initialUserVote = if (isLike) "like" else "dislike"

            val newSong = SongVoteItem(
                id = "${currentTitle.hashCode()}_${System.currentTimeMillis()}",
                title = currentTitle,
                artist = currentArtist,
                album = currentSub.ifBlank { null },
                artworkUrl = currentArtwork,
                fallbackResId = R.drawable.radio_station_logo,
                likes = initialLikes,
                dislikes = initialDislikes,
                userVote = initialUserVote,
                isLive = true
            )
            _votedSongsList.value = listOf(newSong) + currentList
            _userVote.value = initialUserVote
            _likesCount.value = initialLikes
            _dislikesCount.value = initialDislikes
            WeeklyVotingManager.syncLiveVoteWithSchedule(
                title = currentTitle,
                artist = currentArtist,
                artworkUrl = currentArtwork,
                isLike = isLike,
                programInfo = RadioScheduleManager.currentProgram.value
            )
            true
        } else {
            val song = currentList[existingIndex]
            val voted = voteSong(song.id, isLike)
            if (voted) {
                WeeklyVotingManager.syncLiveVoteWithSchedule(
                    title = song.title,
                    artist = song.artist,
                    artworkUrl = song.artworkUrl ?: currentArtwork,
                    isLike = isLike,
                    programInfo = RadioScheduleManager.currentProgram.value
                )
            }
            voted
        }
    }

    fun voteSong(songId: String, isLike: Boolean): Boolean {
        var didVote = false
        val currentTitle = _currentTrackTitle.value
        val currentArt = _currentArtist.value

        val updated = _votedSongsList.value.map { song ->
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

                if (song.isLive || (song.title.equals(currentTitle, ignoreCase = true) && song.artist.equals(currentArt, ignoreCase = true))) {
                    _userVote.value = newVote
                    _likesCount.value = newLikes
                    _dislikesCount.value = newDislikes
                }

                song.copy(likes = newLikes, dislikes = newDislikes, userVote = newVote)
            } else song
        }
        _votedSongsList.value = updated
        return didVote
    }

    fun play(context: Context) {
        if (_playbackState.value is PlaybackState.Playing) return

        _playbackState.value = PlaybackState.Buffering
        startPlaybackService(context)
        startMetadataPolling()

        scope.launch(Dispatchers.IO) {
            try {
                releasePlayer()
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(STREAM_URL)
                    setOnPreparedListener { mp ->
                        Log.d(TAG, "MediaPlayer prepared, starting playback")
                        mp.start()
                        _playbackState.value = PlaybackState.Playing
                        refreshMetadataNow()
                    }
                    setOnBufferingUpdateListener { _, percent ->
                        Log.d(TAG, "Buffering: $percent%")
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        _playbackState.value = PlaybackState.Error("Falha na conexão do streaming (código $what). Toque para tentar novamente.")
                        true
                    }
                    setOnCompletionListener {
                        Log.d(TAG, "Stream completed/disconnected, attempting reconnect")
                        _playbackState.value = PlaybackState.Idle
                    }
                }
                mediaPlayer = player
                player.prepareAsync()
            } catch (e: Exception) {
                Log.e(TAG, "Exception initializing MediaPlayer", e)
                _playbackState.value = PlaybackState.Error("Erro ao conectar à rádio: ${e.localizedMessage ?: "Verifique sua internet"}")
            }
        }
    }

    fun pause(context: Context) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            _playbackState.value = PlaybackState.Paused
            stopPlaybackService(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing player", e)
            _playbackState.value = PlaybackState.Idle
        }
    }

    fun togglePlayPause(context: Context) {
        when (_playbackState.value) {
            is PlaybackState.Playing -> pause(context)
            is PlaybackState.Buffering -> pause(context)
            else -> play(context)
        }
    }

    fun stop(context: Context) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            releasePlayer()
            _playbackState.value = PlaybackState.Idle
            stopPlaybackService(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping player", e)
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing player", e)
        } finally {
            mediaPlayer = null
        }
    }

    // Monitor de status ativo do player: alterna dinamicamente entre iTunes e logo da rádio
    fun startMetadataPolling() {
        PlayerStatusMonitor.startMonitoring(STREAM_URL) { title, artist, album, artworkUrl ->
            onTrackChanged(title, artist, album, artworkUrl)
        }
    }

    fun refreshMetadataNow() {
        PlayerStatusMonitor.refreshNow(STREAM_URL) { title, artist, album, artworkUrl ->
            onTrackChanged(title, artist, album, artworkUrl)
        }
    }

    private fun onTrackChanged(title: String, artist: String, album: String?, artworkUrl: String?) {
        val currentList = _votedSongsList.value.map { it.copy(isLive = false) }.toMutableList()

        val existingIndex = currentList.indexOfFirst {
            it.title.equals(title, ignoreCase = true) && it.artist.equals(artist, ignoreCase = true)
        }

        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            val updated = existing.copy(
                isLive = true,
                album = album ?: existing.album,
                artworkUrl = artworkUrl ?: existing.artworkUrl
            )
            currentList[existingIndex] = updated
            _likesCount.value = updated.likes
            _dislikesCount.value = updated.dislikes
            _userVote.value = updated.userVote
        } else {
            _likesCount.value = 0
            _dislikesCount.value = 0
            _userVote.value = null
        }

        _votedSongsList.value = currentList

        // Check if any requested song is playing or just finished playing
        checkRequestedSongsPlaying(title, artist)
    }

    private fun checkRequestedSongsPlaying(currentTitle: String, currentArtist: String) {
        val currentRequests = _registeredMusicRequests.value
        if (currentRequests.isEmpty()) return

        val cleanCurrentTitle = currentTitle.lowercase().trim()
        val cleanCurrentArtist = currentArtist.lowercase().trim()

        val updatedList = mutableListOf<RegisteredMusicRequest>()
        var foundAnyPlaying = false

        for (req in currentRequests) {
            val reqTitle = req.title.lowercase().trim()
            val reqArtist = req.artist.lowercase().trim()

            val isMatch = (reqTitle.length >= 3 && cleanCurrentTitle.contains(reqTitle)) ||
                    (cleanCurrentTitle.contains(reqTitle) && cleanCurrentArtist.contains(reqArtist))

            if (isMatch) {
                // Requested song is currently playing on air!
                foundAnyPlaying = true
                updatedList.add(req.copy(isPlayingNow = true))
                _songAlertMessage.value = "🔔 SEU PEDIDO ESTÁ NO AR AGORA!\nA Rádio Studio Armix está tocando: ${req.title} - ${req.artist}!"
            } else if (req.isPlayingNow) {
                // Song finished playing! Remove it to free space for new requests
                Log.d(TAG, "Requested song finished playing, removing to give space for new requests: ${req.title}")
                _justFinishedSongAlert.value = "✅ Seu pedido \"${req.title}\" acabou de tocar na Rádio Studio Armix! O campo foi liberado para novos pedidos."
                // Omitted from updatedList so it is removed!
            } else {
                updatedList.add(req)
            }
        }

        if (!foundAnyPlaying && _songAlertMessage.value != null) {
            _songAlertMessage.value = null
        }

        if (updatedList != currentRequests) {
            _registeredMusicRequests.value = updatedList
            saveRegisteredRequestsToDisk(appContext, updatedList)
        }
    }

    // Sleep timer management
    fun startSleepTimer(context: Context, minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60
        _sleepTimerSeconds.value = totalSeconds

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1
                _sleepTimerSeconds.value = remaining
            }
            _sleepTimerSeconds.value = null
            pause(context)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerSeconds.value = null
    }

    private fun startPlaybackService(context: Context) {
        try {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_PLAY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start Foreground Service", e)
        }
    }

    private fun stopPlaybackService(context: Context) {
        try {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not stop Foreground Service", e)
        }
    }

    fun registerMusicRequest(request: RegisteredMusicRequest, context: Context? = null) {
        val ctx = context ?: appContext
        val currentList = _registeredMusicRequests.value.toMutableList()
        currentList.removeAll {
            it.title.equals(request.title, ignoreCase = true) &&
                    it.artist.equals(request.artist, ignoreCase = true)
        }
        currentList.add(0, request)
        _registeredMusicRequests.value = currentList
        ctx?.let { saveRegisteredRequestsToDisk(it, currentList) }
    }

    fun initRegisteredRequests(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("studio_armix_requests", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("registered_requests", null) ?: return
            val jsonArray = org.json.JSONArray(jsonString)
            val list = mutableListOf<RegisteredMusicRequest>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RegisteredMusicRequest(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        artist = obj.optString("artist", ""),
                        artworkUrl = obj.optString("artworkUrl", "").ifEmpty { null },
                        source = obj.optString("source", "iTunes"),
                        programTitle = obj.optString("programTitle", "").ifEmpty { null },
                        presenterName = obj.optString("presenterName", "").ifEmpty { null },
                        presenterPhotoUrl = obj.optString("presenterPhotoUrl", "").ifEmpty { null },
                        isPlayingNow = obj.optBoolean("isPlayingNow", false),
                        requestedAt = obj.optLong("requestedAt", System.currentTimeMillis())
                    )
                )
            }
            if (list.isNotEmpty()) {
                _registeredMusicRequests.value = list
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading registered music requests", e)
        }
    }

    private fun saveRegisteredRequestsToDisk(context: Context?, list: List<RegisteredMusicRequest>) {
        val ctx = context ?: appContext ?: return
        try {
            val jsonArray = org.json.JSONArray()
            for (req in list.take(50)) {
                val obj = org.json.JSONObject()
                obj.put("id", req.id)
                obj.put("title", req.title)
                obj.put("artist", req.artist)
                obj.put("artworkUrl", req.artworkUrl ?: "")
                obj.put("source", req.source)
                obj.put("programTitle", req.programTitle ?: "")
                obj.put("presenterName", req.presenterName ?: "")
                obj.put("presenterPhotoUrl", req.presenterPhotoUrl ?: "")
                obj.put("isPlayingNow", req.isPlayingNow)
                obj.put("requestedAt", req.requestedAt)
                jsonArray.put(obj)
            }
            val prefs = ctx.getSharedPreferences("studio_armix_requests", Context.MODE_PRIVATE)
            prefs.edit().putString("registered_requests", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving registered requests", e)
        }
    }

    fun sendRequestToWhatsApp(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String? = null,
        source: String = "iTunes"
    ) {
        // Query live on-air program & presenter with photo from radiostudioarmix.com.br
        val currentProg = RadioScheduleManager.currentProgram.value

        val request = RegisteredMusicRequest(
            title = title,
            artist = artist,
            artworkUrl = artworkUrl,
            source = source,
            programTitle = currentProg.title,
            presenterName = currentProg.presenter,
            presenterPhotoUrl = currentProg.photoUrl
        )

        registerMusicRequest(request, context)

        // Detailed message format including program name and locutor
        val message = "Olá Rádio Studio Armix!\n" +
                "Gostaria de pedir uma música:\n" +
                "🎵 Música: $title - $artist\n" +
                "📻 Programa: ${currentProg.title}\n" +
                "🎙️ Locutor: ${currentProg.presenter}"

        try {
            val uri = android.net.Uri.parse(
                "https://api.whatsapp.com/send?phone=$WHATSAPP_PHONE&text=" +
                        java.net.URLEncoder.encode(message, "UTF-8")
            )
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val webUri = android.net.Uri.parse(
                    "https://wa.me/$WHATSAPP_PHONE?text=" +
                            java.net.URLEncoder.encode(message, "UTF-8")
                )
                val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.w(TAG, "Error opening WhatsApp", e2)
            }
        }
    }
}

data class RegisteredMusicRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val artist: String,
    val artworkUrl: String? = null,
    val source: String = "iTunes",
    val programTitle: String? = null,
    val presenterName: String? = null,
    val presenterPhotoUrl: String? = null,
    val isPlayingNow: Boolean = false,
    val requestedAt: Long = System.currentTimeMillis()
)


