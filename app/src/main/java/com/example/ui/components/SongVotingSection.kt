package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.auth.UserManager
import com.example.player.ProgramSongVote
import com.example.player.RadioPlayerManager
import com.example.player.SongVoteItem
import com.example.player.WeeklyVotingManager
import com.example.ui.theme.ArmixYellow

private enum class VotingViewTab {
    LIVE,
    SIXTH_DAY,
    SCHEDULE
}

@Composable
fun SongVotingSection(
    onNavigateToRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val liveVotedSongs by RadioPlayerManager.votedSongsList.collectAsState()
    val weeklyVotes by WeeklyVotingManager.weeklyVotes.collectAsState()
    val isLoggedIn by UserManager.isLoggedIn.collectAsState()
    val currentUser by UserManager.currentUser.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var currentTab by rememberSaveable { mutableStateOf(VotingViewTab.SIXTH_DAY) }
    var selectedDayNumber by rememberSaveable { mutableIntStateOf(6) } // Default 6 = Sexta-feira (6º dia)

    val sixthDaySongs = remember(weeklyVotes) {
        weeklyVotes.filter { it.dayNumber == 6 }.sortedByDescending { it.score }
    }

    val selectedDaySongs = remember(weeklyVotes, selectedDayNumber) {
        weeklyVotes.filter { it.dayNumber == selectedDayNumber }.sortedByDescending { it.score }
    }

    val totalLikes = remember(weeklyVotes, liveVotedSongs, currentTab) {
        when (currentTab) {
            VotingViewTab.LIVE -> liveVotedSongs.sumOf { it.likes }
            VotingViewTab.SIXTH_DAY -> sixthDaySongs.sumOf { it.likes }
            VotingViewTab.SCHEDULE -> selectedDaySongs.sumOf { it.likes }
        }
    }

    val totalDislikes = remember(weeklyVotes, liveVotedSongs, currentTab) {
        when (currentTab) {
            VotingViewTab.LIVE -> liveVotedSongs.sumOf { it.dislikes }
            VotingViewTab.SIXTH_DAY -> sixthDaySongs.sumOf { it.dislikes }
            VotingViewTab.SCHEDULE -> selectedDaySongs.sumOf { it.dislikes }
        }
    }

    fun executeWithAuth(action: () -> Unit) {
        if (!isLoggedIn) {
            pendingAction = action
            showAuthDialog = true
        } else {
            action()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .shadow(12.dp, RoundedCornerShape(14.dp))
            .testTag("song_voting_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E3A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title and Overall Likes/Dislikes Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ArmixYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VOTAÇÃO DOS OUVINTES",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            if (!isLoggedIn) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF382300))
                                        .border(0.5.dp, ArmixYellow, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = ArmixYellow,
                                            modifier = Modifier.size(8.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "LOGIN",
                                            color = ArmixYellow,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (isLoggedIn) "Conectado como ${currentUser?.firstName ?: "Ouvinte"}" else "Cadastre-se ou entre para registrar seu voto",
                            color = if (isLoggedIn) Color(0xFF81C784) else Color(0xFFAAAAAA),
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Global count pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Likes pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF263326))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$totalLikes",
                            color = Color(0xFF81C784),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Dislikes pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF381F21))
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$totalDislikes",
                            color = Color(0xFFEF5350),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs Selector: [ 6º DIA (SEXTA) 🏆 ] [ PROGRAMAÇÃO ] [ AO VIVO ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0E0E12))
                    .border(1.dp, Color(0xFF262633), RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: 6º Dia (Highlight)
                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (currentTab == VotingViewTab.SIXTH_DAY) ArmixYellow
                            else Color.Transparent
                        )
                        .clickable { currentTab = VotingViewTab.SIXTH_DAY }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (currentTab == VotingViewTab.SIXTH_DAY) Color(0xFF141419) else Color(0xFFFFD54F),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "6º DIA (SEXTA) 🏆",
                            color = if (currentTab == VotingViewTab.SIXTH_DAY) Color(0xFF141419) else Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Tab 2: Programação Semanal
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (currentTab == VotingViewTab.SCHEDULE) ArmixYellow
                            else Color.Transparent
                        )
                        .clickable { currentTab = VotingViewTab.SCHEDULE }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (currentTab == VotingViewTab.SCHEDULE) Color(0xFF141419) else Color(0xFFB0B0C0),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PROGRAMAÇÃO",
                            color = if (currentTab == VotingViewTab.SCHEDULE) Color(0xFF141419) else Color(0xFFCCCCCC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tab 3: Ao Vivo
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (currentTab == VotingViewTab.LIVE) ArmixYellow
                            else Color.Transparent
                        )
                        .clickable { currentTab = VotingViewTab.LIVE }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (currentTab == VotingViewTab.LIVE) Color(0xFFD32F2F) else Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AO VIVO",
                            color = if (currentTab == VotingViewTab.LIVE) Color(0xFF141419) else Color(0xFFCCCCCC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Area based on Selected Tab
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "voting_tab_content"
            ) { tab ->
                when (tab) {
                    VotingViewTab.SIXTH_DAY -> {
                        SixthDayResultsView(
                            songs = sixthDaySongs,
                            onGoToLive = { currentTab = VotingViewTab.LIVE },
                            onVote = { songId, isLike ->
                                executeWithAuth {
                                    val voted = WeeklyVotingManager.voteOnProgramSong(songId, isLike)
                                    val action = if (isLike) "Toca +" else "Toca -"
                                    val userName = currentUser?.firstName ?: "Ouvinte"
                                    val msg = if (voted) "Voto $action registrado por $userName!" else "Voto removido"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    VotingViewTab.SCHEDULE -> {
                        ScheduleWeeklyResultsView(
                            selectedDayNumber = selectedDayNumber,
                            onSelectDay = { selectedDayNumber = it },
                            songs = selectedDaySongs,
                            onGoToLive = { currentTab = VotingViewTab.LIVE },
                            onVote = { songId, isLike ->
                                executeWithAuth {
                                    val voted = WeeklyVotingManager.voteOnProgramSong(songId, isLike)
                                    val action = if (isLike) "Toca +" else "Toca -"
                                    val userName = currentUser?.firstName ?: "Ouvinte"
                                    val msg = if (voted) "Voto $action registrado por $userName!" else "Voto removido"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    VotingViewTab.LIVE -> {
                        LiveVotingView(
                            liveSongs = liveVotedSongs,
                            onVoteSong = { songId, isLike ->
                                executeWithAuth {
                                    val voted = RadioPlayerManager.voteSong(songId, isLike)
                                    val action = if (isLike) "Toca +" else "Toca -"
                                    val userName = currentUser?.firstName ?: "Ouvinte"
                                    val msg = if (voted) "Voto $action registrado por $userName!" else "Voto removido"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onVoteLiveCurrent = { isLike ->
                                executeWithAuth {
                                    val voted = if (isLike) RadioPlayerManager.voteLike() else RadioPlayerManager.voteDislike()
                                    val action = if (isLike) "Toca +" else "Toca -"
                                    val userName = currentUser?.firstName ?: "Ouvinte"
                                    val msg = if (voted) "Primeiro voto válido ($action) registrado por $userName!" else "Voto computado!"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal de Login / Cadastro Obrigatório
    if (showAuthDialog) {
        LoginOrRegisterDialog(
            reason = AuthRequiredReason.VOTE,
            onDismiss = {
                showAuthDialog = false
                pendingAction = null
            },
            onLoginSuccess = {
                showAuthDialog = false
                pendingAction?.invoke()
                pendingAction = null
            },
            onNavigateToRegistration = {
                showAuthDialog = false
                pendingAction = null
                onNavigateToRegistration()
            }
        )
    }
}

/**
 * Visualização do 6º Dia da Semana (Sexta-feira) - Parada Musical Oficial
 */
@Composable
private fun SixthDayResultsView(
    songs: List<ProgramSongVote>,
    onGoToLive: () -> Unit,
    onVote: (String, Boolean) -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Golden Badge Header for 6th Day
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF261D04))
                .border(1.dp, ArmixYellow.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(ArmixYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "6º",
                        color = Color(0xFF141419),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RESULTADO OFICIAL: 6º DIA DA SEMANA",
                        color = ArmixYellow,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Sexta-feira • Fechamento da Parada Musical dos Ouvintes",
                        color = Color(0xFFE0E0E0),
                        fontSize = 9.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArmixYellow.copy(alpha = 0.2f))
                        .border(1.dp, ArmixYellow, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SEXTA",
                        color = ArmixYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Songs list container with visible scrollbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F0F14))
                .border(1.dp, Color(0xFF282834), RoundedCornerShape(8.dp))
        ) {
            if (songs.isEmpty()) {
                // Estado Zerado - Demonstração limpa
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF261D04))
                            .border(1.dp, ArmixYellow.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ArmixYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PARADA DO 6º DIA ZERADA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Votações de demonstração zeradas. Cadastre-se e entre com seu login para votar e inaugurar o primeiro voto válido da parada!",
                        color = Color(0xFF9999AA),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onGoToLive,
                        colors = ButtonDefaults.buttonColors(containerColor = ArmixYellow),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = Color(0xFF141419),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REGISTRAR PRIMEIRO VOTO (AO VIVO)",
                            color = Color(0xFF141419),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        ProgramSongVoteRow(
                            song = song,
                            isSixthDay = true,
                            onVote = { isLike -> onVote(song.id, isLike) }
                        )
                    }
                }

                // Vertical Scrollbar
                val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
                val totalItemsCount by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }
                if (totalItemsCount > 2) {
                    val scrollFraction = firstVisibleItemIndex.toFloat() / (totalItemsCount - 2).coerceAtLeast(1)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 2.dp, top = 8.dp, bottom = 8.dp)
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.35f)
                                .align(
                                    androidx.compose.ui.BiasAlignment(
                                        horizontalBias = 0f,
                                        verticalBias = (scrollFraction * 2f - 1f).coerceIn(-1f, 1f)
                                    )
                                )
                                .background(ArmixYellow.copy(alpha = 0.9f), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visualização com Filtros por Dia e Programa da Semana
 */
@Composable
private fun ScheduleWeeklyResultsView(
    selectedDayNumber: Int,
    onSelectDay: (Int) -> Unit,
    songs: List<ProgramSongVote>,
    onGoToLive: () -> Unit,
    onVote: (String, Boolean) -> Unit
) {
    val listState = rememberLazyListState()

    val daysList = listOf(
        Triple(2, "SEG", "Segunda-feira"),
        Triple(3, "TER", "Terça-feira"),
        Triple(4, "QUA", "Quarta-feira"),
        Triple(5, "QUI", "Quinta-feira"),
        Triple(6, "SEX (6º)", "Sexta-feira (6º Dia)"),
        Triple(7, "SÁB", "Sábado"),
        Triple(1, "DOM", "Domingo")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Horizontal Day Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            daysList.forEach { (dayNum, chipLabel, _) ->
                val isSelected = selectedDayNumber == dayNum
                val isSixth = dayNum == 6

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                isSelected -> ArmixYellow
                                isSixth -> Color(0xFF261D04)
                                else -> Color(0xFF1B1B22)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                isSelected -> ArmixYellow
                                isSixth -> ArmixYellow.copy(alpha = 0.5f)
                                else -> Color(0xFF333342)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectDay(dayNum) }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = chipLabel,
                        color = when {
                            isSelected -> Color(0xFF141419)
                            isSixth -> ArmixYellow
                            else -> Color(0xFFDDDDDD)
                        },
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected || isSixth) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val currentDayName = daysList.firstOrNull { it.first == selectedDayNumber }?.third ?: "Programação"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = ArmixYellow,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentDayName,
                    color = Color(0xFFD0D0E0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${songs.size} voto(s)",
                color = Color(0xFF888899),
                fontSize = 10.5.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Songs list container with visible scrollbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F0F14))
                .border(1.dp, Color(0xFF282834), RoundedCornerShape(8.dp))
        ) {
            if (songs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF555566),
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NENHUM VOTO NESTE DIA",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Votações de demonstração zeradas. Faça login para registrar votos nas músicas da rádio!",
                        color = Color(0xFF888899),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onGoToLive,
                        colors = ButtonDefaults.buttonColors(containerColor = ArmixYellow),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "VOTAR NA MÚSICA AO VIVO",
                            color = Color(0xFF141419),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        ProgramSongVoteRow(
                            song = song,
                            isSixthDay = song.dayNumber == 6,
                            onVote = { isLike -> onVote(song.id, isLike) }
                        )
                    }
                }

                // Vertical Scrollbar
                val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
                val totalItemsCount by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }
                if (totalItemsCount > 2) {
                    val scrollFraction = firstVisibleItemIndex.toFloat() / (totalItemsCount - 2).coerceAtLeast(1)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 2.dp, top = 8.dp, bottom = 8.dp)
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.35f)
                                .align(
                                    androidx.compose.ui.BiasAlignment(
                                        horizontalBias = 0f,
                                        verticalBias = (scrollFraction * 2f - 1f).coerceIn(-1f, 1f)
                                    )
                                )
                                .background(ArmixYellow.copy(alpha = 0.9f), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Item Individual de Música Votada com Informações da Programação, Horário, Dia, Data e Botões de Voto
 */
@Composable
private fun ProgramSongVoteRow(
    song: ProgramSongVote,
    isSixthDay: Boolean,
    onVote: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isTopOne = song.rank == 1

    val liveArtwork by RadioPlayerManager.currentArtworkUrl.collectAsState()
    val liveTitle by RadioPlayerManager.currentTrackTitle.collectAsState()
    val liveArtist by RadioPlayerManager.currentArtist.collectAsState()

    val isMatchingCurrentPlayer = remember(song.title, song.artist, liveTitle, liveArtist) {
        val st = song.title.lowercase().replace(Regex("[^a-z0-9]"), "")
        val lt = liveTitle.lowercase().replace(Regex("[^a-z0-9]"), "")
        val sa = song.artist.lowercase().replace(Regex("[^a-z0-9]"), "")
        val la = liveArtist.lowercase().replace(Regex("[^a-z0-9]"), "")
        (st.contains(lt) || lt.contains(st)) || (sa.contains(la) || la.contains(sa))
    }

    val effectiveArtwork = remember(song.artworkUrl, liveArtwork, isMatchingCurrentPlayer) {
        if (!song.artworkUrl.isNullOrBlank()) {
            song.artworkUrl
        } else if (isMatchingCurrentPlayer && !liveArtwork.isNullOrBlank()) {
            liveArtwork
        } else null
    }

    androidx.compose.runtime.LaunchedEffect(song.id, song.title, song.artist, song.artworkUrl) {
        if (song.artworkUrl.isNullOrBlank()) {
            WeeklyVotingManager.ensureArtworkLoaded(song.id, song.title, song.artist)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isTopOne && isSixthDay -> Color(0xFF261D05)
                    isSixthDay -> Color(0xFF1E1A12)
                    else -> Color(0xFF171720)
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isTopOne && isSixthDay -> ArmixYellow
                    isSixthDay -> ArmixYellow.copy(alpha = 0.4f)
                    else -> Color(0xFF282836)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Program Schedule Tag & Time Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = if (isSixthDay) ArmixYellow else Color(0xFF81C784),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${song.programTitle} • ${song.presenter}",
                        color = if (isSixthDay) ArmixYellow else Color(0xFFEEEEEE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F0F14))
                        .border(1.dp, Color(0xFF333342), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (song.dateFormatted.isNotBlank()) "${song.timeSlot} • ${song.dateFormatted}" else song.timeSlot,
                            color = Color(0xFFBBBBBB),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Main Row: Artwork, Rank, Song Details, Approval & Voting Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork with Rank Badge (Capa do Artista)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22222E))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(effectiveArtwork ?: R.drawable.radio_station_logo)
                            .crossfade(true)
                            .error(R.drawable.radio_station_logo)
                            .placeholder(R.drawable.radio_station_logo)
                            .build(),
                        contentDescription = "Capa de ${song.title} - ${song.artist}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Rank badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(bottomEnd = 4.dp))
                            .background(
                                if (isTopOne) ArmixYellow else Color(0xFF141419).copy(alpha = 0.85f)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "#${song.rank}",
                            color = if (isTopOne) Color(0xFF141419) else Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Song Info: Nome da Música & Nome do Artista + Approval Bar
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = Color(0xFFDDDDED),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Progress Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { (song.approvalPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE53935)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${song.approvalPercentage}%",
                            color = Color(0xFFAAAAAA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Interactive Like & Dislike Links/Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Toca + (Gostei)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (song.userVote == "like") Color(0xFF2E482E)
                                else Color(0xFF1E2620)
                            )
                            .border(
                                width = 1.dp,
                                color = if (song.userVote == "like") Color(0xFF4CAF50) else Color(0xFF2A3D2D),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onVote(true) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Gostei",
                            tint = if (song.userVote == "like") Color(0xFF66BB6A) else Color(0xFFA5D6A7),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${song.likes}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Toca - (Não Gostei)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (song.userVote == "dislike") Color(0xFF4A1E20)
                                else Color(0xFF26191B)
                            )
                            .border(
                                width = 1.dp,
                                color = if (song.userVote == "dislike") Color(0xFFE53935) else Color(0xFF3F2528),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onVote(false) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Não Gostei",
                            tint = if (song.userVote == "dislike") Color(0xFFEF5350) else Color(0xFFEF9A9A),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${song.dislikes}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visualização da Votação Ao Vivo
 */
@Composable
private fun LiveVotingView(
    liveSongs: List<SongVoteItem>,
    onVoteSong: (String, Boolean) -> Unit,
    onVoteLiveCurrent: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val currentTitle by RadioPlayerManager.currentTrackTitle.collectAsState()
    val currentArtist by RadioPlayerManager.currentArtist.collectAsState()
    val currentArtwork by RadioPlayerManager.currentArtworkUrl.collectAsState()
    val likesCount by RadioPlayerManager.likesCount.collectAsState()
    val dislikesCount by RadioPlayerManager.dislikesCount.collectAsState()
    val userVote by RadioPlayerManager.userVote.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F0F14))
            .border(1.dp, Color(0xFF282834), RoundedCornerShape(8.dp))
    ) {
        if (liveSongs.isEmpty()) {
            // Apresenta a música no ar agora com contagem zerada para o ouvinte cadastrado inaugurar o 1º voto
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B1B24))
                        .border(1.dp, ArmixYellow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF22222E))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentArtwork ?: R.drawable.radio_station_logo)
                                .crossfade(true)
                                .error(R.drawable.radio_station_logo)
                                .placeholder(R.drawable.radio_station_logo)
                                .build(),
                            contentDescription = "Capa de $currentTitle",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ArmixYellow)
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "NO AR",
                                color = Color(0xFF141419),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTitle,
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentArtist,
                            color = Color(0xFFDDDDED),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Votação zerada • Aguardando 1º voto",
                            color = Color(0xFF888899),
                            fontSize = 9.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cadastre-se ou entre com seu login para registrar o primeiro voto válido nesta música!",
                    color = Color(0xFFBBBBCC),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onVoteLiveCurrent(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == "like") Color(0xFF2E482E) else Color(0xFF263326)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Toca + ($likesCount)",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }

                    Button(
                        onClick = { onVoteLiveCurrent(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == "dislike") Color(0xFF4A1E20) else Color(0xFF381F21)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Toca - ($dislikesCount)",
                            color = Color(0xFFEF5350),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(liveSongs, key = { it.id }) { song ->
                    LiveSongVoteRowItem(
                        song = song,
                        onVote = { isLike -> onVoteSong(song.id, isLike) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveSongVoteRowItem(
    song: SongVoteItem,
    onVote: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isLive = song.isLive || song.id == "live_current"

    val liveArtwork by RadioPlayerManager.currentArtworkUrl.collectAsState()
    val liveTitle by RadioPlayerManager.currentTrackTitle.collectAsState()
    val liveArtist by RadioPlayerManager.currentArtist.collectAsState()

    val effectiveArtwork = remember(song.artworkUrl, liveArtwork, isLive, liveTitle, liveArtist) {
        if (!song.artworkUrl.isNullOrBlank()) {
            song.artworkUrl
        } else if (isLive && !liveArtwork.isNullOrBlank()) {
            liveArtwork
        } else if (song.title.contains(liveTitle, ignoreCase = true) || liveTitle.contains(song.title, ignoreCase = true)) {
            liveArtwork
        } else null
    }

    androidx.compose.runtime.LaunchedEffect(song.id, song.title, song.artist, song.artworkUrl) {
        if (song.artworkUrl.isNullOrBlank()) {
            WeeklyVotingManager.ensureArtworkLoaded(song.id, song.title, song.artist)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLive) Color(0xFF221A06) else Color(0xFF1B1B24))
            .border(
                width = 1.dp,
                color = if (isLive) ArmixYellow.copy(alpha = 0.7f) else Color(0xFF2C2C3C),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF22222E))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(effectiveArtwork ?: song.fallbackResId ?: R.drawable.radio_station_logo)
                        .crossfade(true)
                        .error(song.fallbackResId ?: R.drawable.radio_station_logo)
                        .placeholder(song.fallbackResId ?: R.drawable.radio_station_logo)
                        .build(),
                    contentDescription = "Capa de ${song.title} - ${song.artist}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isLive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ArmixYellow)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "NO AR",
                            color = Color(0xFF1A1A1A),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = Color(0xFFDDDDED),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { (song.approvalPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${song.approvalPercentage}%",
                        color = Color(0xFFAAAAAA),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Toca +
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (song.userVote == "like") Color(0xFF2E482E)
                            else Color(0xFF1E2620)
                        )
                        .border(
                            width = 1.dp,
                            color = if (song.userVote == "like") Color(0xFF4CAF50) else Color(0xFF2A3D2D),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onVote(true) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Toca +",
                        tint = if (song.userVote == "like") Color(0xFF66BB6A) else Color(0xFFA5D6A7),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${song.likes}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Toca -
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (song.userVote == "dislike") Color(0xFF4A1E20)
                            else Color(0xFF26191B)
                        )
                        .border(
                            width = 1.dp,
                            color = if (song.userVote == "dislike") Color(0xFFE53935) else Color(0xFF3F2528),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onVote(false) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Toca -",
                        tint = if (song.userVote == "dislike") Color(0xFFEF5350) else Color(0xFFEF9A9A),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${song.dislikes}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
