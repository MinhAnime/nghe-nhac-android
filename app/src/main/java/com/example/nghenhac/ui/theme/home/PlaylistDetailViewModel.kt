package com.example.nghenhac.ui.theme.home

import android.app.Application
import android.content.ComponentName
import android.net.Uri
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
import com.example.nghenhac.services.PlaybackService
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel

data class PlaylistDetailState(
    val isLoading: Boolean = true,
    val playlist: PlaylistDetailDTO? = null,
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
}
