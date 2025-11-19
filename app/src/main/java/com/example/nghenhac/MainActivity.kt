package com.example.nghenhac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nghenhac.data.AuthEvents // <-- 1. IMPORT MỚI
import com.example.nghenhac.data.TokenHolder
import com.example.nghenhac.data.TokenManager
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import com.example.nghenhac.ui.theme.NgheNhacTheme
import com.example.nghenhac.ui.theme.auth.AuthScreen
import com.example.nghenhac.ui.theme.home.HomeScreen
import com.example.nghenhac.ui.theme.home.PlaylistDetailScreen
import com.example.nghenhac.ui.theme.player.BottomPlayerBar
import com.example.nghenhac.ui.theme.player.FullScreenPlayer
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.search.SearchScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NgheNhacTheme {

                var isLoading by remember { mutableStateOf(true) }
                var startDestination by remember { mutableStateOf("auth") }
                val tokenManager = TokenManager(applicationContext)

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        try {
                            val token = withTimeoutOrNull(2000L) {
                                tokenManager.getTokenFlow().first()
                            }

                            if (token.isNullOrBlank()) {
                                startDestination = "auth"
                            } else {
                                TokenHolder.token = token
                                val apiService = RetrofitClient.create(applicationContext)
                                val repo = HomeRepository(apiService)
                                try {
                                    repo.getMyPlaylists(page = 0)
                                    startDestination = "home"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    TokenHolder.token = null
                                    startDestination = "auth"
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            startDestination = "auth"
                        } finally {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val navController = rememberNavController()
                    val playerViewModel: SharedPlayerViewModel = viewModel()
                    val playerState by playerViewModel.playerState.collectAsState()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val isSheetVisible by playerViewModel.isPlayerSheetVisible.collectAsState()
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    // --- 2. LẮNG NGHE SỰ KIỆN ĐĂNG XUẤT (Từ Interceptor) ---
                    LaunchedEffect(Unit) {
                        AuthEvents.logoutFlow.collect {
                            tokenManager.deleteToken()
                            TokenHolder.token = null
                            playerViewModel.pause() // Dừng nhạc

                            // Chuyển về màn hình Auth và xóa lịch sử
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    // -------------------------------------------------------

                    Scaffold(
                        bottomBar = {
                            val showInCurrentScreen = (currentRoute != "auth")
                            val hasRealSong = playerState.currentMediaId.isNotBlank()
                            val sheetIsClosed = !isSheetVisible

                            if (showInCurrentScreen && hasRealSong && sheetIsClosed) {
                                BottomPlayerBar(
                                    playerState = playerState,
                                    onPlayPauseClicked = {
                                        if (playerState.isPlaying) playerViewModel.pause()
                                        else playerViewModel.play()
                                    },
                                    onBarClicked = {
                                        playerViewModel.openPlayerSheet()
                                    }
                                )
                            }
                        }
                    ) { paddingValues ->

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("auth") {
                                AuthScreen(onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                })
                            }

                            composable("home") {
                                HomeScreen(
                                    playerViewModel = playerViewModel,
                                    onPlaylistClick = { playlist ->
                                        navController.navigate("playlist/${playlist.id}")
                                    },
                                    // --- 3.TRUYỀN HÀM ĐĂNG XUẤT ---
                                    onLogoutClick = {
                                        lifecycleScope.launch {
                                            tokenManager.deleteToken()
                                            TokenHolder.token = null
                                            withContext(Dispatchers.Main) {
                                                playerViewModel.pause()
                                                navController.navigate("auth") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onSearchClick = {
                                        navController.navigate("search")
                                    }
                                )
                            }
                            composable("search") {
                                SearchScreen(
                                    playerViewModel = playerViewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "playlist/{playlistId}",
                                arguments = listOf(navArgument("playlistId") {
                                    type = NavType.LongType
                                })
                            ) {
                                PlaylistDetailScreen(
                                    sharedPlayerViewModel = playerViewModel,
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }

                    if (isSheetVisible) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                playerViewModel.closePlayerSheet()
                            },
                            sheetState = sheetState,
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = { WindowInsets(0.dp) }
                        ) {
                            FullScreenPlayer(
                                playerState = playerState,
                                onClose = {
                                    playerViewModel.closePlayerSheet()
                                },
                                onPlayPauseClicked = {
                                    if (playerState.isPlaying) playerViewModel.pause()
                                    else playerViewModel.play()
                                },
                                onNextClicked = { playerViewModel.next() },
                                onPreviousClicked = { playerViewModel.previous() },
                                onShuffleClicked = { playerViewModel.toggleShuffle() },
                                onRepeatClicked = { playerViewModel.toggleRepeatMode() },
                                onSeek = { positionFraction ->
                                    playerViewModel.seekTo(positionFraction)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}