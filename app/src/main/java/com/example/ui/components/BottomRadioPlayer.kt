package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.player.PlaybackState
import com.example.player.RadioPlayerManager
import com.example.player.RadioScheduleManager

@Composable
fun BottomRadioPlayer(
    modifier: Modifier = Modifier,
    onProgramClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val playbackState by RadioPlayerManager.playbackState.collectAsState()
    val trackTitle by RadioPlayerManager.currentTrackTitle.collectAsState()
    val artist by RadioPlayerManager.currentArtist.collectAsState()
    val subtitle by RadioPlayerManager.currentSubtitle.collectAsState()
    val artworkUrl by RadioPlayerManager.currentArtworkUrl.collectAsState()
    val isCommercialBreak by RadioPlayerManager.isCommercialBreak.collectAsState()
    val onAirProgram by RadioScheduleManager.currentProgram.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // On-air program & locutor card sitting on top of the bar, glued to the left side
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF221703),
                            Color(0xFF141418)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFF5A603).copy(alpha = 0.65f),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                )
                .clickable { onProgramClick?.invoke() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("player_on_air_tab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Locutor avatar with red live dot
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = onAirProgram.photoUrl,
                        contentDescription = onAirProgram.presenter,
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.radio_station_logo),
                        placeholder = painterResource(id = R.drawable.radio_station_logo),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFF5A603), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F))
                            .border(1.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF5A603))
                                .padding(horizontal = 4.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = "NO AR AGORA",
                                color = Color(0xFF111111),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "radiostudioarmix.com.br",
                            color = Color(0xFF888899),
                            fontSize = 8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = onAirProgram.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFFF5A603),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = onAirProgram.presenter,
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Player bottom bar with yellow-to-black vertical gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5A603), // Amarelo na parte de cima
                            Color(0xFF000000)  // Preto na parte de baixo
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail: iTunes artwork when song plays, radio logo during commercials
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(artworkUrl ?: R.drawable.radio_station_logo)
                        .crossfade(true)
                        .error(R.drawable.radio_station_logo)
                        .placeholder(R.drawable.radio_station_logo)
                        .build(),
                    contentDescription = if (isCommercialBreak) "Logo da Rádio" else "Capa do Álbum",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .border(1.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Track metadata: Title in black, Artist in white bold
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trackTitle,
                        color = Color.Black,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play / Pause Icon Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { RadioPlayerManager.togglePlayPause(context) }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    when (playbackState) {
                        is PlaybackState.Buffering -> {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is PlaybackState.Playing -> {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausar",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Tocar",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Share button
                IconButton(
                    onClick = { shareRadioStream(context) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun shareRadioStream(context: Context) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Ouça a rádio ao vivo! Tocando os maiores sucessos: ${RadioPlayerManager.STREAM_URL}"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Rádio")
        context.startActivity(shareIntent)
    } catch (_: Exception) {}
}
