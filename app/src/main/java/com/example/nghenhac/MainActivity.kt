package com.example.nghenhac

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nghenhac.ui.theme.NgheNhacTheme
import com.google.common.util.concurrent.MoreExecutors
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nghenhac.ui.theme.auth.AuthScreen
import com.example.nghenhac.ui.theme.home.HomeScreen
import com.example.nghenhac.ui.theme.home.PlaylistDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NgheNhacTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "auth"
                ) {
                    composable("auth") {
                        AuthScreen(
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            onPlaylistClick = { playlistSummary ->
                                navController.navigate("playlist/${playlistSummary.id}")
                            }
                        )
                    }
                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(navArgument("playlistId") {
                            type = NavType.LongType // Định nghĩa kiểu của tham số
                        })
                    ) {
                        PlaylistDetailScreen()
                    }
                }
            }
        }
    }
}



@Composable
fun PlaybackTestScreen(songUrl: String) {
    val context = LocalContext.current

    // Trạng thái MediaController
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var controllerState by remember { mutableStateOf("Đang kết nối...") }

    // Kết nối MediaController khi Composable resumed
    LifecycleResumeEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                controllerState = "Bấm play để phát"
            } catch (e: Exception) {
                controllerState = "Kết nối thất bại: ${e.message}"
            }
        }, MoreExecutors.directExecutor())

        onPauseOrDispose {
            mediaController?.release()
            mediaController = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = controllerState)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mediaController?.let { controller ->
                        if (isPlaying){
                            controller.pause()
                            isPlaying = false
                        } else {
                            val mediaItem = MediaItem.fromUri(songUrl)
                            controller.setMediaItem(mediaItem)
                            controller.play()
                            isPlaying = true
                        }
                    }
                },
                enabled = mediaController != null
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
}

