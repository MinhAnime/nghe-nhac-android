package com.example.nghenhac.ui.theme.player // (Hoặc package của bạn)

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    // --- 1. MÀU SẮC ---
    // Lấy màu chủ đạo từ ảnh bìa (nếu có), nếu không dùng màu xám tối (giống Spotify)
    val backgroundColor = playerState.dominantColor ?: Color(0xFF2C2C2C)

    // Màu chữ luôn là màu sáng để nổi trên nền tối
    val contentColor = Color.White

    // --- 2. TÍNH TIẾN TRÌNH (PROGRESS) ---
    val progress = if (playerState.totalDuration > 0) {
        playerState.currentPosition.toFloat() / playerState.totalDuration.toFloat()
    } else 0f

    // --- 3. GIAO DIỆN THẺ NỔI (CARD) ---
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Tạo khoảng hở để nó "nổi" lên
            .height(64.dp) // Chiều cao cố định giống Spotify
            .clickable { onBarClicked() },
        shape = RoundedCornerShape(8.dp), // Bo góc 8dp
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Dùng Box để xếp chồng Thanh tiến trình lên dưới cùng
        Box(modifier = Modifier.fillMaxSize()) {

            // --- NỘI DUNG CHÍNH (Hàng ngang) ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp), // Padding bên trong thẻ
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A. ẢNH BÌA
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
                        .size(48.dp) // Kích thước ảnh
                        .clip(RoundedCornerShape(4.dp)) // Bo góc ảnh nhẹ
                )

                Spacer(modifier = Modifier.width(12.dp))

                // B. TÊN BÀI HÁT & CA SĨ (Ở giữa)
                Column(
                    modifier = Modifier.weight(1f), // Chiếm hết không gian còn lại
                    verticalArrangement = Arrangement.Center
                ) {
                    // Tên bài hát (Đậm, Trắng)
                    AnimatedContent(
                        targetState = nowPlaying.title,
                        label = "TitleAnimation"
                    ) { title ->
                        Text(
                            text = title?.toString() ?: "Không có tiêu đề",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            color = contentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                    }

                    // Tên ca sĩ (Nhạt hơn)
                    AnimatedContent(
                        targetState = nowPlaying.artist,
                        label = "ArtistAnimation"
                    ) { artist ->
                        Text(
                            text = artist?.toString() ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(),
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }

                // C. NÚT PLAY/PAUSE (Bên phải)
                IconButton(onClick = onPlayPauseClicked) {
                    Icon(
                        imageVector = if (playerState.isPlaying)
                            Icons.Filled.Pause
                        else
                            Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = contentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}