package com.example.nghenhac.ui.theme.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.example.nghenhac.data.PlayerEvents
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import com.example.nghenhac.services.PlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PlayerState(
    val nowPlaying: MediaMetadata? = null,
    val isPlaying: Boolean = false,
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val currentMediaId: String = "",
    val dominantColor: Color? = null
)
class SharedPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var mediaController: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _isPlayerSheetVisible = MutableStateFlow(false)
    val isPlayerSheetVisible = _isPlayerSheetVisible.asStateFlow()

    private val repository: HomeRepository


    private var originalQueue: List<MediaItem> = emptyList()
    private var currentQueue: List<MediaItem> = emptyList()
    private var currentIndex: Int = -1

    private val controllerListener = object : Player.Listener {

        // Khi bài hát thay đổi (metadata)
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _playerState.value = _playerState.value.copy(nowPlaying = mediaMetadata)
        }

        // Khi Play/Pause
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playerState.value = _playerState.value.copy(isShuffleOn = shuffleModeEnabled)
        }
        override fun onRepeatModeChanged(repeatMode: Int) {
            _playerState.value = _playerState.value.copy(repeatMode = repeatMode)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _playerState.value = _playerState.value.copy(
                nowPlaying = mediaItem?.mediaMetadata,
                currentMediaId = mediaItem?.mediaId ?: ""
            )
            // Cập nhật tổng thời lượng cho bài hát MỚI
            viewModelScope.launch {
                delay(50) // Chờ 50ms để ExoPlayer kịp tải duration
                _playerState.value = _playerState.value.copy(
                    totalDuration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                )
            }
            val artworkUri = mediaItem?.mediaMetadata?.artworkUri
            if (artworkUri != null) {
                // Chạy một coroutine riêng để tải màu
                viewModelScope.launch {
                    val color = extractDominantColor(getApplication(), artworkUri.toString())
                    // Cập nhật state với màu mới
                    _playerState.value = _playerState.value.copy(dominantColor = color)
                }
            } else {
                // Nếu không có ảnh bìa, reset màu
                _playerState.value = _playerState.value.copy(dominantColor = null)
            }
        }



        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                // Nếu lặp 1 bài, phát lại chính nó
                if (_playerState.value.repeatMode == Player.REPEAT_MODE_ONE) {
                    playSongAtIndex(currentIndex)
                } else {
                    // Nếu không, gọi "next"
                    next()
                }
            }
        }
    }

    init {
        // Kết nối với Service ngay khi ViewModel được tạo
        val apiService = RetrofitClient.create(application.applicationContext)
        repository = HomeRepository(apiService)
        initializeMediaController()

        startPositionPolling()

        viewModelScope.launch {
            PlayerEvents.eventFlow.collect { event ->
                when (event) {
                    is PlayerEvents.Event.Next -> next()
                    is PlayerEvents.Event.Previous -> previous()
                }
            }
        }
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )

        // Build controller (bất đồng bộ)
        val controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()

        // Thêm listener để biết khi nào nó sẵn sàng
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                // Thêm "người nghe" vào
                mediaController?.addListener(controllerListener)

                // Lấy trạng thái hiện tại (nếu service đã chạy)
                _playerState.value = PlayerState(
                    nowPlaying = mediaController?.mediaMetadata,
                    isPlaying = mediaController?.isPlaying ?: false,
                    isShuffleOn = mediaController?.shuffleModeEnabled ?: false,
                    repeatMode = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
                )
            },
            MoreExecutors.directExecutor() // Chạy trên luồng hiện tại
        )
    }
    fun playQueue(queue: List<MediaItem>, startIndex: Int) {
        this.originalQueue = queue

        // Kiểm tra xem chế độ Shuffle có đang bật không
        val isShuffleOn = _playerState.value.isShuffleOn

        if (isShuffleOn) {
            // LOGIC TRỘN BÀI (Thông minh)
            // 1. Lấy bài hát người dùng vừa bấm
            val clickedItem = queue[startIndex]

            // 2. Tạo bản sao có thể thay đổi
            val shuffledList = queue.toMutableList()

            // 3. Xóa bài vừa bấm ra khỏi danh sách, sau đó trộn phần còn lại
            shuffledList.removeAt(startIndex)
            shuffledList.shuffle()

            // 4. Chèn bài vừa bấm vào ĐẦU danh sách
            shuffledList.add(0, clickedItem)

            this.currentQueue = shuffledList
            this.currentIndex = 0 // Vì ta đã đưa nó lên đầu
        } else {
            // KHÔNG TRỘN
            this.currentQueue = queue
            this.currentIndex = startIndex
        }

        // Bắt đầu phát bài tại vị trí đã tính toán
        playSongAtIndex(this.currentIndex)
    }
    private fun playSongAtIndex(index: Int) {
        var newIndex = index

        if (newIndex < 0) {
            newIndex = 0
        } else if ( newIndex >= currentQueue.size) {
            if (_playerState.value.repeatMode == Player.REPEAT_MODE_ALL) {
                newIndex = 0
            } else {
                mediaController?.stop()
                _playerState.value = _playerState.value.copy(isPlaying = false)
                return
            }
        }
        this.currentIndex = newIndex
        val metadataItem = currentQueue[newIndex]
        val songId = metadataItem.mediaId.toLongOrNull() ?: return


        viewModelScope.launch {
            try {
                val streamUrl = repository.getSongStreamUrl(songId)
                val playableItem = metadataItem.buildUpon()
                    .setUri(Uri.parse(streamUrl))
                    .build()
                mediaController?.setMediaItem(playableItem)
                mediaController?.prepare()
                mediaController?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun startPositionPolling() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                // Chỉ "hỏi" khi đang phát nhạc
                if (_playerState.value.isPlaying) {
                    val currentPosition = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
                    val totalDuration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L

                    _playerState.value = _playerState.value.copy(
                        currentPosition = currentPosition,
                        totalDuration = totalDuration
                    )
                }
                delay(500L) // Hỏi 2 lần mỗi giây
            }
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }
    fun next() {
        var nextIndex = currentIndex + 1
        if (nextIndex >= currentQueue.size) {
            if (_playerState.value.repeatMode == Player.REPEAT_MODE_ALL) {
                nextIndex = 0 // Quay về đầu
            } else {
                // Nếu không lặp, dừng phát
                mediaController?.stop()
                return
            }
        }
        playSongAtIndex(nextIndex)
    }
    fun previous() {
        var prevIndex = currentIndex - 1
        if (prevIndex < 0) {
            prevIndex = currentQueue.size - 1
        }
        playSongAtIndex(prevIndex)
    }
    fun seekTo(positionFraction: Float) {
        val totalDuration = _playerState.value.totalDuration
        if (totalDuration > 0) {
            val newPosition = (totalDuration * positionFraction).toLong()
            mediaController?.seekTo(newPosition)
        }
    }
    fun toggleShuffle() {
        val isShuffleOn = !_playerState.value.isShuffleOn
        // Cập nhật Service (để nó biết) VÀ State (để UI biết)
        mediaController?.shuffleModeEnabled = isShuffleOn
        _playerState.value = _playerState.value.copy(isShuffleOn = isShuffleOn)

        // Cập nhật hàng đợi (queue) ngay lập tức
        if (isShuffleOn) {
            val currentItem = currentQueue.getOrNull(currentIndex)
            val shuffledList = originalQueue.toMutableList()
            if (currentItem != null) {
                shuffledList.remove(currentItem)
                shuffledList.shuffle()
                shuffledList.add(0, currentItem)
            } else {
                shuffledList.shuffle()
            }
            this.currentQueue = shuffledList
            this.currentIndex = 0
        } else {
            // Hủy trộn, quay về danh sách gốc
            val currentItem = currentQueue.getOrNull(currentIndex)
            this.currentQueue = originalQueue
            // Tìm lại index cũ
            this.currentIndex = originalQueue.indexOf(currentItem).coerceAtLeast(0)
        }
    }
    fun toggleRepeatMode() {
        val currentMode = _playerState.value.repeatMode // Đọc từ State
        val nextMode = when (currentMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = nextMode // Báo cho Service
        _playerState.value = _playerState.value.copy(repeatMode = nextMode) // Cập nhật State
    }


    fun openPlayerSheet() {
        _isPlayerSheetVisible.value = true
    }
    fun closePlayerSheet() {
        _isPlayerSheetVisible.value = false
    }

    fun clearData() {
        mediaController?.stop()
        mediaController?.clearMediaItems()
        _playerState.value = PlayerState()
        originalQueue = emptyList()
        currentQueue = emptyList()
        currentIndex = -1
    }

}