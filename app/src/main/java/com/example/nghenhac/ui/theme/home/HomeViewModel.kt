package com.example.nghenhac.ui.theme.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nghenhac.data.PlaylistResponse
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class HomeUiState(
    val isLoading: Boolean = true,
    val playlists: List<PlaylistSummaryDTO> = emptyList(),
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val homeRepository: HomeRepository

    // Trạng thái (State) của UI
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Khởi tạo Repository
        // (Lưu ý: Tốt hơn nên dùng Hilt/Dagger, nhưng làm thủ công cũng được)
        val apiService = RetrofitClient.create(application.applicationContext)
        homeRepository = HomeRepository(apiService)

        // Tải playlist ngay khi ViewModel được tạo
        fetchMyPlaylists()
    }

    /**
     * Gọi API để lấy playlist
     */
    fun fetchMyPlaylists() {
        _uiState.value = HomeUiState(isLoading = true) // Bắt đầu loading

        viewModelScope.launch {
            try {
                // AuthInterceptor sẽ tự động thêm token vào request này
                val playlists = homeRepository.getMyPlaylists()
                _uiState.value = HomeUiState(isLoading = false, playlists = playlists)
            } catch (e: Exception) {
                // Xử lý lỗi (ví dụ: token hết hạn -> 401 Unauthorized)
                _uiState.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}