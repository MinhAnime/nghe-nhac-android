package com.example.nghenhac.ui.theme.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaylistDetailState(
    val isLoading: Boolean = true,
    val playlist: PlaylistDetailDTO? = null,
    val error: String? = null
)

class PlaylistDetailViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val repository: HomeRepository
    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailState())
    val uiState = _uiState.asStateFlow()

    init {
        val apiService = RetrofitClient.create(application.applicationContext)
        repository = HomeRepository(apiService)
        fetchPlaylistDetails()
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
}