package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RadioScreen {
    HOME,
    MUSIC_REQUEST,
    PROMOTIONS,
    HOURLY_PRIZE,
    SLEEP_TIMER,
    ADMIN_PANEL,
    REGISTRATION
}

data class QuickActionItem(
    val screen: RadioScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

// Deep dark circular buttons for crisp contrast on the yellow header bar
val ActionCircleBg = Color(0xFF201905)
val ActionCircleBorder = Color(0xFF5E460E)

@Composable
fun QuickActionsRow(
    currentScreen: RadioScreen,
    onSelectScreen: (RadioScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem(
            screen = RadioScreen.MUSIC_REQUEST,
            title = "PEÇA SUA\nMÚSICA",
            icon = Icons.Default.MusicNote,
            testTag = "action_request_music"
        ),
        QuickActionItem(
            screen = RadioScreen.PROMOTIONS,
            title = "PROMO\nÇÃO",
            icon = Icons.Default.Equalizer,
            testTag = "action_promotions"
        ),
        QuickActionItem(
            screen = RadioScreen.HOME,
            title = "INÍCIO",
            icon = Icons.Default.Headphones,
            testTag = "action_home"
        ),
        QuickActionItem(
            screen = RadioScreen.HOURLY_PRIZE,
            title = "PRÊMIO\nDA HORA",
            icon = Icons.Default.Speed,
            testTag = "action_hourly_prize"
        ),
        QuickActionItem(
            screen = RadioScreen.SLEEP_TIMER,
            title = "ANÚNCIOS",
            icon = Icons.Default.Campaign,
            testTag = "action_ads_carousel"
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ArmixYellow)
            .padding(bottom = 10.dp, top = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF000000)
                            else ActionCircleBg
                        )
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color.White else ActionCircleBorder,
                            shape = CircleShape
                        )
                        .clickable { onSelectScreen(item.screen) }
                        .testTag(item.testTag)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title.replace("\n", " "),
                            tint = if (isSelected) ArmixYellow else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            color = if (isSelected) ArmixYellow else Color.White,
                            fontSize = if (item.title.contains("\n")) 7.5.sp else 8.5.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 9.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
