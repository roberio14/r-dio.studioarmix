package com.example.player

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

/**
 * Tipo de conteúdo detectado no sinal da transmissão da rádio.
 */
enum class BroadcastContentType {
    MUSIC,
    COMMERCIAL_BREAK,
    STATION_JINGLE
}

/**
 * Estado detalhado do Monitor de Status do Player.
 */
data class PlayerStatus(
    val contentType: BroadcastContentType = BroadcastContentType.MUSIC,
    val isCommercialBreak: Boolean = false,
    val title: String = "I Wanna Love You",
    val artist: String = "Akon",
    val subtitle: String = "Prog. Musical",
    val artworkUrl: String? = null,
    val defaultCoverResId: Int = R.drawable.radio_station_logo,
    val statusLabel: String = "NO AR • MÚSICA AO VIVO",
    val isSearchingItunes: Boolean = false,
    val lastSignalDetected: String = "",
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Monitor de status para o player que alterna dinamicamente entre buscar metadados
 * no iTunes e exibir a logo da rádio quando o sistema detectar um sinal de intervalo
 * comercial, garantindo que o ouvinte sempre veja a capa correta.
 */
object PlayerStatusMonitor {
    private const val TAG = "PlayerStatusMonitor"

    private val scope = CoroutineScope(Dispatchers.Main)
    private var monitorJob: Job? = null

    private val _status = MutableStateFlow(PlayerStatus())
    val status: StateFlow<PlayerStatus> = _status.asStateFlow()

    private var lastRawStreamTitle: String = ""

    // Padrões completos para identificação de sinais de intervalo comercial e vinhetas
    private val commercialKeywords = listOf(
        "comercial",
        "intervalo",
        "intervalo comercial",
        "vinheta",
        "propaganda",
        "chamada",
        "patrocinio",
        "patrocínio",
        "anuncio",
        "anúncio",
        "break",
        "bloco comercial",
        "hora certa",
        "voz do brasil",
        "studioarmx",
        "studio armix",
        "radio studioarmx",
        "rádio studio armix",
        "guaiuba",
        "guaiúba",
        "spot",
        "jingle",
        "sintonize",
        "programacao",
        "programação"
    )

    fun startMonitoring(streamUrl: String, onTrackChangedCallback: ((title: String, artist: String, album: String?, artworkUrl: String?) -> Unit)? = null) {
        if (monitorJob?.isActive == true) return

        monitorJob = scope.launch(Dispatchers.IO) {
            // Executa a primeira verificação imediatamente
            evaluateStreamSignal(streamUrl, onTrackChangedCallback)

            while (isActive) {
                delay(10000L) // Checagem ativa a cada 10 segundos
                evaluateStreamSignal(streamUrl, onTrackChangedCallback)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    /**
     * Força uma reavaliação imediata do status (ex: quando o usuário dá Play)
     */
    fun refreshNow(streamUrl: String, onTrackChangedCallback: ((title: String, artist: String, album: String?, artworkUrl: String?) -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            evaluateStreamSignal(streamUrl, onTrackChangedCallback)
        }
    }

    /**
     * Avalia o sinal bruto da rádio e alterna dinamicamente entre iTunes e Logo oficial
     */
    suspend fun evaluateStreamSignal(
        streamUrl: String,
        onTrackChangedCallback: ((title: String, artist: String, album: String?, artworkUrl: String?) -> Unit)? = null
    ) {
        try {
            val rawTitle = StreamMetadataFetcher.fetchCurrentStreamTitle(streamUrl)
            if (!rawTitle.isNullOrBlank() && rawTitle != lastRawStreamTitle) {
                lastRawStreamTitle = rawTitle
                Log.d(TAG, "Sinal bruto detectado na transmissão: $rawTitle")

                if (isCommercialOrBreak(rawTitle)) {
                    // SINAL DE INTERVALO COMERCIAL DETECTADO
                    // Ação: Pausa busca no iTunes, limpa artworkUrl e ativa a logo oficial da rádio
                    Log.d(TAG, "Intervalo comercial identificado. Alternando para Logo Oficial da Rádio.")
                    _status.value = _status.value.copy(
                        contentType = BroadcastContentType.COMMERCIAL_BREAK,
                        isCommercialBreak = true,
                        title = "Intervalo Comercial",
                        artist = "Rádio Studio Armix",
                        subtitle = "Guaiúba-CE • No Ar",
                        artworkUrl = null, // Força a exibição imediata de R.drawable.radio_station_logo
                        defaultCoverResId = R.drawable.radio_station_logo,
                        statusLabel = "INTERVALO COMERCIAL",
                        isSearchingItunes = false,
                        lastSignalDetected = rawTitle,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )

                    onTrackChangedCallback?.invoke("Intervalo Comercial", "Rádio Studio Armix", "Guaiúba-CE", null)
                } else {
                    // SINAL DE MÚSICA DETECTADO
                    // Ação: Busca dinamicamente os metadados e capa em alta resolução no iTunes
                    Log.d(TAG, "Música em execução. Consultando metadados e capa no iTunes: $rawTitle")
                    _status.value = _status.value.copy(
                        isSearchingItunes = true,
                        statusLabel = "BUSCANDO CAPA NO ITUNES..."
                    )

                    val itunesResult = ItunesRepository.searchTrack(rawTitle)
                    if (itunesResult != null && !itunesResult.artworkUrl.isNullOrBlank()) {
                        Log.d(TAG, "Capa encontrada no iTunes: ${itunesResult.artworkUrl}")
                        _status.value = _status.value.copy(
                            contentType = BroadcastContentType.MUSIC,
                            isCommercialBreak = false,
                            title = itunesResult.title,
                            artist = itunesResult.artist,
                            subtitle = itunesResult.album ?: "Prog. Musical",
                            artworkUrl = itunesResult.artworkUrl,
                            defaultCoverResId = R.drawable.radio_station_logo,
                            statusLabel = "NO AR • MÚSICA AO VIVO",
                            isSearchingItunes = false,
                            lastSignalDetected = rawTitle,
                            lastCheckedTimestamp = System.currentTimeMillis()
                        )

                        onTrackChangedCallback?.invoke(
                            itunesResult.title,
                            itunesResult.artist,
                            itunesResult.album,
                            itunesResult.artworkUrl
                        )
                    } else {
                        // Música sem correspondência direta no iTunes: divide por traço
                        val parts = rawTitle.split(" - ", limit = 2)
                        val artistPart = if (parts.size == 2) parts[0].trim() else "Rádio Studio Armix"
                        val titlePart = if (parts.size == 2) parts[1].replace("_", " ").trim() else rawTitle.replace("_", " ").trim()

                        _status.value = _status.value.copy(
                            contentType = BroadcastContentType.MUSIC,
                            isCommercialBreak = false,
                            title = titlePart,
                            artist = artistPart,
                            subtitle = "Prog. Musical",
                            artworkUrl = null, // Logo padrão da rádio como capa alternativa
                            defaultCoverResId = R.drawable.radio_station_logo,
                            statusLabel = "NO AR • MÚSICA AO VIVO",
                            isSearchingItunes = false,
                            lastSignalDetected = rawTitle,
                            lastCheckedTimestamp = System.currentTimeMillis()
                        )

                        onTrackChangedCallback?.invoke(titlePart, artistPart, null, null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro na checagem do monitor de sinal: ${e.message}")
        }
    }

    /**
     * Identifica se a string de transmissão corresponde a um intervalo comercial ou vinheta
     */
    fun isCommercialOrBreak(rawTitle: String): Boolean {
        val lower = rawTitle.lowercase().trim()
        if (lower.isBlank()) return true

        for (pattern in commercialKeywords) {
            if (lower == pattern || lower.contains(pattern)) {
                return true
            }
        }

        // Sem separador de música e com menção a rádio ou estúdio
        if (!rawTitle.contains(" - ") && !rawTitle.contains(" – ")) {
            if (lower.contains("radio") || lower.contains("studio")) {
                return true
            }
        }

        return false
    }

    /**
     * Alterna manualmente o status para teste ou simulação de intervalo comercial
     */
    fun simulateCommercialBreak(active: Boolean) {
        if (active) {
            _status.value = _status.value.copy(
                contentType = BroadcastContentType.COMMERCIAL_BREAK,
                isCommercialBreak = true,
                title = "Intervalo Comercial",
                artist = "Rádio Studio Armix",
                subtitle = "Guaiúba-CE • No Ar",
                artworkUrl = null,
                statusLabel = "INTERVALO COMERCIAL"
            )
        } else {
            _status.value = _status.value.copy(
                contentType = BroadcastContentType.MUSIC,
                isCommercialBreak = false,
                title = "I Wanna Love You",
                artist = "Akon",
                subtitle = "Prog. Musical",
                statusLabel = "NO AR • MÚSICA AO VIVO"
            )
        }
    }
}
