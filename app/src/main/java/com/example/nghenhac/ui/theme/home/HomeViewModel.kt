package com.example.nghenhac.ui.theme.home

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
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

    var playlistToRename: PlaylistSummaryDTO? by mutableStateOf(null)

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
                // 1. Nhận playlist vừa tạo từ API
                val newPlaylist = homeRepository.createPlaylist(name)

                // 2. Chèn vào đầu danh sách hiện tại (Client-side update)
                val currentList = _uiState.value.playlists
                val newList = listOf(newPlaylist) + currentList

                _uiState.value = _uiState.value.copy(playlists = newList)

                isCreatePlaylistDialogOpen = false
                _messageChannel.send("Tạo playlist thành công!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi tạo playlist: ${e.message}")
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long) {
        val song = selectedSongToAdd ?: return

        viewModelScope.launch {
            try {
                // 1. Gọi API (Server cập nhật DB)
                homeRepository.addSongToPlaylist(playlistId, song.id)

                // --- BẮT ĐẦU SỬA: CẬP NHẬT UI CỤC BỘ (Client-side Update) ---

                // Tìm playlist vừa được thêm nhạc trong danh sách hiện tại
                val currentPlaylists = _uiState.value.playlists
                val updatedPlaylists = currentPlaylists.map { playlist ->
                    if (playlist.id == playlistId) {
                        // Nếu đúng là playlist này, ta cập nhật list thumbnails của nó
                        val newCoverUrl = song.coverArtUrl

                        val currentThumbnails = playlist.thumbnails ?: emptyList()

                        // Logic: Nếu bài hát có ảnh, thêm nó vào danh sách thumbnails hiện có
                        val newThumbnails = if (newCoverUrl != null) {
                            // Thêm vào đầu hoặc cuối tùy bạn (ở đây thêm vào cuối)
                            // Take(4) để đảm bảo không lưu quá nhiều URL thừa
                            (playlist.thumbnails + newCoverUrl).take(4)
                        } else {
                            currentThumbnails
                        }

                        // Tạo bản sao playlist mới với thumbnails mới
                        playlist.copy(_thumbnails = newThumbnails)
                    } else {
                        playlist
                    }
                }

                // Cập nhật State để UI vẽ lại ngay lập tức
                _uiState.value = _uiState.value.copy(playlists = updatedPlaylists)

                // --- KẾT THÚC SỬA ---

                selectedSongToAdd = null
                _messageChannel.send("Đã thêm bài hát thành công!")
            } catch (e: Exception) {
                val msg = parseErrorMessage(e)
                Log.d(TAG,msg)
                _uiState.value = _uiState.value.copy(error = msg)

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
    private fun parseErrorMessage(e: Exception): String {
        return if (e is retrofit2.HttpException && e.code() == 400) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val jsonObject = org.json.JSONObject(errorBody)
                jsonObject.getString("message") // Lấy message từ Backend
            } catch (e2: Exception) {
                "Yêu cầu không hợp lệ"
            }
        } else {
            e.message ?: "Lỗi không xác định"
        }
    }

    fun openRenameDialog(playlist: PlaylistSummaryDTO) {
        playlistToRename = playlist
    }

    fun closeRenameDialog() {
        playlistToRename = null
    }

    fun renamePlaylist(newName: String) {
        val playlist = playlistToRename ?: return

        viewModelScope.launch {
            try {
                // 1. Gọi API
                homeRepository.renamePlaylist(playlist.id, newName)

                // 2. Cập nhật UI Cục bộ (Client-side update)
                val currentPlaylists = _uiState.value.playlists
                val updatedPlaylists = currentPlaylists.map {
                    if (it.id == playlist.id) {
                        it.copy(name = newName) // Đổi tên trong list
                    } else {
                        it
                    }
                }

                _uiState.value = _uiState.value.copy(playlists = updatedPlaylists)

                _messageChannel.send("Đã đổi tên thành: $newName")
                closeRenameDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi đổi tên: ${e.message}")
                closeRenameDialog()
            }
        }
    }
}