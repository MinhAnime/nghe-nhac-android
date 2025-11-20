package com.example.nghenhac.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.SongResponseDTO


@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo Playlist mới") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Tên Playlist") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) {
                Text("Tạo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    song: SongResponseDTO,
    playlists: List<PlaylistSummaryDTO>,
    onPlaylistSelected: (Long) -> Unit, // Callback nhận vào một số Long (ID)
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Thêm bài \"${song.title}\" vào...",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (playlists.isEmpty()) {
                Text("Bạn chưa có playlist nào.")
            } else {
                LazyColumn {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                            modifier = Modifier.clickable {
                                onPlaylistSelected(playlist.id)

                                onDismiss()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // Khởi tạo giá trị ban đầu là tên cũ
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi tên Playlist") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Tên mới") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank() && text != currentName) {
                    onConfirm(text)
                } else {
                    onDismiss() // Nếu tên không đổi hoặc rỗng thì đóng luôn
                }
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}