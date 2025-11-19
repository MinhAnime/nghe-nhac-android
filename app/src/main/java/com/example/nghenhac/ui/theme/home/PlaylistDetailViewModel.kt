package com.example.nghenhac.ui.theme.home

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.MediaMetadata
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.services.PlaybackService
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel

data class PlaylistDetailState(
    val isLoading: Boolean = true,
    val playlist: PlaylistDetailDTO? = null,
    val songs: List<SongResponseDTO> = emptyList(),
    val myPlaylists: List<PlaylistSummaryDTO> = emptyList(),
    val error: String? = null
)

class PlaylistDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository: HomeRepository
    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailState())
    val uiState = _uiState.asStateFlow()

    var selectedSongToAdd: SongResponseDTO? by mutableStateOf(null)

    private var currentPage = 0
    var isLastPage = false
    var isLoadingMore = false


    init {
        val apiService = RetrofitClient.create(application.applicationContext)
        repository = HomeRepository(apiService)
        fetchPlaylistDetails()
        fetchSongs(page = 0)
        fetchMyPlayLists()

    }

    fun fetchSongs(page: Int) {
        if (page == 0) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val newSongs = repository.getSongsInPlaylist(playlistId, page)

                if (newSongs.isNotEmpty()) {
                    val currentSongs = _uiState.value.songs
                    val combinedSongs = currentSongs + newSongs

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        songs = combinedSongs
                    )
                    // Cập nhật trang hiện tại
                    currentPage = page
                } else {
                    // Nếu trả về rỗng -> Đã hết dữ liệu
                    isLastPage = true
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            } finally {
                isLoadingMore = false // Đảm bảo tắt cờ loading
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore || isLastPage) return

        isLoadingMore = true
        fetchSongs(currentPage + 1)
    }

    private fun fetchPlaylistDetails() {
        viewModelScope.launch {
            try {
                val details = repository.getPlaylistDetails(playlistId)
                _uiState.value = PlaylistDetailState(isLoading = false, playlist = details)
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailState(isLoading = false, error = e.message)
            }
        }
    }
    private fun fetchMyPlayLists() {
        viewModelScope.launch {
            try {
                val playlists = repository.getMyPlaylists()
                _uiState.value = _uiState.value.copy(myPlaylists = playlists)
            } catch (e: Exception) {
            }
        }
    }

    fun onSongSelected(
        clickedIndex: Int,
        sharedPlayerViewModel: SharedPlayerViewModel
    ) {
        val songList = _uiState.value.playlist?.songs
        if (songList.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Không có bài hát để phát")
            return
        }

        val mediaItems = songList.map { song ->
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artistName)
            song.coverArtUrl?.let { url ->
                if (url.isNotBlank()) {
                    metadataBuilder.setArtworkUri(Uri.parse(url))
                }
            }
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }

        sharedPlayerViewModel.playQueue(
            queue = mediaItems,
            startIndex = clickedIndex
        )
    }

    fun addSongToOtherPlaylist(targetPlaylistId: Long) {
        val song = selectedSongToAdd ?: return
        viewModelScope.launch {
            try {
                repository.addSongToPlaylist(targetPlaylistId, song.id)
                selectedSongToAdd = null // Đóng sheet
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi thêm nhạc: ${e.message}")
            }
        }
    }
    fun removeSongFromPlaylist(songId: Long) {
        viewModelScope.launch {
            try {
                // Gọi xuống Repository
                repository.removeSongFromPlaylist(playlistId, songId)

                // Cập nhật lại danh sách bài hát trên UI ngay lập tức (Client-side update)
                // để người dùng không phải chờ tải lại
                val updatedList = _uiState.value.songs.filter { it.id != songId }
                _uiState.value = _uiState.value.copy(songs = updatedList)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi xóa bài: ${e.message}")
            }
        }
    }
    fun openAddSongSheet(song: SongResponseDTO) { selectedSongToAdd = song }
    fun closeAddSongSheet() { selectedSongToAdd = null }
}
