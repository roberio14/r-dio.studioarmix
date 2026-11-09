package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.player.PlaybackState
import com.example.player.RadioPlayerManager
import com.example.ui.components.BottomRadioPlayer
import com.example.ui.components.ContactDialog
import com.example.ui.components.QuickActionsRow
import com.example.ui.components.RadioScreen
import com.example.ui.components.TopRadioBar
import com.example.admin.RadioAdminManager
import com.example.auth.UserManager
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HourlyPrizeScreen
import com.example.ui.screens.MusicRequestScreen
import com.example.ui.screens.NavigationDrawerContent
import com.example.ui.screens.PromotionsScreen
import com.example.ui.screens.RadioSplashScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SleepTimerScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UserManager.init(this)
        RadioAdminManager.init(this)
        RadioPlayerManager.initRegisteredRequests(this)
        com.example.player.WeeklyVotingManager.init(this)

        setContent {
            MyApplicationTheme {
                RequestPermissions()
                var showSplashScreen by rememberSaveable { mutableStateOf(true) }
                AnimatedContent(
                    targetState = showSplashScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    },
                    label = "splash_smooth_transition"
                ) { isSplash ->
                    if (isSplash) {
                        RadioSplashScreen(
                            onSplashFinished = { showSplashScreen = false }
                        )
                    } else {
                        RadioAppMain()
                    }
                }
            }
        }
    }

    @Composable
    private fun RequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Permission result handled */ }

            LaunchedEffect(Unit) {
                if (!permissionGranted) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun RadioAppMain() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(RadioScreen.HOME) }
    var showContactDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val playbackState by RadioPlayerManager.playbackState.collectAsState()

    // Show error toast or snackbar when player has an issue
    LaunchedEffect(playbackState) {
        if (playbackState is PlaybackState.Error) {
            val err = (playbackState as PlaybackState.Error).message
            snackbarHostState.showSnackbar(err)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBackground,
                drawerContentColor = Color.White
            ) {
                NavigationDrawerContent(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                    },
                    onOpenLogin = {
                        currentScreen = RadioScreen.REGISTRATION
                        scope.launch { drawerState.close() }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        val isRegistrationScreen = currentScreen == RadioScreen.REGISTRATION

        Scaffold(
            topBar = {
                if (!isRegistrationScreen) {
                    Column {
                        TopRadioBar(
                            onOpenMenu = {
                                scope.launch { drawerState.open() }
                            },
                            onPhoneClick = {
                                showContactDialog = true
                            },
                            onEmailClick = {
                                showContactDialog = true
                            }
                        )
                        QuickActionsRow(
                            currentScreen = currentScreen,
                            onSelectScreen = { screen ->
                                currentScreen = screen
                            }
                        )
                    }
                }
            },
            bottomBar = {
                if (!isRegistrationScreen) {
                    BottomRadioPlayer(
                        onProgramClick = {
                            currentScreen = RadioScreen.MUSIC_REQUEST
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            containerColor = DarkBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isRegistrationScreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
            ) {
                when (currentScreen) {
                    RadioScreen.HOME -> HomeScreen(
                        onNavigateToRegistration = { currentScreen = RadioScreen.REGISTRATION }
                    )
                    RadioScreen.MUSIC_REQUEST -> MusicRequestScreen(
                        onNavigateBack = { currentScreen = RadioScreen.HOME },
                        onNavigateToRegistration = { currentScreen = RadioScreen.REGISTRATION }
                    )
                    RadioScreen.PROMOTIONS -> PromotionsScreen(
                        onNavigateBack = { currentScreen = RadioScreen.HOME },
                        onNavigateToRegistration = { currentScreen = RadioScreen.REGISTRATION }
                    )
                    RadioScreen.HOURLY_PRIZE -> HourlyPrizeScreen(
                        onNavigateBack = { currentScreen = RadioScreen.HOME },
                        onNavigateToRegistration = { currentScreen = RadioScreen.REGISTRATION }
                    )
                    RadioScreen.SLEEP_TIMER -> SleepTimerScreen(
                        onNavigateBack = { currentScreen = RadioScreen.HOME }
                    )
                    RadioScreen.ADMIN_PANEL -> AdminDashboardScreen(
                        onNavigateBack = { currentScreen = RadioScreen.HOME }
                    )
                    RadioScreen.REGISTRATION -> RegistrationScreen(
                        onNavigateBack = { currentScreen = RadioScreen.HOME },
                        onRegistrationSuccess = { currentScreen = RadioScreen.PROMOTIONS }
                    )
                }
            }
        }
    }

    if (showContactDialog) {
        ContactDialog(onDismiss = { showContactDialog = false })
    }
}
