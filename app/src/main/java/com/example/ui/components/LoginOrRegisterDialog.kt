package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auth.UserManager
import com.example.ui.theme.ArmixYellow
import com.example.ui.theme.RadioRedPrimary

enum class AuthRequiredReason(val title: String, val message: String) {
    VOTE(
        title = "Login Obrigatório para Votar",
        message = "Para registrar o seu voto no ranking e na parada musical do 6º dia, você precisa se cadastrar e entrar com sua conta."
    ),
    MUSIC_REQUEST(
        title = "Login Obrigatório para Pedir Música",
        message = "Para pedir sua música favorita e ter seu nome anunciado no ar pelo comunicador, faça login ou cadastre-se."
    ),
    PROMOTION(
        title = "Login Obrigatório para Prêmios",
        message = "O cadastro e login são obrigatórios para validar sua participação nos sorteios, brindes e premiações da Studio Armix."
    )
}

/**
 * Modal inteligente de Login e Acesso Rápido para garantir que o ouvinte
 * se cadastre e entre com seu login antes de votar, pedir música ou participar de promoções.
 */
@Composable
fun LoginOrRegisterDialog(
    reason: AuthRequiredReason = AuthRequiredReason.VOTE,
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ArmixYellow.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .testTag("auth_required_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ArmixYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (reason) {
                                    AuthRequiredReason.VOTE -> Icons.Default.HowToVote
                                    AuthRequiredReason.MUSIC_REQUEST -> Icons.Default.MusicNote
                                    AuthRequiredReason.PROMOTION -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = Color(0xFF141419),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "IDENTIFICAÇÃO",
                            color = ArmixYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Description of requirement
                Text(
                    text = reason.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reason.message,
                    color = Color(0xFFBBBBCC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message if any
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF381F21))
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Email Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("E-mail cadastrado", fontSize = 12.sp) },
                    placeholder = { Text("seuemail@gmail.com", color = Color(0xFF666677), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = ArmixYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ArmixYellow,
                        unfocusedBorderColor = Color(0xFF333344),
                        focusedLabelColor = ArmixYellow,
                        unfocusedLabelColor = Color(0xFFAAAAAA),
                        focusedContainerColor = Color(0xFF1B1B22),
                        unfocusedContainerColor = Color(0xFF181820)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Senha", fontSize = 12.sp) },
                    placeholder = { Text("••••••••", color = Color(0xFF666677), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ArmixYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                                tint = Color(0xFF888899),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ArmixYellow,
                        unfocusedBorderColor = Color(0xFF333344),
                        focusedLabelColor = ArmixYellow,
                        unfocusedLabelColor = Color(0xFFAAAAAA),
                        focusedContainerColor = Color(0xFF1B1B22),
                        unfocusedContainerColor = Color(0xFF181820)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button: ENTRAR
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Por favor, digite seu e-mail cadastrado."
                            return@Button
                        }
                        if (password.isBlank()) {
                            errorMessage = "Por favor, digite sua senha de acesso."
                            return@Button
                        }

                        val success = UserManager.login(email.trim(), password)
                        if (success) {
                            Toast.makeText(context, "Login realizado com sucesso! Bem-vindo!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            errorMessage = "E-mail ou senha incorretos."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArmixYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_auth_login")
                ) {
                    Text(
                        text = "ENTRAR COM MEU LOGIN",
                        color = Color(0xFF141419),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Divider / OR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF2C2C3A)))
                    Text(
                        text = " OU ",
                        color = Color(0xFF888899),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF2C2C3A)))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button: CADASTRAR-SE
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToRegistration()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RadioRedPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_auth_register")
                ) {
                    Text(
                        text = "NÃO TEM CONTA? CADASTRAR-SE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel Link
                Text(
                    text = "Continuar ouvindo sem logar",
                    color = Color(0xFF888899),
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(6.dp)
                )
            }
        }
    }
}
