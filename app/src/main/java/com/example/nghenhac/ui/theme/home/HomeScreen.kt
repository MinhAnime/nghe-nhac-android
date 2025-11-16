package com.example.nghenhac.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // <-- Import Coil
import coil.request.ImageRequest
import com.example.nghenhac.R // <-- Bạn sẽ cần 1 ảnh placeholder
import com.example.nghenhac.data.PlaylistResponse
import com.example.nghenhac.data.PlaylistSummaryDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlaylistClick: (PlaylistSummaryDTO) -> Unit
    // (Sau này chúng ta sẽ truyền NavController vào đây)
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý lỗi (ví dụ: 401 Token hết hạn)
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_LONG).show()
            viewModel.clearError()
            // (Bạn có thể điều hướng về màn hình Login ở đây nếu lỗi là 401)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang chủ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        // (Chúng ta sẽ thêm bottomBar cho trình phát nhạc sau)
    ) { paddingValues ->

        // Box để xử lý trạng thái Loading
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // 1. Trạng thái Đang tải
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.playlists.isEmpty()) {
                // 2. Trạng thái Rỗng
                Text(
                    text = "Bạn chưa có playlist nào.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // 3. Trạng thái có dữ liệu
                PlaylistGrid(
                    playlists = uiState.playlists,
                    onPlaylistClick = onPlaylistClick
                )
            }
        }
    }
}

/**
 * Hiển thị lưới các playlist
 */
@Composable
fun PlaylistGrid(
    playlists: List<PlaylistSummaryDTO>,
    onPlaylistClick: (PlaylistSummaryDTO) -> Unit
) {
    // LazyColumn (Giống RecyclerView)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(playlists) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) }
            )
        }
    }
}

/**
 * Một item playlist (Phong cách Material 3 "Expressive")
 */
@Composable
fun PlaylistItem(
    playlist: PlaylistSummaryDTO,
    onClick: () -> Unit
) {
    // ElevatedCard là một component M3 rất "expressive"
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ShapeDefaults.Large // Bo góc lớn, mềm mại
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ẢNH BÌA ---
            // Lấy ảnh bìa của bài hát ĐẦU TIÊN trong playlist
            val imageUrl = playlist.coverArtUrl

            // Sử dụng Coil (AsyncImage) để tải ảnh
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    // (Bạn cần tạo 1 file ảnh `ic_default_cover.xml`
                    //  trong `res/drawable` làm placeholder)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build(),
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(ShapeDefaults.Medium) // Bo góc ảnh
            )

            Spacer(modifier = Modifier.width(16.dp))

            // --- TÊN PLAYLIST VÀ CHỦ SỞ HỮU ---
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Bởi ${playlist.ownerUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}