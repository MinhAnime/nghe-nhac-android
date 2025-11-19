package com.example.nghenhac.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nghenhac.ui.theme.components.AddToPlaylistSheet
import com.example.nghenhac.ui.theme.components.MenuItemData
import com.example.nghenhac.ui.theme.components.SongListItem
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel = viewModel(),
    sharedPlayerViewModel: SharedPlayerViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.playlist?.name ?: "Đang tải...") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues -> // <-- Đây là padding của TopBar

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = "Lỗi: ${uiState.error}")
                }
                uiState.playlist != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(uiState.songs) { index, song ->
                            SongListItem(
                                song = song,
                                onClick = {
                                    viewModel.onSongSelected(index, sharedPlayerViewModel)
                                },
                                menuItems = listOf(
                                    MenuItemData(
                                        text = "Xóa khỏi Playlist",
                                        icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = { viewModel.removeSongFromPlaylist(song.id) }
                                    ),
                                    MenuItemData(text = "Thêm vào Playlist khác",
                                        icon = { Icon(Icons.Default.PlaylistAdd, null) },
                                        onClick = { viewModel.openAddSongSheet(song) })
                                )
                            )
                            if (index >= uiState.songs.size - 1 && !viewModel.isLastPage && !viewModel.isLoadingMore) {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMore()
                                }
                            }
                        }
                        if (viewModel.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
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