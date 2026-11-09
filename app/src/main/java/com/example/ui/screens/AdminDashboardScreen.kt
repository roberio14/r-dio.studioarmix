package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.admin.AdminPromotion
import com.example.admin.PromotionParticipant
import com.example.admin.RadioAdminManager
import com.example.admin.SponsorAdBanner
import com.example.admin.StationConfig
import com.example.ui.components.ArmixYellow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    val promotions by RadioAdminManager.promotions.collectAsState()
    val participants by RadioAdminManager.participants.collectAsState()
    val banners by RadioAdminManager.sponsorBanners.collectAsState()
    val config by RadioAdminManager.stationConfig.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Promoções", "Inscritos (${participants.size})", "Anúncios / Loja", "Configurações")

    // Dialog state for adding/editing promotions
    var promoDialogTarget by remember { mutableStateOf<AdminPromotion?>(null) }
    var isNewPromo by remember { mutableStateOf(false) }

    // Dialog state for adding/editing sponsor banners
    var bannerDialogTarget by remember { mutableStateOf<SponsorAdBanner?>(null) }
    var isNewBanner by remember { mutableStateOf(false) }

    // Draw Winner dialog state
    var drawnWinner by remember { mutableStateOf<PromotionParticipant?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141414))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("admin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = ArmixYellow
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ArmixYellow)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Painel Studio Armix",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Gestão de promoções, sorteios e anúncios",
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                }

                // Secure Logout button
                IconButton(
                    onClick = {
                        RadioAdminManager.logoutAdmin()
                        Toast.makeText(context, "Sessão do Painel Admin encerrada com segurança", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sair do Painel Admin",
                        tint = Color(0xFFFF5252)
                    )
                }
            }

            // Tab bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1F1F1F),
                contentColor = ArmixYellow,
                edgePadding = 12.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) ArmixYellow else Color(0xFFAAAAAA),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> PromotionsTab(
                    promotions = promotions,
                    onEdit = { promo ->
                        promoDialogTarget = promo
                        isNewPromo = false
                    },
                    onDelete = { id -> RadioAdminManager.deletePromotion(id) },
                    onToggle = { id, active -> RadioAdminManager.togglePromotion(id, active) }
                )
                1 -> ParticipantsTab(
                    participants = participants,
                    onDelete = { id -> RadioAdminManager.deleteParticipant(id) },
                    onDrawWinner = {
                        if (participants.isNotEmpty()) {
                            drawnWinner = participants.random()
                        } else {
                            Toast.makeText(context, "Nenhum participante inscrito ainda!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenWhatsApp = { phone, name, campaign ->
                        openWhatsAppChat(context, phone, name, campaign)
                    }
                )
                2 -> SponsorBannersTab(
                    banners = banners,
                    onEdit = { banner ->
                        bannerDialogTarget = banner
                        isNewBanner = false
                    },
                    onDelete = { id -> RadioAdminManager.deleteSponsorBanner(id) },
                    onToggle = { id, active -> RadioAdminManager.toggleSponsorBanner(id, active) },
                    onTestLink = { url -> openWebLink(context, url) },
                    onPublishToCarousel = { id ->
                        RadioAdminManager.publishToCarousel(id, true)
                        Toast.makeText(context, "Anúncio publicado com sucesso no Campo de Anúncios!", Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> StationSettingsTab(
                    config = config,
                    onSave = { updated ->
                        RadioAdminManager.updateConfig(updated)
                        Toast.makeText(context, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    onResetDefaults = {
                        RadioAdminManager.resetToDefaults()
                        Toast.makeText(context, "Dados restaurados para o padrão original!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Floating Action Button for adding items on Tabs 0 and 2
        if (selectedTabIndex == 0) {
            FloatingActionButton(
                onClick = {
                    promoDialogTarget = AdminPromotion(
                        tag = "PROMO",
                        title = "",
                        schedule = "Sorteio ao vivo",
                        description = ""
                    )
                    isNewPromo = true
                },
                containerColor = ArmixYellow,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("admin_add_promotion_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Promoção")
            }
        } else if (selectedTabIndex == 2) {
            FloatingActionButton(
                onClick = {
                    bannerDialogTarget = SponsorAdBanner(
                        title = "",
                        sponsorName = "",
                        imageUrl = "",
                        targetPurchaseUrl = "https://radiostudioarmix.com.br",
                        callToAction = "Comprar Agora",
                        priceOrOffer = "Oferta Especial"
                    )
                    isNewBanner = true
                },
                containerColor = ArmixYellow,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("admin_add_banner_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Anúncio")
            }
        }
    }

    // Dialog for Add/Edit Promotion
    promoDialogTarget?.let { promo ->
        PromotionEditDialog(
            promotion = promo,
            isNew = isNewPromo,
            onDismiss = { promoDialogTarget = null },
            onSave = { updated ->
                if (isNewPromo) {
                    RadioAdminManager.addPromotion(updated)
                } else {
                    RadioAdminManager.updatePromotion(updated)
                }
                promoDialogTarget = null
            }
        )
    }

    // Dialog for Add/Edit Sponsor Banner
    bannerDialogTarget?.let { banner ->
        SponsorBannerEditDialog(
            banner = banner,
            isNew = isNewBanner,
            onDismiss = { bannerDialogTarget = null },
            onSave = { updated ->
                if (isNewBanner) {
                    RadioAdminManager.addSponsorBanner(updated)
                } else {
                    RadioAdminManager.updateSponsorBanner(updated)
                }
                bannerDialogTarget = null
            }
        )
    }

    // Winner Dialog (Sorteio)
    drawnWinner?.let { winner ->
        WinnerDrawDialog(
            winner = winner,
            onDismiss = { drawnWinner = null },
            onContactWhatsApp = {
                openWhatsAppChat(context, winner.phone, winner.name, winner.campaignTitle)
                drawnWinner = null
            }
        )
    }
}

// ----------------------------------------------------
// TAB 1: PROMOTIONS LIST
// ----------------------------------------------------
@Composable
private fun PromotionsTab(
    promotions: List<AdminPromotion>,
    onEdit: (AdminPromotion) -> Unit,
    onDelete: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit
) {
    if (promotions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nenhuma promoção cadastrada",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Toque no botão + para criar a primeira",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(promotions, key = { it.id }) { promo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ArmixYellow)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = promo.tag,
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (promo.active) "ATIVA" else "INATIVA",
                                    color = if (promo.active) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Switch(
                                checked = promo.active,
                                onCheckedChange = { onToggle(promo.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = ArmixYellow,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = promo.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = promo.schedule,
                            color = ArmixYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = promo.description,
                            color = TextSecondary,
                            fontSize = 12.5.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = DarkSurfaceBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onEdit(promo) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = ArmixYellow,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(promo.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: PARTICIPANTS LIST & RANDOM DRAW
// ----------------------------------------------------
@Composable
private fun ParticipantsTab(
    participants: List<PromotionParticipant>,
    onDelete: (String) -> Unit,
    onDrawWinner: () -> Unit,
    onOpenWhatsApp: (String, String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Actions Bar: Total Count + "Sortear Ganhador" Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181818))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${participants.size} Ouvinte(s) Inscrito(s)",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cadastros para premiações e sorteios",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onDrawWinner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmixYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sortear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (participants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhum ouvinte inscrito ainda",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quando os ouvintes se cadastrarem na aba Promoções ou na Sequência Suculenta, aparecerão aqui.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(participants, key = { it.id }) { p ->
                    val dateFormatted = remember(p.registeredAt) {
                        try {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            sdf.format(Date(p.registeredAt))
                        } catch (_: Exception) {
                            ""
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                                Text(
                                    text = displayName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = dateFormatted,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            if (p.email.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = p.email,
                                    color = Color(0xFFBBBBBB),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = ArmixYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = p.phone,
                                        color = ArmixYellow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (p.birthDate.isNotBlank() || p.gender.isNotBlank()) {
                                    Text(
                                        text = listOf(p.birthDate, p.gender).filter { it.isNotBlank() }.joinToString(" • "),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF141414))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "🎁 Promoção: ${p.campaignTitle}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (p.onAirProgram.isNotBlank()) {
                                        Text(
                                            text = "📻 Programa No Ar: ${p.onAirProgram}",
                                            color = ArmixYellow,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            if (!p.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Músicas / Resposta: \"${p.notes}\"",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = DarkSurfaceBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onOpenWhatsApp(p.phone, p.name, p.campaignTitle) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF25D366),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "WhatsApp",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { onDelete(p.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir participante",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: SPONSOR ADS & PURCHASE LINKS
// ----------------------------------------------------
@Composable
private fun SponsorBannersTab(
    banners: List<SponsorAdBanner>,
    onEdit: (SponsorAdBanner) -> Unit,
    onDelete: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onTestLink: (String) -> Unit,
    onPublishToCarousel: (String) -> Unit
) {
    if (banners.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nenhum banner de anúncio cadastrado",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Toque no botão + para adicionar um anúncio com link de compra",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(banners, key = { it.id }) { banner ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product image thumbnail
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(banner.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = banner.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .padding(4.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = banner.sponsorName.uppercase(),
                                    color = ArmixYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = banner.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!banner.priceOrOffer.isNullOrBlank()) {
                                    Text(
                                        text = banner.priceOrOffer,
                                        color = Color(0xFF81C784),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (banner.publishedToCarousel) Color(0xFF1B5E20)
                                                else Color(0xFF333333)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (banner.publishedToCarousel) "✓ NO CARROSSEL (${banner.durationSeconds}s)" else "FORA DO CARROSSEL",
                                            color = if (banner.publishedToCarousel) Color(0xFFA5D6A7) else Color.LightGray,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Switch(
                                checked = banner.active,
                                onCheckedChange = { onToggle(banner.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = ArmixYellow,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // URL Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF141414))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = ArmixYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = banner.targetPurchaseUrl,
                                color = Color(0xFFBBBBBB),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DarkSurfaceBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onPublishToCarousel(banner.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (banner.publishedToCarousel) Color(0xFF1B5E20) else ArmixYellow,
                                        contentColor = if (banner.publishedToCarousel) Color.White else Color.Black
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Enviar pro Campo",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { onTestLink(banner.targetPurchaseUrl) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Testar",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onEdit(banner) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = ArmixYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDelete(banner.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: STATION SETTINGS
// ----------------------------------------------------
@Composable
private fun StationSettingsTab(
    config: StationConfig,
    onSave: (StationConfig) -> Unit,
    onResetDefaults: () -> Unit
) {
    var whatsapp by remember(config) { mutableStateOf(config.whatsappNumber) }
    var email by remember(config) { mutableStateOf(config.contactEmail) }
    var website by remember(config) { mutableStateOf(config.websiteUrl) }
    var pin by remember(config) { mutableStateOf(config.adminPin) }
    var isPinVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "CONFIGURAÇÕES DA EMISSORA & SEGURANÇA",
                color = ArmixYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Altere os dados de contato, número de WhatsApp e credenciais do painel.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            // Authorized Admin Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DESTINATÁRIOS AUTORIZADOS DO CÓDIGO PIN",
                        color = ArmixYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "O código PIN de acesso é enviado exclusivamente para:\n• Telefone/WhatsApp: +55 85 99278-8437\n• Email: roberios9.tialpaz@gmail.com",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp de Pedidos de Música") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArmixYellow,
                            focusedLabelColor = ArmixYellow,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email da Rádio") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArmixYellow,
                            focusedLabelColor = ArmixYellow,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Site Oficial da Rádio") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArmixYellow,
                            focusedLabelColor = ArmixYellow,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN Mestre Admin (Oculto)") },
                        placeholder = { Text("••••••") },
                        singleLine = true,
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ArmixYellow)
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
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            onSave(
                                StationConfig(
                                    whatsappNumber = whatsapp,
                                    contactEmail = email,
                                    websiteUrl = website,
                                    adminPin = pin
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArmixYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SALVAR ALTERAÇÕES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221111)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Zona de Restauração",
                        color = Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Restaura todas as promoções, patrocinadores e produtos padrões da emissora.",
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onResetDefaults,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Restaurar Dados Padrão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// DIALOGS: PROMOTION EDIT / ADD
// ----------------------------------------------------
@Composable
private fun PromotionEditDialog(
    promotion: AdminPromotion,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (AdminPromotion) -> Unit
) {
    var tag by remember { mutableStateOf(promotion.tag) }
    var title by remember { mutableStateOf(promotion.title) }
    var schedule by remember { mutableStateOf(promotion.schedule) }
    var description by remember { mutableStateOf(promotion.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Nova Promoção" else "Editar Promoção",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Categoria / Tag (ex: SHOWS, EXCLUSIVO)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Prêmio / Promoção") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Data / Horário do Sorteio") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição Detalhada do Prêmio") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            promotion.copy(
                                tag = tag.ifBlank { "PROMO" },
                                title = title,
                                schedule = schedule.ifBlank { "Sorteio ao vivo" },
                                description = description
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmixYellow,
                    contentColor = Color.Black
                )
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
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

// ----------------------------------------------------
// DIALOGS: SPONSOR BANNER EDIT / ADD
// ----------------------------------------------------
@Composable
private fun SponsorBannerEditDialog(
    banner: SponsorAdBanner,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (SponsorAdBanner) -> Unit
) {
    var title by remember { mutableStateOf(banner.title) }
    var sponsorName by remember { mutableStateOf(banner.sponsorName) }
    var imageUrl by remember { mutableStateOf(banner.imageUrl) }
    var targetUrl by remember { mutableStateOf(banner.targetPurchaseUrl) }
    var callToAction by remember { mutableStateOf(banner.callToAction) }
    var priceOrOffer by remember { mutableStateOf(banner.priceOrOffer ?: "") }
    var durationSecondsText by remember { mutableStateOf(banner.durationSeconds.toString()) }
    var publishedToCarousel by remember { mutableStateOf(banner.publishedToCarousel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Novo Anúncio no Campo" else "Editar Anúncio",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = sponsorName,
                    onValueChange = { sponsorName = it },
                    label = { Text("Nome da Empresa / Patrocinador") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título / Nome do Produto") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL da Imagem do Produto") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("Link de Compra do Produto (URL de Venda)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = callToAction,
                    onValueChange = { callToAction = it },
                    label = { Text("Texto do Botão (ex: Comprar Agora)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceOrOffer,
                    onValueChange = { priceOrOffer = it },
                    label = { Text("Oferta / Preço (ex: Frete Grátis, 50% OFF)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationSecondsText,
                    onValueChange = { durationSecondsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tempo no Carrossel (segundos, ex: 6)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArmixYellow,
                        focusedLabelColor = ArmixYellow,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF181818))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Exibir no Carrossel do Campo",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = publishedToCarousel,
                        onCheckedChange = { publishedToCarousel = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ArmixYellow,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && targetUrl.isNotBlank()) {
                        val duration = durationSecondsText.toIntOrNull()?.coerceIn(3, 60) ?: 6
                        onSave(
                            banner.copy(
                                sponsorName = sponsorName.ifBlank { "Patrocinador Oficial" },
                                title = title,
                                imageUrl = imageUrl.ifBlank { "https://studioarmix.unaux.com/audio/liftdetox123456.png" },
                                targetPurchaseUrl = targetUrl,
                                callToAction = callToAction.ifBlank { "Comprar Agora" },
                                priceOrOffer = priceOrOffer.ifBlank { null },
                                durationSeconds = duration,
                                publishedToCarousel = publishedToCarousel
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmixYellow,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enviar pro Campo do Anúncio", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
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

// ----------------------------------------------------
// DIALOG: WINNER DRAW RESULT
// ----------------------------------------------------
@Composable
private fun WinnerDrawDialog(
    winner: PromotionParticipant,
    onDismiss: () -> Unit,
    onContactWhatsApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ArmixYellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }
        },
        title = {
            Text(
                text = "TEMOS UM GANHADOR!",
                color = ArmixYellow,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = winner.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "WhatsApp: ${winner.phone}",
                    color = ArmixYellow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prêmio / Promoção: ${winner.campaignTitle}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (!winner.notes.isNullOrBlank()) {
                    Text(
                        text = "Anotação: \"${winner.notes}\"",
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContactWhatsApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                )
            ) {
                Text("Notificar no WhatsApp", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = TextSecondary
                )
            ) {
                Text("Fechar")
            }
        },
        containerColor = DarkSurface
    )
}

// ----------------------------------------------------
// UTILS
// ----------------------------------------------------
private fun openWhatsAppChat(context: Context, phone: String, name: String, campaign: String) {
    try {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val msg = "Olá, $name! Você foi sorteado(a) na Rádio Studio Armix na promoção \"$campaign\"! Parabéns!"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Não foi possível abrir o WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun openWebLink(context: Context, url: String) {
    try {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Não foi possível abrir o link", Toast.LENGTH_SHORT).show()
    }
}
