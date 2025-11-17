package com.example.nghenhac.ui.theme.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nghenhac.data.PlaylistResponse
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


data class HomeUiState(
    val isLoading: Boolean = true,
    val playlists: List<PlaylistSummaryDTO> = emptyList(),
    val songs: List<SongResponseDTO> = emptyList(),
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val homeRepository: HomeRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    var isCreatePlaylistDialogOpen by mutableStateOf(false)

    var selectedSongToAdd: SongResponseDTO? by mutableStateOf(null)

    private val _messageChannel = Channel<String>()
    val messageFlow = _messageChannel.receiveAsFlow()

    init {
        // Khởi tạo Repository
        // (Lưu ý: Tốt hơn nên dùng Hilt/Dagger, nhưng làm thủ công cũng được)
        val apiService = RetrofitClient.create(application.applicationContext)
        homeRepository = HomeRepository(apiService)

        fetchAllHomeData()
    }

    /**
     * Gọi API để lấy playlist
     */
    fun fetchAllHomeData() {
        _uiState.value = HomeUiState(isLoading = true) // Bắt đầu loading

        viewModelScope.launch {
            try {
                val playlistsDeferred = async { homeRepository.getMyPlaylists() }
                val songsDeferred = async { homeRepository.getAllSongs() }
                // AuthInterceptor sẽ tự động thêm token vào request này
                val playlists = playlistsDeferred.await()
                val songs = songsDeferred.await()
                _uiState.value = HomeUiState(
                    isLoading = false,
                    playlists = playlists,
                    songs = songs
                )
            } catch (e: Exception) {
                // Xử lý lỗi (ví dụ: token hết hạn -> 401 Unauthorized)
                _uiState.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                homeRepository.createPlaylist(name)
                // Tạo xong thì tải lại danh sách để thấy playlist mới
                fetchAllHomeData()
                isCreatePlaylistDialogOpen = false
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi tạo playlist: ${e.message}")
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long) {
        val song = selectedSongToAdd ?: return

        viewModelScope.launch {
            try {
                homeRepository.addSongToPlaylist(playlistId, song.id)
                selectedSongToAdd = null
                _messageChannel.send("Đã thêm bài hát thành công!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi thêm nhạc: ${e.message}")
            }
        }
    }

    fun openCreateDialog() { isCreatePlaylistDialogOpen = true }
    fun closeCreateDialog() { isCreatePlaylistDialogOpen = false }

    fun openAddSongSheet(song: SongResponseDTO) { selectedSongToAdd = song }
    fun closeAddSongSheet() { selectedSongToAdd = null }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}