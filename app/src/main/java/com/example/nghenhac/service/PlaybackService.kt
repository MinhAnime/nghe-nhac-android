package com.example.nghenhac.services

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.nghenhac.data.PlayerEvents
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer

    companion object {
        lateinit var repository: HomeRepository
            private set
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val apiService = RetrofitClient.create(applicationContext)
        repository = HomeRepository(apiService)

        // 1. Tạo ExoPlayer gốc
        exoPlayer = ExoPlayer.Builder(this).build()

        // 2. Tạo "Lớp vỏ" (Wrapper) để tùy chỉnh lệnh
        val customPlayer = object : ForwardingPlayer(exoPlayer) {

            // Luôn báo là CÓ lệnh Next/Previous
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .build()
            }

            // Luôn báo là lệnh khả dụng
            override fun isCommandAvailable(command: Int): Boolean {
                return command == Player.COMMAND_SEEK_TO_NEXT ||
                        command == Player.COMMAND_SEEK_TO_PREVIOUS ||
                        super.isCommandAvailable(command)
            }

            // Khi bấm Next trên Notification -> Gửi tín hiệu
            override fun seekToNext() {
                serviceScope.launch { PlayerEvents.emitEvent(PlayerEvents.Event.Next) }
            }

            // Khi bấm Previous trên Notification -> Gửi tín hiệu
            override fun seekToPrevious() {
                serviceScope.launch { PlayerEvents.emitEvent(PlayerEvents.Event.Previous) }
            }
        }

        // 3. Đưa "Lớp vỏ" (customPlayer) vào MediaSession
        // Thay vì đưa exoPlayer trực tiếp
        mediaSession = MediaSession.Builder(this, customPlayer).build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}