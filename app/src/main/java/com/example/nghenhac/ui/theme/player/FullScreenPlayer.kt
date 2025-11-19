package com.example.nghenhac.ui.theme.player

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nghenhac.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayer(
    playerState: PlayerState,
    onClose: () -> Unit,
    onPlayPauseClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onShuffleClicked: () -> Unit,
    onRepeatClicked: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val nowPlaying = playerState.nowPlaying ?: return
    val totalDuration = playerState.totalDuration
    val currentPosition = playerState.currentPosition
    val dominantColor = playerState.dominantColor?: MaterialTheme.colorScheme.surface
    val solidBackgroundColor = dominantColor.copy(alpha = 0.85f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(color = solidBackgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.ExpandMore, contentDescription = "Đóng")
        }

        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(nowPlaying.artworkUri)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .build(),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(ShapeDefaults.Large)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = nowPlaying.title?.toString() ?: "Không có tiêu đề",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = nowPlaying.artist?.toString() ?: "Không rõ nghệ sĩ",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(24.dp))

        var isSeeking by remember { mutableStateOf(false) }
        var seekPosition by remember { mutableFloatStateOf(0f) }
        val interactionSource = remember { MutableInteractionSource() }

        val sliderPosition = if (isSeeking) {
            seekPosition
        } else {
            if (totalDuration > 0) {
                currentPosition.toFloat() / totalDuration.toFloat()
            } else { 0f }
        }.coerceIn(0f, 1f)

        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                isSeeking = true
                seekPosition = newPosition
            },
            onValueChangeFinished = {
                onSeek(seekPosition)
                isSeeking = false
            },
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    thumbSize = DpSize(12.dp, 12.dp)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    modifier = Modifier.height(4.dp),
                    sliderState = sliderState
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = formatTime(totalDuration),
                style = MaterialTheme.typography.labelSmall
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(), // Mở rộng hết cỡ
            horizontalArrangement = Arrangement.SpaceEvenly, // Dàn đều
            verticalAlignment = Alignment.CenterVertically
        ) {
            // NÚT SHUFFLE (MIX)
            IconButton(onClick = onShuffleClicked) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Trộn bài",
                    modifier = Modifier.size(32.dp),
                    // Đổi màu nếu Shuffle đang BẬT
                    tint = if (playerState.isShuffleOn) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            // NÚT PREVIOUS
            IconButton(onClick = onPreviousClicked) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Trước", modifier = Modifier.size(48.dp))
            }

            // NÚT PLAY/PAUSE (TO)
            FilledIconButton(
                onClick = onPlayPauseClicked,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(40.dp)
                )
            }

            // NÚT NEXT
            IconButton(onClick = onNextClicked) {
                Icon(Icons.Default.SkipNext, contentDescription = "Sau", modifier = Modifier.size(48.dp))
            }

            // NÚT REPEAT
            IconButton(onClick = onRepeatClicked) {
                val (icon, tint) = when (playerState.repeatMode) {
                    Player.REPEAT_MODE_ONE -> Pair(Icons.Default.RepeatOne, MaterialTheme.colorScheme.primary)
                    Player.REPEAT_MODE_ALL -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.primary)
                    else -> Pair(Icons.Default.Repeat, Color.Gray)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Lặp lại",
                    modifier = Modifier.size(32.dp),
                    tint = tint
                )
            }
        }
    }
}