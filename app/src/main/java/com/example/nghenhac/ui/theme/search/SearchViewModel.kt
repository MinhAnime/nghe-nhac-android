package com.example.nghenhac.ui.theme.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nghenhac.data.PlaylistSummaryDTO // <-- Import DTO Playlist
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- 1. CẬP NHẬT STATE (QUAN TRỌNG) ---
data class SearchUiState(
    val isLoading: Boolean = false,
    // Thay vì 'results', ta tách làm 2
    val songs: List<SongResponseDTO> = emptyList(),
    val playlists: List<PlaylistSummaryDTO> = emptyList(), // <-- DÒNG BẠN ĐANG THIẾU
    val error: String? = null
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeRepository
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    // Từ khóa tìm kiếm hiện tại
    var searchQuery by mutableStateOf("")
        private set

    // Job để quản lý việc hủy tìm kiếm cũ (Debounce)
    private var searchJob: Job? = null

    init {
        val apiService = RetrofitClient.create(application.applicationContext)
        repository = HomeRepository(apiService)
    }

    fun onQueryChange(newQuery: String) {
        searchQuery = newQuery

        // Hủy job cũ nếu đang chạy (người dùng gõ tiếp)
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState() // Reset nếu rỗng
            return
        }

        // Tạo job mới
        searchJob = viewModelScope.launch {
            // 1. Debounce: Chờ 500ms
            delay(500)

            // 2. Bắt đầu gọi API
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Gọi API Search (Trả về SearchResponseDTO gồm cả songs và playlists)
                val result = repository.search(newQuery)

                // 3. Cập nhật UI với dữ liệu tách biệt
                _uiState.value = SearchUiState(
                    isLoading = false,
                    songs = result.songs,
                    playlists = result.playlists // <-- Gán dữ liệu vào đây
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun clearQuery() {
        onQueryChange("")
    }
}