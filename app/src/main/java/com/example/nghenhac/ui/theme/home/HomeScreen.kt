package com.example.nghenhac.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nghenhac.R
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.player.convertSongsToMediaItems
import com.example.nghenhac.ui.theme.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playerViewModel: SharedPlayerViewModel,
    onPlaylistClick: (PlaylistSummaryDTO) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_LONG).show()
            homeViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang chủ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // === PHẦN 1: PLAYLIST ===
                    item {
                        Text(
                            text = "Playlist của bạn",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        if (uiState.playlists.isEmpty()) {
                            Text(
                                text = "Bạn chưa có playlist nào.",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.playlists) { playlist ->
                                    PlaylistCard(
                                        playlist = playlist,
                                        onClick = { onPlaylistClick(playlist) }
                                    )
                                }
                            }
                        }
                    }

                    // === PHẦN 2: BÀI HÁT (SONGS) ===
                    item {
                        Text(
                            text = "Tất cả bài hát",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                        )
                    }

                    if (uiState.songs.isEmpty()) {
                        item {
                            Text(
                                text = "Chưa có bài hát nào.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        itemsIndexed(uiState.songs) { index, song ->
                            SongListItem(
                                song = song,
                                onClick = {
                                    // 1. Convert dữ liệu
                                    val mediaItems = convertSongsToMediaItems(uiState.songs)
                                    // 2. Gọi ViewModel chung để phát nhạc
                                    playerViewModel.playQueue(
                                        queue = mediaItems,
                                        startIndex = index
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PlaylistCard(
    playlist: PlaylistSummaryDTO,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(playlist.coverArtUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build(),
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}