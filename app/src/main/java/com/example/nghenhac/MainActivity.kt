package com.example.nghenhac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nghenhac.data.AuthEvents
import com.example.nghenhac.data.TokenHolder
import com.example.nghenhac.data.TokenManager
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import com.example.nghenhac.ui.navigation.Screen
import com.example.nghenhac.ui.theme.NgheNhacTheme
import com.example.nghenhac.ui.theme.auth.AuthScreen
import com.example.nghenhac.ui.theme.components.CreatePlaylistDialog // Import Dialog
import com.example.nghenhac.ui.theme.home.HomeScreen
import com.example.nghenhac.ui.theme.home.HomeViewModel // Import
import com.example.nghenhac.ui.theme.home.PlaylistDetailScreen
import com.example.nghenhac.ui.theme.player.BottomPlayerBar
import com.example.nghenhac.ui.theme.player.FullScreenPlayer
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
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

                // Logic kiểm tra Token (Giữ nguyên)
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
                            startDestination = "auth"
                        } finally {
                            withContext(Dispatchers.Main) { isLoading = false }
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val navController = rememberNavController()
                    val playerViewModel: SharedPlayerViewModel = viewModel()
                    // 1. ĐƯA HOME VIEWMODEL RA NGOÀI ĐỂ MAIN ACTIVITY DÙNG ĐƯỢC
                    val homeViewModel: HomeViewModel = viewModel()

                    val playerState by playerViewModel.playerState.collectAsState()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val isSheetVisible by playerViewModel.isPlayerSheetVisible.collectAsState()
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    // Logic Đăng xuất tự động (Giữ nguyên)
                    LaunchedEffect(Unit) {
                        AuthEvents.logoutFlow.collect {
                            tokenManager.deleteToken()
                            TokenHolder.token = null
                            playerViewModel.pause()
                            navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            // Chỉ hiện NavigationBar khi không ở màn hình Auth
                            val isAuthScreen = currentDestination?.route == "auth"

                            if (!isAuthScreen) {
                                Column {
                                    // A. THANH PHÁT NHẠC (Nằm trên)
                                    if (playerState.currentMediaId.isNotBlank() && !isSheetVisible) {
                                        BottomPlayerBar(
                                            playerState = playerState,
                                            onPlayPauseClicked = {
                                                if (playerState.isPlaying) playerViewModel.pause()
                                                else playerViewModel.play()
                                            },
                                            onBarClicked = { playerViewModel.openPlayerSheet() }
                                        )
                                    }

                                    // B. THANH ĐIỀU HƯỚNG 4 MỤC (Nằm dưới)
                                    NavigationBar {
                                        val items = listOf(
                                            Screen.Home,
                                            Screen.Search,
                                            Screen.AddPlaylist,
                                            Screen.Logout
                                        )

                                        items.forEach { screen ->
                                            NavigationBarItem(
                                                icon = { Icon(screen.icon, contentDescription = null) },
                                                label = { Text(screen.title) },
                                                // Chỉ Home và Search mới có trạng thái "được chọn"
                                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                                onClick = {
                                                    when (screen) {
                                                        Screen.Home, Screen.Search -> {
                                                            navController.navigate(screen.route) {
                                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        }
                                                        Screen.AddPlaylist -> {
                                                            // Mở Dialog tạo playlist
                                                            homeViewModel.openCreateDialog()
                                                        }
                                                        Screen.Logout -> {
                                                            // Xử lý đăng xuất
                                                            lifecycleScope.launch(Dispatchers.IO) {
                                                                tokenManager.deleteToken()
                                                                TokenHolder.token = null
                                                                withContext(Dispatchers.Main) {
                                                                    playerViewModel.pause()
                                                                    navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                                                                }
                                                            }
                                                        }
                                                        else -> {}
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            // Auth
                            composable("auth") {
                                AuthScreen(onLoginSuccess = {
                                    navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                })
                            }

                            // Home
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    playerViewModel = playerViewModel,
                                    // Truyền homeViewModel đã tạo ở ngoài vào
                                    homeViewModel = homeViewModel,
                                    onPlaylistClick = { playlist ->
                                        navController.navigate("playlist/${playlist.id}")
                                    }
                                )
                            }

                            // Search
                            composable(Screen.Search.route) {
                                com.example.nghenhac.ui.theme.search.SearchScreen(
                                    playerViewModel = playerViewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // Playlist Detail
                            composable(
                                route = "playlist/{playlistId}",
                                arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                            ) {
                                PlaylistDetailScreen(
                                    sharedPlayerViewModel = playerViewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    // 2. HIỂN THỊ DIALOG TẠO PLAYLIST (Ở MainActivity)
                    if (homeViewModel.isCreatePlaylistDialogOpen) {
                        CreatePlaylistDialog(
                            onDismiss = { homeViewModel.closeCreateDialog() },
                            onConfirm = { name -> homeViewModel.createPlaylist(name) }
                        )
                    }

                    // Full Screen Player
                    if (isSheetVisible) {
                        ModalBottomSheet(
                            onDismissRequest = { playerViewModel.closePlayerSheet() },
                            sheetState = sheetState,
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = { WindowInsets(0.dp) }
                        ) {
                            FullScreenPlayer(
                                playerState = playerState,
                                onClose = { playerViewModel.closePlayerSheet() },
                                onPlayPauseClicked = {
                                    if (playerState.isPlaying) playerViewModel.pause()
                                    else playerViewModel.play()
                                },
                                onNextClicked = { playerViewModel.next() },
                                onPreviousClicked = { playerViewModel.previous() },
                                onShuffleClicked = { playerViewModel.toggleShuffle() },
                                onRepeatClicked = { playerViewModel.toggleRepeatMode() },
                                onSeek = { positionFraction -> playerViewModel.seekTo(positionFraction) }
                            )
                        }
                    }
                }
            }
        }
    }
}