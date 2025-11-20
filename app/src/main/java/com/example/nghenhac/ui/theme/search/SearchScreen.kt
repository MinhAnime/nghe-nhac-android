package com.example.nghenhac.ui.theme.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nghenhac.ui.theme.components.SongListItem
import com.example.nghenhac.ui.theme.player.SharedPlayerViewModel
import com.example.nghenhac.ui.theme.player.convertSongsToMediaItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    playerViewModel: SharedPlayerViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            // Thanh tìm kiếm nằm trên TopBar
            SearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.onQueryChange(it) },
                onSearch = { /* (Đã tự động search khi gõ) */ },
                active = false, // Để false để nó trông giống TextField bình thường
                onActiveChange = {},
                placeholder = { Text("Tìm bài hát, nghệ sĩ...") },
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
                modifier = Modifier.fillMaxWidth().padding(8.dp)
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
                uiState.results.isEmpty() && viewModel.searchQuery.isNotEmpty() -> {
                    Text(
                        text = "Không tìm thấy kết quả nào.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(uiState.results) { index, song ->
                            SongListItem(
                                song = song,
                                onClick = {
                                    val mediaItems = convertSongsToMediaItems(uiState.results)
                                    playerViewModel.playQueue(mediaItems, index)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
