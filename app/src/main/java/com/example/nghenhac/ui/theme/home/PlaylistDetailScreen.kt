package com.example.nghenhac.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.nghenhac.ui.theme.components.AddToPlaylistSheet
import com.example.nghenhac.ui.theme.components.SongListItem
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel = viewModel(),
    sharedPlayerViewModel: SharedPlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(uiState.playlist?.name ?: "Đang tải...") })
        }
    ) { paddingValues -> // <-- Đây là padding của TopBar

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Áp dụng padding vào Box
            contentAlignment = Alignment.Center // Căn giữa cho cả Box
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = "Lỗi: ${uiState.error}")
                }
                uiState.playlist != null -> {
                    // Hiển thị danh sách bài hát
                    LazyColumn(
                        // Xóa padding ở đây vì Box đã xử lý
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(uiState.playlist!!.songs) { index, song ->
                            SongListItem(
                                song = song,
                                onClick = {
                                    viewModel.onSongSelected(index, sharedPlayerViewModel)
                                },
                                onAddClick = {
                                    viewModel.openAddSongSheet(song)
                                }
                            )
                        }
                    }
                }
            }
        }
        viewModel.selectedSongToAdd?.let { song ->
            AddToPlaylistSheet(
                song = song,
                playlists = uiState.myPlaylists,
                onPlaylistSelected = { targetPlaylistId ->
                    viewModel.addSongToOtherPlaylist(targetPlaylistId)
                    Toast.makeText(context, "Đã thêm vào playlist!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.closeAddSongSheet() }
            )
        }
    }
}