package com.example.nghenhac.ui.theme.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    // (Sau này ta có thể inject ViewModel, nhưng dùng tạm hàm này)
    viewModel: PlaylistDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(uiState.playlist?.name ?: "Đang tải...") })
        }
    ) { paddingValues -> // <-- Đây là padding của TopBar

        // --- SỬA LỖI Ở ĐÂY ---
        // Bọc mọi thứ trong 1 Box để .align() hoạt động
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Áp dụng padding vào Box
            contentAlignment = Alignment.Center // Căn giữa cho cả Box
        ) {
            when {
                uiState.isLoading -> {
                    // Giờ .align() đã bị xóa vì Box lo việc căn giữa
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
                        items(uiState.playlist!!.songs) { song ->
                            // (Chúng ta sẽ làm item này đẹp hơn sau)
                            ListItem(
                                headlineContent = { Text(text = song.title) },
                                supportingContent = { Text(text = song.artistName) }
                            )
                        }
                    }
                }
            }
        }
    }
}