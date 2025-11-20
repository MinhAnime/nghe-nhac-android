package com.example.nghenhac.ui.theme.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.R

@Composable
fun SongListItem(
    song: SongResponseDTO,
    onClick: () -> Unit,
    menuItems: List<MenuItemData> = emptyList()
) {
    // 1. Giảm khoảng cách giữa các bài (từ 4.dp xuống 2.dp)
    Spacer(modifier = Modifier.height(2.dp))

    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                // 2. Giảm cỡ chữ tiêu đề & làm đậm (bodyLarge thay vì titleMedium)
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium, // Làm đậm nhẹ để dễ đọc
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        },
        supportingContent = {
            Text(
                text = song.artistName,
                maxLines = 1,
                // 3. Giảm cỡ chữ phụ (bodySmall thay vì bodyMedium)
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.coverArtUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build(),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    // 4. Giảm kích thước ảnh (từ 56.dp xuống 48.dp)
                    .size(48.dp)
                    .clip(ShapeDefaults.Small)
            )
        },
        trailingContent = {
            if (menuItems.isNotEmpty()) {
                MoreOptionsButton(menuItems = menuItems)
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(ShapeDefaults.Medium)
            .clickable(onClick = onClick)
    )
}