package com.example.nghenhac.ui.theme.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nghenhac.data.PlaylistSummaryDTO

@Composable
fun PlaylistCard(
    playlist: PlaylistSummaryDTO,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp)
    ) {
        Column {
            PlaylistCoverGrid(
                images = playlist.thumbnails,
                modifier = Modifier
                    .fillMaxWidth()
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .basicMarquee()
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