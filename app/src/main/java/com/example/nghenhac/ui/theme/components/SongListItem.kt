package com.example.nghenhac.ui.theme.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    menuItems: List<MenuItemData> = emptyList(),
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false
) {
    Spacer(modifier = Modifier.height(2.dp))

    // Màu chữ: Nếu đang phát thì dùng màu Primary, không thì dùng màu thường
    val titleColor = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val titleWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium

    Surface(
        shape = ShapeDefaults.Medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(60.dp)
            .clip(ShapeDefaults.Medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. ẢNH BÌA (Bên trái - Giữ nguyên, không còn animation đè lên)
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
                    .width(60.dp)
                    .fillMaxHeight()
                    .clip(ShapeDefaults.Medium)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. THÔNG TIN (Ở giữa)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = titleWeight,
                    color = titleColor,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = song.artistName,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.basicMarquee()
                )
            }

            // 3. PHẦN BÊN PHẢI (Animation + Menu)

            // --- THÊM ANIMATION Ở ĐÂY ---
            if (isCurrentSong) {
                Spacer(modifier = Modifier.width(8.dp))

                // Hiển thị sóng nhạc
                AudioWaveAnimation(
                    isPlaying = isPlaying,
                    barColor = MaterialTheme.colorScheme.primary, // Màu xanh chủ đạo
                    barWidth = 3.dp,
                    maxHeight = 16.dp
                )
            }
            // ----------------------------

            // Menu 3 chấm
            if (menuItems.isNotEmpty()) {
                MoreOptionsButton(
                    menuItems = menuItems,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}