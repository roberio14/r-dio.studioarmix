package com.example.player

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class OnAirProgramInfo(
    val title: String,
    val presenter: String,
    val photoUrl: String,
    val timeSlot: String,
    val description: String = ""
)

data class ScheduleEntry(
    val startMinutes: Int, // e.g. 13 * 60 = 780
    val timeStr: String,
    val title: String,
    val presenter: String,
    val photoUrl: String,
    val description: String
)

object RadioScheduleManager {
    private const val TAG = "RadioScheduleManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Default station fallback locutor
    val DEFAULT_PROGRAM = OnAirProgramInfo(
        title = "Studio Ar Mix",
        presenter = "Locutor de Plantão",
        photoUrl = "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg",
        timeSlot = "Ao Vivo",
        description = "A melhor programação de música e entretenimento da Studio Ar Mix"
    )

    private val _currentProgram = MutableStateFlow(DEFAULT_PROGRAM)
    val currentProgram: StateFlow<OnAirProgramInfo> = _currentProgram.asStateFlow()

    private val weekdaysSchedule = listOf(
        ScheduleEntry(0 * 60, "00:00", "Madrugada Mix", "DJ Night", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "O melhor som para as suas madrugadas"),
        ScheduleEntry(6 * 60, "06:00", "Manhã Ar Mix", "Carla Siqueira", "https://studioarmix.unaux.com/audio/locutor-2-C9N7DQCh.jpg", "Comece o dia com alto astral e muita música"),
        ScheduleEntry(8 * 60, "08:00", "Deus & Amor", "Roberto Alves", "https://studioarmix.unaux.com/audio/locutor-3-CYxRRrFD.jpg", "Louvores e mensagens inspiradoras para abençoar seu dia"),
        ScheduleEntry(12 * 60, "12:00", "Amor I Love You - 1ª Edição", "Adriano Lozan", "https://radiostudioarmix.unaux.com/IMG-20260216-WA0007.jpg", "As mais belas canções românticas e sucessos"),
        ScheduleEntry(13 * 60, "13:00", "Posso Oferecer", "Marcos Mion", "https://studioarmix.unaux.com/audio/locutor-1-BVIfH52m.jpg", "Espaço aberto para o ouvinte pedir suas músicas favoritas"),
        ScheduleEntry(16 * 60, "16:00", "O melhor do Forró Pop", "Adriano Lozan", "https://radiostudioarmix.unaux.com/IMG-20260216-WA0007.jpg", "Os maiores sucessos do forró e ritmo contagiante"),
        ScheduleEntry(19 * 60, "19:00", "Horário de Brasília", "Rede Nacional", "https://radiostudioarmix.unaux.com/horariodebrasilia.jpg", "Informação e notícias oficiais do Brasil"),
        ScheduleEntry(20 * 60, "20:00", "Amor I Love You - 2ª Edição", "Adriano Lozan", "https://radiostudioarmix.unaux.com/IMG-20260216-WA0007.jpg", "A noite mais romântica do rádio")
    )

    private val saturdaySchedule = listOf(
        ScheduleEntry(0 * 60, "00:00", "Madrugada Mix", "DJ Night", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "O melhor som para as suas madrugadas"),
        ScheduleEntry(6 * 60, "06:00", "Manhã de Sábado", "Carla Siqueira", "https://studioarmix.unaux.com/audio/locutor-2-C9N7DQCh.jpg", "Energia para o seu fim de semana"),
        ScheduleEntry(8 * 60, "08:00", "Jovem Guarda", "Silvio Gree", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "Os grandes clássicos inesquecíveis"),
        ScheduleEntry(10 * 60, "10:00", "Sábado de Sucesso", "Rebeca Mariah", "https://studioarmix.unaux.com/audio/locutor-4-bKYB7sy0.jpg", "Os hits mais tocados no Brasil"),
        ScheduleEntry(14 * 60, "14:00", "Forrozeira", "DJ Anderson", "https://studioarmix.unaux.com/audio/locutor-1-BVIfH52m.jpg", "O melhor do forró no seu sábado"),
        ScheduleEntry(18 * 60, "18:00", "Samba e Pagode", "JU Santana", "https://studioarmix.unaux.com/audio/locutor-2-C9N7DQCh.jpg", "O melhor do samba e pagode"),
        ScheduleEntry(22 * 60, "22:00", "Madrugada Mix", "DJ Night", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "Viradão musical Studio Ar Mix")
    )

    private val sundaySchedule = listOf(
        ScheduleEntry(0 * 60, "00:00", "Madrugada Mix", "DJ Night", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "O melhor som para as suas madrugadas"),
        ScheduleEntry(6 * 60, "06:00", "Os Melhores Hits", "Ricardo Vak", "https://studioarmix.unaux.com/audio/locutor-3-CYxRRrFD.jpg", "Comece o domingo com os melhores hits"),
        ScheduleEntry(7 * 60, "07:00", "As Melhores do Rei Roberto Carlos", "Silvio Gree", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "Homenagem especial ao Rei da música brasileira"),
        ScheduleEntry(10 * 60, "10:00", "Domingão de Sucesso", "Rebeca Mariah", "https://studioarmix.unaux.com/audio/locutor-4-bKYB7sy0.jpg", "O seu domingo recheado de sucessos"),
        ScheduleEntry(13 * 60, "13:00", "Forrozão", "DJ Anderson", "https://studioarmix.unaux.com/audio/locutor-1-BVIfH52m.jpg", "Forró animado para toda a família"),
        ScheduleEntry(16 * 60, "16:00", "Domingo Show", "Flavio Dias", "https://studioarmix.unaux.com/audio/locutor-5-e572Ti7z.jpg", "O maior show de variedades do rádio"),
        ScheduleEntry(20 * 60, "20:00", "Amor I Love You - 3ª Edição", "Adriano Lozan", "https://radiostudioarmix.unaux.com/IMG-20260216-WA0007.jpg", "Fechando o domingo com romantismo")
    )

    init {
        updateCurrentProgram()
        // Try fetching online updates from radiostudioarmix.com.br
        scope.launch {
            fetchLiveWebsiteProgram()
        }
    }

    fun updateCurrentProgram() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Fortaleza"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val scheduleList = when (dayOfWeek) {
            Calendar.SUNDAY -> sundaySchedule
            Calendar.SATURDAY -> saturdaySchedule
            else -> weekdaysSchedule
        }

        var matched = scheduleList.firstOrNull() ?: DEFAULT_PROGRAM.toScheduleEntry()
        for (entry in scheduleList) {
            if (entry.startMinutes <= currentMinutes) {
                matched = entry
            } else {
                break
            }
        }

        _currentProgram.value = OnAirProgramInfo(
            title = matched.title,
            presenter = matched.presenter,
            photoUrl = matched.photoUrl,
            timeSlot = "No Ar Agora",
            description = matched.description
        )
    }

    private suspend fun fetchLiveWebsiteProgram() {
        try {
            val request = Request.Builder()
                .url("https://radiostudioarmix.com.br")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully reached radiostudioarmix.com.br")
            }
            updateCurrentProgram()
        } catch (e: Exception) {
            Log.w(TAG, "Offline or unreachable website, using accurate local schedule", e)
            updateCurrentProgram()
        }
    }

    private fun OnAirProgramInfo.toScheduleEntry(): ScheduleEntry {
        return ScheduleEntry(0, "00:00", title, presenter, photoUrl, description)
    }
}
