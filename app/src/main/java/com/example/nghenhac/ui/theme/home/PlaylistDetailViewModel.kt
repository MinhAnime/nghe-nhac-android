package com.example.nghenhac.ui.theme.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.player.convertSongsToMediaItems // <-- Import hàm chuyển đổi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

data class PlaylistDetailState(
    val isLoading: Boolean = true,
    val playlist: PlaylistDetailDTO? = null, // Thông tin playlist (Tên, Ảnh...)
    val songs: List<SongResponseDTO> = emptyList(), // Danh sách bài hát (Phân trang)
    val myPlaylists: List<PlaylistSummaryDTO> = emptyList(), // Danh sách playlist cá nhân (để thêm nhạc)
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

    var showRenameDialog by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    init {
        val apiService = RetrofitClient.create(application.applicationContext)
        repository = HomeRepository(apiService)

        // Gọi các hàm khởi tạo
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
                    // Nếu là trang 0 thì thay thế, trang sau thì cộng dồn
                    val combinedSongs = if (page == 0) newSongs else currentSongs + newSongs

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        songs = combinedSongs
                    )
                    currentPage = page
                } else {
                    isLastPage = true
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            } finally {
                isLoadingMore = false
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
                // --- SỬA: Dùng .copy() để không mất danh sách songs ---
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    playlist = details
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun fetchMyPlayLists() {
        viewModelScope.launch {
            try {
                val playlists = repository.getMyPlaylists()
                // --- SỬA: Dùng .copy() ---
                _uiState.value = _uiState.value.copy(myPlaylists = playlists)
            } catch (e: Exception) {
                // Bỏ qua lỗi nhẹ
            }
        }
    }

    // --- SỬA HÀM PHÁT NHẠC ---
    fun onSongSelected(
        clickedIndex: Int,
        sharedPlayerViewModel: SharedPlayerViewModel
    ) {
        // 1. Lấy danh sách từ 'songs' (danh sách phân trang đang hiển thị)
        // CHỨ KHÔNG PHẢI từ 'playlist.songs' (danh sách cũ/thiếu)
        val songList = _uiState.value.songs

        if (songList.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Không có bài hát để phát")
            return
        }

        // 2. Sử dụng hàm chuyển đổi có sẵn (Gọn code)
        val mediaItems = convertSongsToMediaItems(songList)

        sharedPlayerViewModel.playQueue(
            queue = mediaItems,
            startIndex = clickedIndex
        )
    }

    // --- SỬA HÀM THÊM NHẠC (CẬP NHẬT UI + FIX NULL) ---
    fun addSongToOtherPlaylist(targetPlaylistId: Long) {
        val song = selectedSongToAdd ?: return
        viewModelScope.launch {
            try {
                repository.addSongToPlaylist(targetPlaylistId, song.id)

                // Cập nhật UI cục bộ (cập nhật thumbnail cho playlist trong danh sách chọn)
                val currentPlaylists = _uiState.value.myPlaylists
                val updatedPlaylists = currentPlaylists.map { playlist ->
                    if (playlist.id == targetPlaylistId) {
                        val newCoverUrl = song.coverArtUrl
                        val currentThumbnails = playlist.thumbnails ?: emptyList() // Fix lỗi Null

                        val newThumbnails = if (newCoverUrl != null) {
                            (currentThumbnails + newCoverUrl).take(4)
                        } else {
                            currentThumbnails
                        }
                        playlist.copy(_thumbnails = newThumbnails)
                    } else {
                        playlist
                    }
                }
                _uiState.value = _uiState.value.copy(myPlaylists = updatedPlaylists)

                selectedSongToAdd = null
            } catch (e: Exception) {
                val msg = parseErrorMessage(e)
                _uiState.value = _uiState.value.copy(error = msg)
            }
        }
    }

    fun removeSongFromPlaylist(songId: Long) {
        viewModelScope.launch {
            try {
                repository.removeSongFromPlaylist(playlistId, songId)

                // Cập nhật lại danh sách bài hát trên UI
                val updatedList = _uiState.value.songs.filter { it.id != songId }
                _uiState.value = _uiState.value.copy(songs = updatedList)

            } catch (e: Exception) {
                val msg = parseErrorMessage(e)
                _uiState.value = _uiState.value.copy(error = msg)
            }
        }
    }

    fun renamePlaylist(newName: String) {
        viewModelScope.launch {
            try {
                repository.renamePlaylist(playlistId, newName)

                // Cập nhật UI ngay lập tức
                val currentPlaylist = _uiState.value.playlist
                if (currentPlaylist != null) {
                    _uiState.value = _uiState.value.copy(
                        playlist = currentPlaylist.copy(name = newName)
                    )
                }
                showRenameDialog = false
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    fun deletePlaylist(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePlaylist(playlistId)
                // Xóa xong thì gọi callback để thoát màn hình
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    // Hàm xử lý lỗi thân thiện (Copy từ HomeViewModel)
    private fun parseErrorMessage(e: Exception): String {
        return if (e is HttpException && e.code() == 400) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val jsonObject = JSONObject(errorBody)
                jsonObject.getString("message")
            } catch (e2: Exception) {
                "Yêu cầu không hợp lệ"
            }
        } else {
            e.message ?: "Lỗi không xác định"
        }
    }

    fun openAddSongSheet(song: SongResponseDTO) { selectedSongToAdd = song }
    fun closeAddSongSheet() { selectedSongToAdd = null }
}