package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

// The exact reference yellow from the official Rádio Studio Armix logo (#F5A603)
val ArmixYellow = Color(0xFFF5A603)
val JpRed = ArmixYellow

@Composable
fun TopRadioBar(
    onOpenMenu: () -> Unit,
    onPhoneClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ArmixYellow)
            .statusBarsPadding()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center
        ) {
            // Hamburger menu button (dark lines for clear contrast on yellow) aligned to the left
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Abrir Menu",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Official station logo (Rádio Studio Armix) aligned strictly in the center
            Image(
                painter = painterResource(id = R.drawable.radio_station_logo),
                contentDescription = "Logo Rádio Studio Armix",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .testTag("station_logo")
            )

            // Right Action Icons (Phone + Envelope) aligned to the right
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPhoneClick,
                    modifier = Modifier.testTag("phone_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "Ligar",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onEmailClick,
                    modifier = Modifier.testTag("email_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Mensagem",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
