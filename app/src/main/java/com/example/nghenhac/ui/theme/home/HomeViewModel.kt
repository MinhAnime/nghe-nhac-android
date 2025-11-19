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
import com.example.nghenhac.data.TokenManager
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.AuthRepository
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
    private val authRepository: AuthRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    var isCreatePlaylistDialogOpen by mutableStateOf(false)

    var selectedSongToAdd: SongResponseDTO? by mutableStateOf(null)

    private val _messageChannel = Channel<String>()
    val messageFlow = _messageChannel.receiveAsFlow()

    private var currentPage = 0

    var isLoadingMore = false
    var isLastPage = false

    private var currentSongPage = 0
    var isLastSongPage = false
    var isLoadingMoreSongs = false
    var playlistToDelete: PlaylistSummaryDTO? by mutableStateOf(null)

    init {
        // Khởi tạo Repository
        val apiService = RetrofitClient.create(application.applicationContext)
        val tokenManager = TokenManager(application.applicationContext)
        homeRepository = HomeRepository(apiService)
        authRepository = AuthRepository(apiService, tokenManager)

        fetchAllHomeData()
    }

    fun fetchAllHomeData() {

        currentPage = 0
        isLastPage = false
        isLoadingMore = false

        currentSongPage = 0
        isLastSongPage = false


        _uiState.value = HomeUiState(isLoading = true) // Bắt đầu loading

        viewModelScope.launch {
            try {
                val playlistsDeferred = async { homeRepository.getMyPlaylists(page = 0) }
                val songsDeferred = async { homeRepository.getAllSongs(page = 0) }
                // AuthInterceptor sẽ tự động thêm token vào request này
                val playlists = playlistsDeferred.await()
                val songs = songsDeferred.await()

                if (playlists.isEmpty()) {
                    isLastPage = true
                }
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
    fun loadMorePlaylists() {
        if (isLoadingMore || isLastPage) return

        isLoadingMore = true

        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val newPlaylists = homeRepository.getMyPlaylists(page = nextPage)

                if (newPlaylists.isNotEmpty()) {
                    val currentList = _uiState.value.playlists
                    val combinedList = currentList + newPlaylists

                    _uiState.value = _uiState.value.copy(playlists = combinedList)
                    currentPage = nextPage
                } else {
                    isLastPage = true
                }
            } catch (e: Exception) {
            } finally {
                isLoadingMore = false
            }
        }
    }
    fun loadMoreSongs() {
        if (isLoadingMoreSongs || isLastSongPage) return
        isLoadingMoreSongs = true

        viewModelScope.launch {
            try {
                val nextPage = currentSongPage + 1
                val newSongs = homeRepository.getAllSongs(page = nextPage)

                if (newSongs.isNotEmpty()) {
                    val currentList = _uiState.value.songs
                    val combinedList = currentList + newSongs

                    _uiState.value = _uiState.value.copy(songs = combinedList)
                    currentSongPage = nextPage
                } else {
                    isLastSongPage = true
                }
            } catch (e: Exception) {
            } finally {
                isLoadingMoreSongs = false
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
    fun openDeleteDialog(playlist: PlaylistSummaryDTO) {
        playlistToDelete = playlist
    }

    fun closeDeleteDialog() {
        playlistToDelete = null}

    fun deletePlaylist() {
        val playlist = playlistToDelete ?: return

        viewModelScope.launch {
            try {
                homeRepository.deletePlaylist(playlist.id)

                val updatedList = _uiState.value.playlists.filter { it.id != playlist.id }
                _uiState.value = _uiState.value.copy(playlists = updatedList)

                _messageChannel.send("Đã xóa playlist: ${playlist.name}")
                closeDeleteDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi xóa: ${e.message}")
                closeDeleteDialog()
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
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _messageChannel.send("Đã đăng xuất!")
        }
    }
}