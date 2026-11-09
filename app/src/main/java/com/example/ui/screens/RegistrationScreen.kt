package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.UserManager
import com.example.ui.components.ArmixYellow
import com.example.ui.components.JpRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Tela de Cadastro de Ouvinte fiel ao Print 2 da referência.
 * Permite cadastrar dados completos para concorrer a todas as premiações da Rádio Studio Armix.
 */
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currentUser by UserManager.currentUser.collectAsState()

    var nome by remember { mutableStateOf("") }
    var sobrenome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dddCelular by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("Masculino") }
    var senha by remember { mutableStateOf("") }
    var confirmacaoSenha by remember { mutableStateOf("") }
    var termosAceitos by remember { mutableStateOf(false) }

    var isSenhaVisible by remember { mutableStateOf(false) }
    var isConfirmacaoVisible by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Red / Gold fiber wave background from screenshot reference
        Image(
            painter = painterResource(id = R.drawable.red_fiber_waves_1793787358802),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay to keep text ultra crisp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // --- TOP BAR matching Print 2 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JpRed.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("reg_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                // Logo Station Center
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.radio_station_logo),
                        contentDescription = "Studio Armix Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "STUDIO ARMIX",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "GUAIÚBA - CE",
                            color = ArmixYellow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Contact quick actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+5585992788437"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Telefone: (85) 99278-8437", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Telefone da Rádio",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:contato@radiostudioarmix.com.br"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Email: contato@radiostudioarmix.com.br", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email da Rádio",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // --- FORM CONTENT (Scrollable, identical to Print 2) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLoginMode) {
                    // --- MODO LOGIN ---
                    Text(
                        text = "Acesse sua Conta",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Entre com seu e-mail e senha cadastrados para participar dos sorteios.",
                        color = Color(0xFFDDDDDD),
                        fontSize = 12.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FormLabel("Email:")
                    FormWhiteInput(
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email,
                        testTag = "login_field_email"
                    )

                    FormLabel("Senha:")
                    FormWhiteInput(
                        value = senha,
                        onValueChange = { senha = it },
                        isPassword = true,
                        isPasswordVisible = isSenhaVisible,
                        onTogglePassword = { isSenhaVisible = !isSenhaVisible },
                        testTag = "login_field_password"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (email.isNotBlank() && senha.isNotBlank()) {
                                UserManager.login(email, senha)
                                email = ""
                                senha = ""
                                Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                onRegistrationSuccess()
                            } else {
                                Toast.makeText(context, "Preencha e-mail e senha para entrar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JpRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_submit_login")
                    ) {
                        Text(
                            text = "Entrar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { isLoginMode = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Ainda não tem cadastro? Clique aqui para Cadastrar",
                            color = ArmixYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // --- MODO CADASTRO (Print 2 fiel) ---
                    // 1. Nome
                    FormLabel("Nome:")
                    FormWhiteInput(
                        value = nome,
                        onValueChange = { nome = it },
                        placeholderText = "Seu nome",
                        testTag = "reg_field_nome"
                    )

                    // 2. Sobrenome
                    FormLabel("Sobrenome:")
                    FormWhiteInput(
                        value = sobrenome,
                        onValueChange = { sobrenome = it },
                        placeholderText = "Seu sobrenome",
                        testTag = "reg_field_sobrenome"
                    )

                    // 3. Email
                    FormLabel("Email:")
                    FormWhiteInput(
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email,
                        placeholderText = "seuemail@exemplo.com",
                        testTag = "reg_field_email"
                    )

                    // 4. DDD + Celular
                    FormLabel("DDD + Celular:")
                    FormWhiteInput(
                        value = dddCelular,
                        onValueChange = { dddCelular = it },
                        keyboardType = KeyboardType.Phone,
                        placeholderText = "(85) 99999-9999",
                        testTag = "reg_field_phone"
                    )

                    // 5. Data de nascimento
                    FormLabel("Data de nascimento:")
                    FormWhiteInput(
                        value = dataNascimento,
                        onValueChange = { dataNascimento = it },
                        keyboardType = KeyboardType.Number,
                        placeholderText = "DD/MM/AAAA",
                        testTag = "reg_field_birthdate"
                    )

                    // 6. Gênero
                    FormLabel("Gênero:")
                    GenderOptionItem(
                        title = "Masculino",
                        isSelected = genero == "Masculino",
                        onSelect = { genero = "Masculino" }
                    )
                    GenderOptionItem(
                        title = "Feminino",
                        isSelected = genero == "Feminino",
                        onSelect = { genero = "Feminino" }
                    )
                    GenderOptionItem(
                        title = "Prefiro não informar",
                        isSelected = genero == "Prefiro não informar",
                        onSelect = { genero = "Prefiro não informar" }
                    )

                    // 7. Senha
                    FormLabel("Senha:")
                    FormWhiteInput(
                        value = senha,
                        onValueChange = { senha = it },
                        isPassword = true,
                        isPasswordVisible = isSenhaVisible,
                        onTogglePassword = { isSenhaVisible = !isSenhaVisible },
                        placeholderText = "••••••••",
                        testTag = "reg_field_password"
                    )

                    // 8. Confirmação da Senha
                    FormLabel("Confirmação da Senha:")
                    FormWhiteInput(
                        value = confirmacaoSenha,
                        onValueChange = { confirmacaoSenha = it },
                        isPassword = true,
                        isPasswordVisible = isConfirmacaoVisible,
                        onTogglePassword = { isConfirmacaoVisible = !isConfirmacaoVisible },
                        placeholderText = "••••••••",
                        testTag = "reg_field_confirm_password"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 9. Checkbox Termos de Uso
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termosAceitos = !termosAceitos },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termosAceitos,
                            onCheckedChange = { termosAceitos = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = JpRed,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.testTag("reg_checkbox_terms")
                        )
                        Text(
                            text = "Declaro que li e estou de acordo com os ",
                            color = Color.White,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Termos de Uso",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { showTermsDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 10. Botão Cadastrar (Estilo Print 2)
                    Button(
                        onClick = {
                            if (nome.isBlank()) {
                                Toast.makeText(context, "Por favor, digite seu Nome", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (dddCelular.isBlank()) {
                                Toast.makeText(context, "Por favor, informe seu DDD + Celular", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (email.isBlank()) {
                                Toast.makeText(context, "Por favor, informe seu E-mail", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (senha.length < 4) {
                                Toast.makeText(context, "A senha deve ter pelo menos 4 caracteres", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (senha != confirmacaoSenha) {
                                Toast.makeText(context, "A confirmação da senha não confere", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!termosAceitos) {
                                Toast.makeText(context, "Você precisa aceitar os Termos de Uso para continuar", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Cadastra e salva no UserManager
                            UserManager.register(
                                firstName = nome,
                                lastName = sobrenome,
                                email = email,
                                phone = dddCelular,
                                birthDate = dataNascimento,
                                gender = genero,
                                password = senha
                            )

                            // Limpa e apaga imediatamente todas as informações da tela para não ficarem expostas
                            nome = ""
                            sobrenome = ""
                            email = ""
                            dddCelular = ""
                            dataNascimento = ""
                            genero = "Masculino"
                            senha = ""
                            confirmacaoSenha = ""
                            termosAceitos = false

                            Toast.makeText(
                                context,
                                "Cadastro realizado com sucesso! Você já pode concorrer aos prêmios!",
                                Toast.LENGTH_LONG
                            ).show()

                            onRegistrationSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JpRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_submit_cadastrar")
                    ) {
                        Text(
                            text = "Cadastrar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    TextButton(
                        onClick = { isLoginMode = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Já possui cadastro? Clique aqui para Entrar",
                            color = ArmixYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal de Termos de Uso
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Termos de Uso e Participação",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. Elegibilidade: As premiações e sorteios são abertos para todos os ouvintes da Rádio Studio Armix residentes no Brasil.",
                        color = Color(0xFFDDDDDD),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "2. Cadastro Único: Cada participante deve fornecer dados reais (Nome, WhatsApp, E-mail e Data de Nascimento). Cadastros duplicados serão unificados.",
                        color = Color(0xFFDDDDDD),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "3. Sorteios Ao Vivo: Os prêmios são sorteados ao vivo de acordo com a programação oficial da rádio. O ouvinte ganhador será contatado via WhatsApp ou ligação.",
                        color = Color(0xFFDDDDDD),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "4. Privacidade: Seus dados cadastrais ficam armazenados com segurança pela emissora para validação e auditoria no Painel Admin.",
                        color = Color(0xFFDDDDDD),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        termosAceitos = true
                        showTermsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JpRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Aceitar Termos", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showTermsDialog = false },
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
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun FormWhiteInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    placeholderText: String = "",
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = if (placeholderText.isNotBlank()) {
            {
                Text(
                    text = placeholderText,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.5.sp
                )
            }
        } else null,
        textStyle = TextStyle(
            color = Color(0xFF1A1A1A),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        ),
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Ocultar senha" else "Ver senha",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            cursorColor = Color(0xFFB71C1C)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    )
}

@Composable
private fun GenderOptionItem(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selecionado",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
