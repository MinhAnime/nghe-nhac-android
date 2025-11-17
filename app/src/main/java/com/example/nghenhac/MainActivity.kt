package com.example.nghenhac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nghenhac.ui.theme.player.BottomPlayerBar
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.NgheNhacTheme
import com.example.nghenhac.ui.theme.auth.AuthScreen
import com.example.nghenhac.ui.theme.home.HomeScreen
import com.example.nghenhac.ui.theme.home.PlaylistDetailScreen
import com.example.nghenhac.ui.theme.player.FullScreenPlayer
import androidx.media3.common.MediaMetadata

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NgheNhacTheme {

                    var startDestination by remember { mutableStateOf("auth") }

                    // Nếu đã đọc xong, hiển thị ứng dụng
                    val navController = rememberNavController()

                    // 3. Khởi tạo ViewModel chung (shared)
                    val playerViewModel: SharedPlayerViewModel = viewModel()
                    val playerState by playerViewModel.playerState.collectAsState()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val isSheetVisible by playerViewModel.isPlayerSheetVisible.collectAsState()
                    val sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )





                    Scaffold(

                        // Gắn BottomBar vào đây
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
                    ) { paddingValues -> // paddingValues chứa độ cao của BottomBar

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            // Áp dụng padding để nội dung không bị Bar che
                            modifier = Modifier.padding(paddingValues)
                        ) {

                            // (Tất cả composable() của bạn giữ nguyên)
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
                                })
                            }

                            composable(
                                route = "playlist/{playlistId}",
                                arguments = listOf(navArgument("playlistId") {
                                    type = NavType.LongType
                                })
                            ) {
                                PlaylistDetailScreen(
                                    sharedPlayerViewModel = playerViewModel
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