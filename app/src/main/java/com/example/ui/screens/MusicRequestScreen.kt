package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.metadata.MusicSearchRepository
import com.example.metadata.SearchSongResult
import com.example.player.RadioPlayerManager
import com.example.player.RadioScheduleManager
import com.example.player.RegisteredMusicRequest
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.RadioRedDark
import com.example.ui.theme.RadioRedPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.auth.UserManager
import com.example.ui.components.AuthRequiredReason
import com.example.ui.components.LoginOrRegisterDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MusicRequestScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val isLoggedIn by UserManager.isLoggedIn.collectAsState()
    val currentUser by UserManager.currentUser.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    var pendingRequest by remember { mutableStateOf<Triple<String, String, String?>?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchSongResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var confirmedSongTitle by remember { mutableStateOf("") }
    var confirmedSongArtist by remember { mutableStateOf("") }

    val registeredRequests by RadioPlayerManager.registeredMusicRequests.collectAsState()
    val livePlayingAlert by RadioPlayerManager.songAlertMessage.collectAsState()
    val justFinishedAlert by RadioPlayerManager.justFinishedSongAlert.collectAsState()
    val onAirProgram by RadioScheduleManager.currentProgram.collectAsState()

    fun performSearch() {
        if (searchQuery.isNotBlank()) {
            focusManager.clearFocus()
            scope.launch {
                isSearching = true
                hasSearched = true
                searchResults = MusicSearchRepository.searchSongs(searchQuery)
                isSearching = false
            }
        } else {
            Toast.makeText(context, "Digite sua música ou artista", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSendRequest(title: String, artist: String, artworkUrl: String? = null, source: String = "iTunes") {
        if (!isLoggedIn) {
            pendingRequest = Triple(title, artist, artworkUrl)
            showAuthDialog = true
            return
        }
        confirmedSongTitle = title
        confirmedSongArtist = artist
        RadioPlayerManager.sendRequestToWhatsApp(
            context = context,
            title = title,
            artist = artist,
            artworkUrl = artworkUrl,
            source = source
        )
        showSuccessDialog = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Golden fiber background matching design
        Image(
            painter = painterResource(id = R.drawable.gold_fiber_waves),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f)
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkBackground.copy(alpha = 0.85f),
                            DarkBackground.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. On-Air Program & Locutor Photo Banner (Direct from radiostudioarmix.com.br)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF221703),
                                    DarkSurface.copy(alpha = 0.95f),
                                    Color(0xFF1E1502)
                                )
                            )
                        )
                        .border(1.dp, RadioRedPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Locutor Photo from radiostudioarmix.com.br with glowing live circle
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = onAirProgram.photoUrl,
                                contentDescription = onAirProgram.presenter,
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.radio_station_logo),
                                placeholder = painterResource(id = R.drawable.radio_station_logo),
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, RadioRedPrimary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD32F2F))
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RadioRedPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NO AR AGORA",
                                        color = Color(0xFF1A1A1A),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "radiostudioarmix.com.br",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = onAirProgram.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = RadioRedPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Locutor(a): ${onAirProgram.presenter}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 2. Alert: Song currently playing on live stream!
            item {
                AnimatedVisibility(
                    visible = livePlayingAlert != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    livePlayingAlert?.let { alertText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFB71C1C),
                                            Color(0xFFE65100)
                                        )
                                    )
                                )
                                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alertText,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 17.sp
                                    )
                                    Text(
                                        text = "Ao término desta música, ela será concluída dando espaço para novos pedidos!",
                                        color = Color(0xFFFFE082),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Alert: Song just completed and cleared from the requests list
            item {
                AnimatedVisibility(
                    visible = justFinishedAlert != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    justFinishedAlert?.let { finishedText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B5E20).copy(alpha = 0.95f))
                                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = finishedText,
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { RadioPlayerManager.dismissSongAlerts() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fechar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Search Box (Matching Screenshot: White background with black text)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isBlank()) {
                                hasSearched = false
                                searchResults = emptyList()
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Digite sua música ou artista",
                                color = Color(0xFF757575),
                                fontSize = 15.sp
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    hasSearched = false
                                    searchResults = emptyList()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpar busca",
                                        tint = Color.DarkGray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = RadioRedPrimary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_music_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Yellow Pesquisar button matching screenshot
                    Button(
                        onClick = { performSearch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadioRedPrimary,
                            contentColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("search_music_button")
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Buscando no iTunes / Deezer...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1A1A)
                            )
                        } else {
                            Text(
                                text = "Pesquisar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }

            // 5. Section: Registered Requests (Seus Pedidos Registrados)
            if (registeredRequests.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = RadioRedPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SEUS PEDIDOS REGISTRADOS",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RadioRedPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, RadioRedPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${registeredRequests.size} ativo(s)",
                                    color = RadioRedPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                items(registeredRequests) { request ->
                    RegisteredRequestCard(
                        request = request,
                        onResend = {
                            handleSendRequest(
                                title = request.title,
                                artist = request.artist,
                                artworkUrl = request.artworkUrl,
                                source = request.source
                            )
                        }
                    )
                }
            }

            // 6. Section: Real-time Search Results from iTunes / Deezer
            if (hasSearched) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = RadioRedPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (searchResults.isNotEmpty()) {
                                "RESULTADOS ENCONTRADOS (${searchResults.firstOrNull()?.source ?: "Música"})"
                            } else {
                                "RESULTADO DA BUSCA"
                            },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (searchResults.isNotEmpty()) {
                    items(searchResults) { result ->
                        SearchResultCard(
                            result = result,
                            currentProgramName = onAirProgram.title,
                            currentPresenterName = onAirProgram.presenter,
                            onPedirClick = {
                                handleSendRequest(
                                    title = result.title,
                                    artist = result.artist,
                                    artworkUrl = result.artworkUrl,
                                    source = result.source
                                )
                            }
                        )
                    }
                } else if (!isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface.copy(alpha = 0.9f))
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nenhum resultado encontrado no iTunes ou Deezer para:",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$searchQuery\"",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        handleSendRequest(
                                            title = searchQuery,
                                            artist = "Pedido do Ouvinte",
                                            source = "Direto"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RadioRedPrimary,
                                        contentColor = Color(0xFF1A1A1A)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pedir essa música no WhatsApp",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (registeredRequests.isEmpty()) {
                // Initial zeroed-out state with clear instructions (zero demo/fictitious songs)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface.copy(alpha = 0.85f))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(RadioRedPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = RadioRedPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Faça seu Pedido Musical!",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Digite sua música ou artista favorito acima para buscar no catálogo oficial do iTunes e Deezer. O pedido será encaminhado diretamente ao WhatsApp do locutor no ar!",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = RadioRedPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "O app avisará quando sua música tocar no ar!",
                                    color = RadioRedPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Success dialog confirming registration and dispatch to WhatsApp
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RadioRedPrimary,
                    modifier = Modifier.size(46.dp)
                )
            },
            title = {
                Text(
                    text = "Pedido Registrado!",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sua música foi registrada e encaminhada ao WhatsApp da Rádio Studio Armix:",
                        color = TextSecondary,
                        fontSize = 13.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, RadioRedPrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = confirmedSongTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                            Text(
                                text = confirmedSongArtist,
                                color = RadioRedPrimary,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📻 Programa: ${onAirProgram.title}",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "🎙️ Locutor(a): ${onAirProgram.presenter}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = "Assim que tocar no ar, o aplicativo emitirá um alerta e liberará o campo para novos pedidos ao término da música!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadioRedPrimary,
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("OK", color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showAuthDialog) {
        LoginOrRegisterDialog(
            reason = AuthRequiredReason.MUSIC_REQUEST,
            onDismiss = {
                showAuthDialog = false
                pendingRequest = null
            },
            onLoginSuccess = {
                showAuthDialog = false
                pendingRequest?.let { (t, a, art) ->
                    handleSendRequest(t, a, art)
                }
                pendingRequest = null
            },
            onNavigateToRegistration = {
                showAuthDialog = false
                pendingRequest = null
                onNavigateToRegistration()
            }
        )
    }
}

@Composable
private fun SearchResultCard(
    result: SearchSongResult,
    currentProgramName: String,
    currentPresenterName: String,
    onPedirClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface.copy(alpha = 0.95f))
            .border(1.dp, RadioRedPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art from iTunes / Deezer
            if (!result.artworkUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = result.artworkUrl,
                    contentDescription = result.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2A2A33)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = RadioRedPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.artist + if (!result.album.isNullOrEmpty()) " • ${result.album}" else "",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RadioRedPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = result.source,
                            color = RadioRedPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "No ar: $currentProgramName",
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onPedirClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RadioRedDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("pedir_button_${result.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Pedir Música",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pedir",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RegisteredRequestCard(
    request: RegisteredMusicRequest,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember(request.requestedAt) {
        try {
            val sdf = SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault())
            sdf.format(Date(request.requestedAt))
        } catch (e: Exception) {
            "Recentemente"
        }
    }

    val isNowPlaying = request.isPlayingNow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isNowPlaying) Color(0xFF2C1E03) else DarkSurface.copy(alpha = 0.9f)
            )
            .border(
                1.dp,
                if (isNowPlaying) RadioRedPrimary else Color(0xFF2E7D32).copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!request.artworkUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = request.artworkUrl,
                    contentDescription = request.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isNowPlaying) RadioRedPrimary.copy(alpha = 0.2f) else Color(0xFF1E3A20)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isNowPlaying) Icons.Default.GraphicEq else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isNowPlaying) RadioRedPrimary else Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${request.artist} • $dateText",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!request.programTitle.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📻 ${request.programTitle}" + if (!request.presenterName.isNullOrEmpty()) " (${request.presenterName})" else "",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isNowPlaying) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFD32F2F))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "🔥 TOCANDO NO AR AGORA!",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Enviado WhatsApp (+55 85 99278-8437)",
                            color = Color(0xFF81C784),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(
                onClick = onResend,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("resend_request_${request.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Reenviar no WhatsApp",
                    tint = RadioRedPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
