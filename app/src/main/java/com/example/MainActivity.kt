package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainContainerScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AnnesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AnnesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> systemInDark
            }

            MyApplicationTheme(isDarkTheme = isDarkTheme) {
                val isSplashDone by viewModel.isSplashDone.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val baseUrl by viewModel.baseUrl.collectAsState()
                val isServerOnline by viewModel.isServerConnected.collectAsState()
                val savedLoginInfo by viewModel.savedLogin.collectAsState()
                val registeredUsers by viewModel.registeredUsers.collectAsState()
                val supabaseUrl by viewModel.supabaseUrl.collectAsState()
                val supabaseAnonKey by viewModel.supabaseAnonKey.collectAsState()
                val supabaseServiceKey by viewModel.supabaseServiceKey.collectAsState()

                var showLoginServerConfig by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = when {
                            !isSplashDone -> "splash"
                            currentUser == null -> "login"
                            else -> "main"
                        },
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "screen_navigation"
                    ) { targetScreen ->
                        when (targetScreen) {
                            "splash" -> {
                                SplashScreen(
                                    onSplashFinished = { viewModel.setSplashFinished() }
                                )
                            }
                            "login" -> {
                                LoginScreen(
                                    savedLoginInfo = savedLoginInfo,
                                    registeredUsers = registeredUsers,
                                    onLoginClick = { email, password, rememberCreds, callback ->
                                        viewModel.login(email, password, rememberCreds, callback)
                                    },
                                    onBiometricLoginClick = { callback ->
                                        viewModel.loginWithBiometric(callback)
                                    },
                                    onRegisterUserClick = { request, callback ->
                                        viewModel.registerUser(request, callback)
                                    },
                                    onConfigureServerClick = { showLoginServerConfig = true },
                                    onToggleThemeClick = { viewModel.toggleThemeMode() },
                                    isDarkTheme = isDarkTheme,
                                    isLoading = isLoading,
                                    currentServerUrl = baseUrl
                                )
                            }
                            else -> {
                                MainContainerScreen(viewModel = viewModel)
                            }
                        }
                    }

                    if (showLoginServerConfig) {
                        SettingsBottomSheet(
                            currentUser = null,
                            currentBaseUrl = baseUrl,
                            isServerOnline = isServerOnline,
                            themeMode = themeMode,
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            supabaseUrl = supabaseUrl,
                            supabaseAnonKey = supabaseAnonKey,
                            supabaseServiceKey = supabaseServiceKey,
                            onUpdateSupabaseCredentials = { url, anon, serviceKey ->
                                viewModel.updateSupabaseCredentials(url, anon, serviceKey)
                            },
                            onUpdateBaseUrl = { viewModel.updateServerUrl(it) },
                            onPingServer = { viewModel.pingServer() },
                            onSwitchRole = { viewModel.switchRole(it) },
                            onLogout = { },
                            onDismiss = { showLoginServerConfig = false }
                        )
                    }
                }
            }
        }
    }
}


