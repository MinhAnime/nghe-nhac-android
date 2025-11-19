package com.example.nghenhac.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
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
import com.example.nghenhac.ui.theme.components.AddToPlaylistSheet
import com.example.nghenhac.ui.theme.components.CreatePlaylistDialog
import com.example.nghenhac.ui.theme.components.MenuItemData
import com.example.nghenhac.ui.theme.components.MoreOptionsButton
import com.example.nghenhac.ui.theme.components.SongListItem
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.player.convertSongsToMediaItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playerViewModel: SharedPlayerViewModel,
    onPlaylistClick: (PlaylistSummaryDTO) -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
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

    // Xử lý thông báo thành công
    LaunchedEffect(Unit) {
        homeViewModel.messageFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                                        onClick = { onPlaylistClick(playlist) },
                                        onDeleteClick = {
                                            homeViewModel.openDeleteDialog(playlist)
                                        }
                                    )
                                }

                                // Logic tải thêm playlist (Infinite Scroll)
                                item {
                                    if (homeViewModel.isLoadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else if (!homeViewModel.isLastPage) {
                                        LaunchedEffect(Unit) { homeViewModel.loadMorePlaylists() }
                                    }
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
                                    val mediaItems = convertSongsToMediaItems(uiState.songs)
                                    playerViewModel.playQueue(
                                        queue = mediaItems,
                                        startIndex = index
                                    )
                                },
                                menuItems = listOf(
                                    MenuItemData(
                                        text = "Thêm vào Playlist",
                                        icon = { Icon(Icons.Default.PlaylistAdd, null) },
                                        onClick = { homeViewModel.openAddSongSheet(song) }
                                    )
                                )
                            )

                            // Logic tải thêm bài hát (Infinite Scroll)
                            if (index >= uiState.songs.size - 1 && !homeViewModel.isLastSongPage && !homeViewModel.isLoadingMoreSongs) {
                                LaunchedEffect(Unit) {
                                    homeViewModel.loadMoreSongs()
                                }
                            }
                        }

                        if (homeViewModel.isLoadingMoreSongs) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                            }
                        }
                    }
                }
            }
        }

        // --- HIỂN THỊ DIALOG VÀ SHEET ---

        if (homeViewModel.isCreatePlaylistDialogOpen) {
            CreatePlaylistDialog(
                onDismiss = { homeViewModel.closeCreateDialog() },
                onConfirm = { name -> homeViewModel.createPlaylist(name) }
            )
        }

        homeViewModel.selectedSongToAdd?.let { song ->
            AddToPlaylistSheet(
                song = song,
                playlists = uiState.playlists,
                onPlaylistSelected = { playlistId ->
                    homeViewModel.addSongToPlaylist(playlistId)
                },
                onDismiss = { homeViewModel.closeAddSongSheet() }
            )
        }

        homeViewModel.playlistToDelete?.let { playlist ->
            AlertDialog(
                onDismissRequest = { homeViewModel.closeDeleteDialog() },
                title = { Text("Xóa Playlist?") },
                text = { Text("Bạn có chắc chắn muốn xóa '${playlist.name}' không?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            homeViewModel.deletePlaylist()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { homeViewModel.closeDeleteDialog() }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: PlaylistSummaryDTO,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(160.dp)
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
                    .fillMaxWidth(1f)
                    .weight(1f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 0.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                MoreOptionsButton(
                    menuItems = listOf(
                        MenuItemData(
                            text = "Xóa Playlist",
                            icon = { Icon(Icons.Default.Cancel, contentDescription = null) },
                            onClick = onDeleteClick
                        )
                    )
                )
            }
        }
    }
}