package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.admin.RadioAdminManager
import com.example.player.RadioPlayerManager
import com.example.ui.components.ArmixYellow
import com.example.ui.components.RadioScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.RadioRedBright
import com.example.ui.theme.RadioRedPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NavigationDrawerContent(
    currentScreen: RadioScreen,
    onNavigate: (RadioScreen) -> Unit,
    onOpenLogin: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationsEnabled by RadioPlayerManager.notificationsEnabled.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var isPinVisible by remember { mutableStateOf(false) }
    var codeSentMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drawer Header matching screenshot
        Text(
            text = "Menu",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        val currentUser by com.example.auth.UserManager.currentUser.collectAsState()
        val isLoggedIn by com.example.auth.UserManager.isLoggedIn.collectAsState()

        Text(
            text = if (isLoggedIn && currentUser != null) "Olá, ${currentUser!!.firstName}!" else "Olá! Ouvinte Studio Armix",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Menu Items
        DrawerItem(
            icon = Icons.Default.Home,
            title = "Home",
            isSelected = currentScreen == RadioScreen.HOME,
            onClick = {
                onNavigate(RadioScreen.HOME)
                onCloseDrawer()
            },
            testTag = "drawer_item_home"
        )

        DrawerItem(
            icon = Icons.Default.AccountCircle,
            title = if (isLoggedIn && currentUser != null) "Meu Cadastro" else "Cadastro / Login",
            isSelected = currentScreen == RadioScreen.REGISTRATION,
            badge = if (isLoggedIn && currentUser != null) "ATIVO" else null,
            onClick = {
                onCloseDrawer()
                onOpenLogin()
            },
            testTag = "drawer_item_login"
        )

        DrawerItem(
            icon = Icons.Default.CardGiftcard,
            title = "Promoções",
            isSelected = currentScreen == RadioScreen.PROMOTIONS,
            onClick = {
                onNavigate(RadioScreen.PROMOTIONS)
                onCloseDrawer()
            },
            testTag = "drawer_item_promotions"
        )

        DrawerItem(
            icon = Icons.Default.Star,
            title = "Prêmio da hora",
            isSelected = currentScreen == RadioScreen.HOURLY_PRIZE,
            onClick = {
                onNavigate(RadioScreen.HOURLY_PRIZE)
                onCloseDrawer()
            },
            testTag = "drawer_item_hourly_prize"
        )

        DrawerItem(
            icon = Icons.Default.MusicNote,
            title = "Peça sua música",
            isSelected = currentScreen == RadioScreen.MUSIC_REQUEST,
            onClick = {
                onNavigate(RadioScreen.MUSIC_REQUEST)
                onCloseDrawer()
            },
            testTag = "drawer_item_music_request"
        )

        DrawerItem(
            icon = Icons.Default.Campaign,
            title = "Campo de Anúncios",
            isSelected = currentScreen == RadioScreen.SLEEP_TIMER,
            onClick = {
                onNavigate(RadioScreen.SLEEP_TIMER)
                onCloseDrawer()
            },
            testTag = "drawer_item_ads"
        )

        DrawerItem(
            icon = Icons.Default.AdminPanelSettings,
            title = "Painel Admin",
            isSelected = currentScreen == RadioScreen.ADMIN_PANEL,
            badge = "ADMIN",
            onClick = {
                if (RadioAdminManager.isAuthenticatedAsAdmin) {
                    onNavigate(RadioScreen.ADMIN_PANEL)
                    onCloseDrawer()
                } else {
                    showPinDialog = true
                }
            },
            testTag = "drawer_item_admin_panel"
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Notificações Section matching screenshot
        Text(
            text = "Notificações",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Geral",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { RadioPlayerManager.toggleNotifications(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RadioRedBright,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                ),
                modifier = Modifier.testTag("notifications_switch")
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Drawer Footer matching screenshot
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "aplicativo criado e desenvolvido para",
                color = TextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(RadioRedPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STUDIO ARMIX",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                enteredPin = ""
                pinError = false
                isPinVisible = false
                codeSentMessage = null
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(ArmixYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Acesso Restrito ao Painel",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "O código PIN é estritamente confidencial e deve ser solicitado para o número do administrador autorizado:",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )

                    // Target Contact Info Box (Only this number/email receives it)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF161616))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = ArmixYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WhatsApp: +55 85 99278-8437",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = ArmixYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Email: roberios9.tialpaz@gmail.com",
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Button to receive PIN on WhatsApp (+5585992788437)
                    Button(
                        onClick = {
                            val ok = RadioAdminManager.sendCodeViaWhatsApp(context)
                            if (ok) {
                                codeSentMessage = "Código enviado para o WhatsApp (+55 85 99278-8437)!"
                                Toast.makeText(context, "Abrindo WhatsApp com código de segurança...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Não foi possível abrir o WhatsApp", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_request_pin_whatsapp")
                    ) {
                        Text(
                            text = "Receber PIN no WhatsApp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Row with SMS & Email buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val ok = RadioAdminManager.sendCodeViaSms(context)
                                if (ok) {
                                    codeSentMessage = "Código enviado por SMS (+55 85 99278-8437)!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2A2A),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_request_pin_sms")
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Via SMS", fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                val ok = RadioAdminManager.sendCodeViaEmail(context)
                                if (ok) {
                                    codeSentMessage = "Código enviado para roberios9.tialpaz@gmail.com!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2A2A),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_request_pin_email")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Via Email", fontSize = 11.5.sp)
                        }
                    }

                    if (codeSentMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1B5E20).copy(alpha = 0.35f))
                                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✓ $codeSentMessage\nDigite o PIN recebido abaixo para entrar:",
                                color = Color(0xFFA5D6A7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Hidden Masked PIN Input (PasswordVisualTransformation)
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("Código PIN de Autorização") },
                        placeholder = { Text("••••••") },
                        singleLine = true,
                        isError = pinError,
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ArmixYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPinVisible) "Ocultar PIN" else "Mostrar PIN",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArmixYellow,
                            focusedLabelColor = ArmixYellow,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            errorBorderColor = Color(0xFFFF5252)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                    )

                    if (pinError) {
                        Text(
                            text = "Código PIN inválido ou expirado. Toque acima para receber um novo código.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.5.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (RadioAdminManager.verifyPin(enteredPin)) {
                            showPinDialog = false
                            enteredPin = ""
                            pinError = false
                            isPinVisible = false
                            codeSentMessage = null
                            onNavigate(RadioScreen.ADMIN_PANEL)
                            onCloseDrawer()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArmixYellow,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("admin_pin_confirm_button")
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showPinDialog = false
                        enteredPin = ""
                        pinError = false
                        isPinVisible = false
                        codeSentMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextSecondary
                    )
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) ArmixYellow else Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = if (isSelected) ArmixYellow else Color.White,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(ArmixYellow)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
