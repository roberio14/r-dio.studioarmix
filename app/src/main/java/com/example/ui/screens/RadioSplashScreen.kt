package com.example.ui.screens

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ArmixYellow
import kotlinx.coroutines.delay

/**
 * Splash Screen oficial da Studio Armix:
 * - Fundo Amarelo radiante oficial (substituindo o vermelho do vídeo de referência).
 * - Animação de impacto no logotipo oficial da Studio Armix (escala com efeito mola + fade in).
 * - Ondas de choque sonoras concêntricas expandindo a partir do centro no momento do impacto.
 * - Áudio da vinheta original em alta fidelidade sincronizado exatamente com o surgimento do logotipo.
 * - Subtítulo "GUAIÚBA - CE" em branco, negrito espaçado, exatamente na mesma diagramação do vídeo de referência ("BELO HORIZONTE - 99,1").
 * - Transição suave e automática para o aplicativo ao término do áudio (com suporte a toque para pular).
 */
@Composable
fun RadioSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Animação de impacto do logotipo (escala e transparência)
    val logoScale = remember { Animatable(0.35f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    // Onda de choque / impacto sonora se expandindo ao redor da logo
    val shockwaveProgress = remember { Animatable(0f) }

    // Pulso suave pós-impacto enquanto o áudio termina
    val infiniteTransition = rememberInfiniteTransition(label = "post_impact_pulse")
    val subtleBreathing by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtle_breathing"
    )

    // MediaPlayer para reprodução em alta fidelidade da vinheta oficial do vídeo
    val mediaPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.splash_jingle)?.apply {
                setOnCompletionListener {
                    onSplashFinished()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer.stop()
                }
                mediaPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    // Disparo sincronizado: Áudio + Animação de Impacto
    LaunchedEffect(Unit) {
        // Inicia o áudio imediatamente no instante do impacto
        try {
            mediaPlayer?.start()
        } catch (_: Exception) {}

        // Dispara a onda de choque e a entrada de impacto da logo em paralelo
        shockwaveProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Logo surge com punch e efeito elástico/mola de impacto
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = 0.58f,
                stiffness = Spring.StiffnessMedium
            )
        )
        // Subtítulo entra em seguida com máxima nitidez
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )

        // Duração total sincronizada com o áudio (~2.8s) com fallback de segurança
        delay(2850)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFC107), // Amarelo dourado vivo no topo
                        ArmixYellow,       // Amarelo oficial da Studio Armix no centro (#F5A603)
                        Color(0xFFE69500)  // Base sólida amarela
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Toque na tela para abrir imediatamente
                try {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer.stop()
                    }
                } catch (_: Exception) {}
                onSplashFinished()
            }
            .testTag("radio_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ondas sonoras de impacto concêntricas (brancas e douradas translúcidas)
        if (shockwaveProgress.value > 0f && shockwaveProgress.value < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val p = shockwaveProgress.value
                val maxRadius = size.width * 0.75f

                // Anel de choque primário
                val radius1 = 70.dp.toPx() + (maxRadius - 70.dp.toPx()) * p
                val alpha1 = ((1f - p) * 0.75f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha1),
                    radius = radius1,
                    center = centerOffset,
                    style = Stroke(width = (6.dp * (1f - p * 0.5f)).toPx())
                )

                // Anel de choque secundário
                if (p > 0.2f) {
                    val p2 = ((p - 0.2f) / 0.8f).coerceIn(0f, 1f)
                    val radius2 = 70.dp.toPx() + (maxRadius - 70.dp.toPx()) * p2
                    val alpha2 = ((1f - p2) * 0.5f).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha2),
                        radius = radius2,
                        center = centerOffset,
                        style = Stroke(width = (4.dp * (1f - p2 * 0.5f)).toPx())
                    )
                }
            }
        }

        // Conteúdo central: Logotipo Studio Armix + Subtítulo "GUAIÚBA - CE"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(logoScale.value * if (logoScale.value >= 0.99f) subtleBreathing else 1f)
                .alpha(logoAlpha.value)
                .padding(24.dp)
        ) {
            // Logotipo oficial da Studio Armix
            Image(
                painter = painterResource(id = R.drawable.studio_armix_splash_logo),
                contentDescription = "Rádio Studio Armix",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Subtítulo em branco e negrito espaçado, na mesma diagramação de "BELO HORIZONTE - 99,1"
            Text(
                text = "GUAIÚBA - CE",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(4.dp))
            )
        }

        // Indicador discreto para pular toque no canto superior
        Text(
            text = "Pular ›",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        )
    }
}
