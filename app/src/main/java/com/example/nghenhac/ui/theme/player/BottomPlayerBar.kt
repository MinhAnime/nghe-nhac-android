package com.example.nghenhac.ui.theme.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nghenhac.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomPlayerBar(
    playerState: PlayerState,
    onPlayPauseClicked: () -> Unit,
    onBarClicked: () -> Unit
) {
    val nowPlaying = playerState.nowPlaying ?: return

    val containerColor = playerState.dominantColor
        ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = contentColorFor(containerColor)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onBarClicked() },
        shape = ShapeDefaults.Medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = nowPlaying.artworkUri,
                label = "AlbumArtAnimation"
            ) { artworkUri ->
                AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUri)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build(),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ShapeDefaults.Small)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center

            ) {
                AnimatedContent(
                    targetState = nowPlaying.title,
                    transitionSpec = {
                        slideInVertically { height -> height } togetherWith
                                slideOutVertically { height -> -height }
                    },
                    label = "TitleAnimation"
                ) { title ->
                    Text(
                        text = title?.toString() ?: "Không có tiêu đề",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor
                    )
                }
                AnimatedContent(
                    targetState = nowPlaying.artist,
                    transitionSpec = {
                        slideInVertically { height -> height } togetherWith
                                slideOutVertically { height -> -height }
                    },
                    label = "ArtistAnimation"
                ) { artist ->
                    Text(
                        text = artist?.toString() ?: "Không rõ nghệ sĩ",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onPlayPauseClicked) {
                Icon(
                    imageVector = if (playerState.isPlaying)
                        Icons.Filled.Pause
                    else
                        Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(32.dp),
                    tint = contentColor
                )
            }
        }
    }
}