package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.admin.AdminPromotion
import com.example.admin.RadioAdminManager
import com.example.auth.UserManager
import com.example.player.RadioScheduleManager
import com.example.ui.components.ArmixYellow
import com.example.ui.components.JpRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.AuthRequiredReason
import com.example.ui.components.LoginOrRegisterDialog

/**
 * Tela de Promoções e Premiações fiel ao Print 3.
 * Carrossel de prêmios integrado com a programação no ar no momento
 * e cadastro de ouvinte sincronizado diretamente com o Painel Admin.
 */
@Composable
fun PromotionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    val promotions by RadioAdminManager.promotions.collectAsState()
    val activePromos = remember(promotions) {
        val list = promotions.filter { it.active }
        if (list.isNotEmpty()) list else promotions
    }

    val currentUser by UserManager.currentUser.collectAsState()
    val isLoggedIn by UserManager.isLoggedIn.collectAsState()
    val onAirProgram by RadioScheduleManager.currentProgram.collectAsState()

    var currentPromoIndex by remember { mutableIntStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var successDialogTitle by remember { mutableStateOf("") }
    var successDialogMessage by remember { mutableStateOf("") }
    var showTermsDialog by remember { mutableStateOf(false) }

    val safeIndex = if (activePromos.isNotEmpty()) currentPromoIndex % activePromos.size else 0
    val currentPromo: AdminPromotion? = if (activePromos.isNotEmpty()) activePromos[safeIndex] else null

    // Autoplay suave do carrossel (a cada 7 segundos caso o ouvinte não interaja)
    LaunchedEffect(activePromos.size) {
        if (activePromos.size > 1) {
            while (true) {
                delay(7000L)
                currentPromoIndex = (currentPromoIndex + 1) % activePromos.size
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Red and black fiber wave backdrop identical to Print 3
        Image(
            painter = painterResource(id = R.drawable.red_fiber_waves_1793787358802),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Barra de Programa no Ar no Momento (Sincronizado com a grade da rádio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, ArmixYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ArmixYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PROGRAMAÇÃO AO VIVO",
                                    color = ArmixYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "${onAirProgram.title} • ${onAirProgram.presenter}",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JpRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NO AR",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Carrossel de Banners de Premiação (Print 3 fiel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Banner Card animado com transição de crossfade
                AnimatedContent(
                    targetState = safeIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "promo_carousel_anim"
                ) { targetIdx ->
                    val promo = if (activePromos.isNotEmpty()) activePromos[targetIdx] else null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .shadow(8.dp, RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .background(Color(0xFF141414))
                            .testTag("promo_carousel_card_$targetIdx")
                    ) {
                        // Renderiza o banner do prêmio (local drawable ou URL da promoção)
                        when {
                            promo?.id == "promo_basike_speaker" -> {
                                Image(
                                    painter = painterResource(id = R.drawable.promo_basike_speaker_1794049765618),
                                    contentDescription = promo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            promo?.id == "promo_smartwatch_ultra" -> {
                                Image(
                                    painter = painterResource(id = R.drawable.promo_smartwatch_prize_1794049779277),
                                    contentDescription = promo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            !promo?.bannerUrl.isNullOrBlank() -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(promo!!.bannerUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = promo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Image(
                                    painter = painterResource(id = R.drawable.sequencia_suculenta_1793788011099),
                                    contentDescription = promo?.title ?: "Promoção",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Gradiente escuro sutil na base do card para realçar o título e categoria
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = promo?.tag ?: "PROMO ESPECIAL",
                                        color = ArmixYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = promo?.schedule ?: "Sorteio ao vivo",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = promo?.title ?: "Prêmio Oficial da Rádio",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Seta de navegação esquerda (<)
                if (activePromos.size > 1) {
                    IconButton(
                        onClick = {
                            currentPromoIndex = if (currentPromoIndex > 0) currentPromoIndex - 1 else activePromos.size - 1
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 2.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("btn_prev_promo")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Promoção Anterior",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Seta de navegação direita (>)
                    IconButton(
                        onClick = {
                            currentPromoIndex = (currentPromoIndex + 1) % activePromos.size
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 2.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("btn_next_promo")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Próxima Promoção",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Indicadores de Páginas do Carrossel (Pontos • • • do Print 3)
            if (activePromos.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activePromos.forEachIndexed { idx, _ ->
                        val isSelected = idx == safeIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
                                )
                                .clickable { currentPromoIndex = idx }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Botão PARTICIPAR (Print 3 fiel com ícone de check e cor vermelha da emissora)
            Button(
                onClick = {
                    val user = currentUser
                    if (user == null || !isLoggedIn) {
                        showAuthDialog = true
                    } else {
                        // Ouvinte já cadastrado: registra a participação atrelada à promoção e ao programa no ar!
                        val promo = currentPromo ?: activePromos.firstOrNull()
                        val campaignId = promo?.id ?: "promo_geral"
                        val campaignTitle = promo?.title ?: "Promoção Studio Armix"
                        val programDesc = "${onAirProgram.title} (${onAirProgram.presenter})"
                        val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                        RadioAdminManager.registerFullParticipant(
                            name = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            phone = user.phone,
                            birthDate = user.birthDate,
                            gender = user.gender,
                            campaignId = campaignId,
                            campaignTitle = campaignTitle,
                            onAirProgram = programDesc,
                            notes = "Inscrição confirmada pelo botão PARTICIPAR"
                        )

                        successDialogTitle = "PARTICIPAÇÃO CONFIRMADA!"
                        successDialogMessage = "Parabéns, ${user.firstName}!\n\n" +
                                "Você já está concorrendo ao prêmio:\n👉 \"$campaignTitle\"\n\n" +
                                "📻 Programa No Ar: $programDesc\n" +
                                "⏰ Inscrição: $currentTime\n" +
                                "📱 WhatsApp: ${user.phone}\n\n" +
                                "Sua participação foi enviada diretamente para o Painel Admin da emissora. Fique ligado na programação da Studio Armix!"
                        showSuccessDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JpRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(48.dp)
                    .testTag("btn_participar_promo")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PARTICIPAR",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Link "Termos" (Print 3 fiel)
            Text(
                text = "Termos",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { showTermsDialog = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("btn_promo_terms")
            )
        }
    }

    // Modal de Sucesso na Inscrição
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ArmixYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = successDialogTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = successDialogMessage,
                    color = Color(0xFFDDDDDD),
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JpRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("OK, Entendido!", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Acabei de me cadastrar para concorrer ao prêmio na Rádio Studio Armix no programa ${onAirProgram.title}! Sintonize você também pelo app oficial ou pelo site https://radiostudioarmix.com.br"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Participação"))
                        } catch (_: Exception) { }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartilhar", fontSize = 12.sp)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Modal Regulamento e Termos da Promoção
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Regulamento Oficial da Promoção",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "• Período: Promoção válida durante toda a programação oficial da emissora.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Participação: O ouvinte deve estar devidamente cadastrado no aplicativo com Nome, Sobrenome, WhatsApp, E-mail e Data de Nascimento.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Sorteio: Os sorteios acontecem ao vivo conforme anunciado pelos locutores da Studio Armix.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Entrega: O contemplado receberá contato oficial pelo WhatsApp cadastrado ou telefone para alinhamento da retirada ou envio do prêmio.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JpRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Fechar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showAuthDialog) {
        LoginOrRegisterDialog(
            reason = AuthRequiredReason.PROMOTION,
            onDismiss = { showAuthDialog = false },
            onLoginSuccess = { showAuthDialog = false },
            onNavigateToRegistration = {
                showAuthDialog = false
                onNavigateToRegistration()
            }
        )
    }
}
