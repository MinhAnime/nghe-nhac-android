package com.example.nghenhac.ui.theme.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nghenhac.data.PlaylistSummaryDTO // Import DTO
import com.example.nghenhac.ui.theme.components.PlaylistCard // Import Card
import com.example.nghenhac.ui.theme.components.SongListItem
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.player.convertSongsToMediaItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    playerViewModel: SharedPlayerViewModel,
    onBackClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Lấy state nhạc để kiểm tra bài đang phát (tùy chọn, để làm nổi bật)
    val playerState by playerViewModel.playerState.collectAsState()

    Scaffold(
        topBar = {
            SearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.onQueryChange(it) },
                onSearch = { /* Debounce đã tự xử lý */ },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Tìm bài hát, playlist...") },
                leadingIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {}
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Lỗi: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Kiểm tra nếu có bất kỳ kết quả nào (Playlist HOẶC Bài hát)
                uiState.playlists.isNotEmpty() || uiState.songs.isNotEmpty() -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp) // Chừa chỗ cho BottomBar
                    ) {

                        // === PHẦN 1: KẾT QUẢ PLAYLIST (Ngang) ===
                        if (uiState.playlists.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Playlist",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(uiState.playlists) { playlist ->
                                        PlaylistCard(
                                            playlist = playlist,
                                            onClick = { onPlaylistClick(playlist.id) },
                                        )
                                    }
                                }
                            }
                        }

                        // === PHẦN 2: KẾT QUẢ BÀI HÁT (Dọc) ===
                        if (uiState.songs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Bài hát",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                                )
                            }

                            itemsIndexed(uiState.songs) { index, song ->

                                val isCurrentSong = playerState.currentMediaId == song.id.toString()

                                SongListItem(
                                    song = song,
                                    onClick = {
                                        // Chuyển đổi danh sách tìm kiếm thành danh sách phát
                                        val mediaItems = convertSongsToMediaItems(uiState.songs)
                                        playerViewModel.playQueue(mediaItems, index)
                                    },
                                    // Hiển thị trạng thái đang phát (nếu có)
                                    isCurrentSong = isCurrentSong,
                                    isPlaying = playerState.isPlaying,
                                    // (Chưa có menu thêm vào playlist ở đây, có thể thêm sau)
                                )
                            }
                        }
                    }
                }
                // Trường hợp không tìm thấy gì
                viewModel.searchQuery.isNotEmpty() -> {
                    Text(
                        text = "Không tìm thấy kết quả nào.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}