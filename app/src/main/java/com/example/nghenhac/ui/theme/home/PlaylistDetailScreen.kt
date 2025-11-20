package com.example.nghenhac.ui.theme.home

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share // <-- 1. IMPORT ICON SHARE
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nghenhac.ui.theme.components.*
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

    // Lấy trạng thái phát nhạc chung
    val playerState by sharedPlayerViewModel.playerState.collectAsState()

    // State cho Menu 3 chấm trên TopBar
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Để trống title vì hiện to ở dưới */ },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Tùy chọn")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // --- 2. MỤC CHIA SẺ (THÊM MỚI) ---
                        DropdownMenuItem(
                            text = { Text("Chia sẻ") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                showMenu = false
                                // Gọi hàm chia sẻ (kiểm tra null cho chắc)
                                uiState.playlist?.let { pl ->
                                    sharePlaylist(context, pl.id, pl.name)
                                }
                            }
                        )
                        // --------------------------------
                        val isPublic = uiState.playlist?.isPublic == true
                        DropdownMenuItem(
                            text = { Text(if (isPublic) "Riêng tư" else "Công khai") },
                            leadingIcon = {
                                Icon(
                                    if (isPublic) Icons.Default.Lock else Icons.Default.Public,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                viewModel.togglePrivacy() // Gọi hàm ViewModel
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Đổi tên") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = {
                                showMenu = false
                                viewModel.showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa Playlist", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                viewModel.showDeleteDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && uiState.playlist == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val playlist = uiState.playlist

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // --- HEADER ---
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PlaylistCoverGrid(
                                images = uiState.songs.mapNotNull { it.coverArtUrl }.take(4),
                                modifier = Modifier
                                    .size(220.dp)
                                    .aspectRatio(1f)
                                    .shadow(12.dp, MaterialTheme.shapes.medium)
                                    .clip(MaterialTheme.shapes.medium)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = playlist?.name ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Tạo bởi ${playlist?.ownerUsername ?: "..."}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // --- DANH SÁCH BÀI HÁT ---
                    item {
                        Text(
                            "Danh sách bài hát (${uiState.songs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    itemsIndexed(uiState.songs) { index, song ->

                        val isCurrentSong = playerState.currentMediaId == song.id.toString()
                        val isPlaying = playerState.isPlaying

                        SongListItem(
                            song = song,
                            onClick = { viewModel.onSongSelected(index, sharedPlayerViewModel) },

                            isCurrentSong = isCurrentSong,
                            isPlaying = isPlaying,

                            menuItems = listOf(
                                MenuItemData(
                                    text = "Thêm vào Playlist khác",
                                    icon = { Icon(Icons.Default.PlaylistAdd, null) },
                                    onClick = { viewModel.openAddSongSheet(song) }
                                ),
                                MenuItemData(
                                    text = "Xóa khỏi Playlist này",
                                    icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { viewModel.removeSongFromPlaylist(song.id) }
                                )
                            )
                        )

                        if (index >= uiState.songs.size - 1 && !viewModel.isLastPage && !viewModel.isLoadingMore) {
                            LaunchedEffect(Unit) { viewModel.loadMore() }
                        }
                    }

                    if (viewModel.isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // --- CÁC DIALOG ---

        if (viewModel.showRenameDialog) {
            RenamePlaylistDialog(
                currentName = uiState.playlist?.name ?: "",
                onDismiss = { viewModel.showRenameDialog = false },
                onConfirm = { newName -> viewModel.renamePlaylist(newName) }
            )
        }

        if (viewModel.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteDialog = false },
                title = { Text("Xóa Playlist?") },
                text = { Text("Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePlaylist(onSuccess = {
                                Toast.makeText(context, "Đã xóa playlist", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            })
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Xóa") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteDialog = false }) { Text("Hủy") }
                }
            )
        }

        viewModel.selectedSongToAdd?.let { song ->
            AddToPlaylistSheet(
                song = song,
                playlists = uiState.myPlaylists,
                onPlaylistSelected = { id -> viewModel.addSongToOtherPlaylist(id) },
                onDismiss = { viewModel.closeAddSongSheet() }
            )
        }
    }
}

// --- 3. HÀM CHIA SẺ (HELPER) ---
fun sharePlaylist(context: Context, playlistId: Long, playlistName: String) {
    val deepLink = "https://api.minhduong.id.vn/playlist/$playlistId"
    val shareText = "Nghe playlist \"$playlistName\" cực hay này nè:\n$deepLink"

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ playlist qua...")
    context.startActivity(shareIntent)
}