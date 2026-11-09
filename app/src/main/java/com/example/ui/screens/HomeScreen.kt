package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.player.RadioPlayerManager
import com.example.ui.components.ArmixYellow
import com.example.ui.components.JpRed
import com.example.auth.UserManager
import com.example.ui.components.AuthRequiredReason
import com.example.ui.components.LoginOrRegisterDialog
import com.example.ui.components.SongVotingSection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun HomeScreen(
    onNavigateToRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoggedIn by UserManager.isLoggedIn.collectAsState()
    val currentUser by UserManager.currentUser.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    var pendingVoteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val trackTitle by RadioPlayerManager.currentTrackTitle.collectAsState()
    val artist by RadioPlayerManager.currentArtist.collectAsState()
    val subtitle by RadioPlayerManager.currentSubtitle.collectAsState()
    val artworkUrl by RadioPlayerManager.currentArtworkUrl.collectAsState()
    val isCommercialBreak by RadioPlayerManager.isCommercialBreak.collectAsState()
    val likesCount by RadioPlayerManager.likesCount.collectAsState()
    val dislikesCount by RadioPlayerManager.dislikesCount.collectAsState()
    val userVote by RadioPlayerManager.userVote.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Monitor de Status do Player com Capa Dinâmica (iTunes na música vs Logo no intervalo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUrl ?: R.drawable.radio_station_logo)
                    .crossfade(true)
                    .error(R.drawable.radio_station_logo)
                    .placeholder(R.drawable.radio_station_logo)
                    .build(),
                contentDescription = if (isCommercialBreak) "Logo da Rádio Studio Armix" else "Capa do Álbum - $artist - $trackTitle",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("album_art_full_width")
            )

            // Indicador visual do Monitor de Status do Player
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isCommercialBreak)
                            Color(0xFFE65100).copy(alpha = 0.92f)
                        else
                            Color(0xFF1B5E20).copy(alpha = 0.92f)
                    )
                    .border(
                        1.dp,
                        if (isCommercialBreak) Color(0xFFFFB74D) else Color(0xFF81C784),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("player_status_badge")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isCommercialBreak) "INTERVALO COMERCIAL" else "MÚSICA • ITUNES",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Subtitle / Album banner right above the red bar (matching the print: "AO VIVO EM UBERLÂNDIA - VOL.01" or interval notice)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isCommercialBreak) "INTERVALO COMERCIAL • RÁDIO STUDIO ARMIX" else subtitle.uppercase(),
                color = if (isCommercialBreak) Color.Yellow else Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }

        // 2. Yellow Vote and Current Song Bar (flush with the album art, matching station logo yellow)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArmixYellow)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Toca + and Thumbs Up
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            if (!isLoggedIn) {
                                pendingVoteAction = {
                                    val voted = RadioPlayerManager.voteLike()
                                    val userName = currentUser?.firstName ?: "Ouvinte"
                                    val msg = if (voted) "Primeiro voto válido (Toca +) por $userName!" else "Voto removido"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                showAuthDialog = true
                            } else {
                                val voted = RadioPlayerManager.voteLike()
                                val userName = currentUser?.firstName ?: "Ouvinte"
                                val msg = if (voted) "Voto computado por $userName!" else "Voto removido"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("vote_like_button")
                ) {
                    Text(
                        text = "Toca +",
                        color = Color(0xFF1A1A1A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Toca +",
                            tint = if (userVote == "like") Color(0xFF1B5E20) else Color(0xFF1A1A1A),
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$likesCount",
                            color = Color(0xFF1A1A1A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center: Artist | Song & NO AR AGORA
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isCommercialBreak) "Rádio Studio Armix | Guaiúba-CE" else "$artist | $trackTitle",
                        color = Color(0xFF1A1A1A),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isCommercialBreak) "INTERVALO COMERCIAL" else "NO AR AGORA",
                        color = Color(0xFF1A1A1A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // Right: Toca - and Thumbs Down
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            if (!isLoggedIn) {
                                pendingVoteAction = {
                                    val voted = RadioPlayerManager.voteDislike()
                                    val userName = currentUser?.firstName ?: "Ouvinte"
                                    val msg = if (voted) "Primeiro voto válido (Toca -) por $userName!" else "Voto removido"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                showAuthDialog = true
                            } else {
                                val voted = RadioPlayerManager.voteDislike()
                                val userName = currentUser?.firstName ?: "Ouvinte"
                                val msg = if (voted) "Voto computado por $userName!" else "Voto removido"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("vote_dislike_button")
                ) {
                    Text(
                        text = "Toca -",
                        color = Color(0xFF1A1A1A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Toca -",
                            tint = if (userVote == "dislike") Color(0xFFB71C1C) else Color(0xFF1A1A1A),
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$dislikesCount",
                            color = Color(0xFF1A1A1A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Golden Wave Abstract Wallpaper with Centered White Outline Social Icons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.gold_fiber_waves),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Centered 3 large white outline icons directly floating over wallpaper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Instagram
                Icon(
                    painter = painterResource(id = R.drawable.ic_instagram),
                    contentDescription = "Instagram",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { openUrl(context, "https://instagram.com") }
                        .testTag("social_instagram")
                )

                // WhatsApp
                Icon(
                    painter = painterResource(id = R.drawable.ic_whatsapp),
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { openWhatsApp(context) }
                        .testTag("social_whatsapp")
                )

                // X (Twitter)
                Icon(
                    painter = painterResource(id = R.drawable.ic_twitter_x),
                    contentDescription = "X (Twitter)",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { openUrl(context, "https://x.com") }
                        .testTag("social_twitter_x")
                )
            }
        }

        // 4. Dedicated Vertical Scrollable Field with Likes and Dislikes (Toca + / Toca -) with Cover Art and Song Titles
        SongVotingSection(
            onNavigateToRegistration = onNavigateToRegistration,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAuthDialog) {
        LoginOrRegisterDialog(
            reason = AuthRequiredReason.VOTE,
            onDismiss = {
                showAuthDialog = false
                pendingVoteAction = null
            },
            onLoginSuccess = {
                showAuthDialog = false
                pendingVoteAction?.invoke()
                pendingVoteAction = null
            },
            onNavigateToRegistration = {
                showAuthDialog = false
                pendingVoteAction = null
                onNavigateToRegistration()
            }
        )
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Não foi possível abrir o link", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=Ol%C3%A1!%20Gostaria%20de%20pedir%20uma%20m%C3%BAsica%20na%20r%C3%A1dio:")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "WhatsApp não encontrado", Toast.LENGTH_SHORT).show()
    }
}
