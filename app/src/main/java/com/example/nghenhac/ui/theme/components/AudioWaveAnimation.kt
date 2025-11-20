package com.example.nghenhac.ui.theme.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 16.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Tạo 3 thanh sóng với tốc độ khác nhau
        AnimatingBar(isPlaying, 0, 500, maxHeight, barWidth, barColor)
        AnimatingBar(isPlaying, 100, 400, maxHeight, barWidth, barColor)
        AnimatingBar(isPlaying, 200, 600, maxHeight, barWidth, barColor)
    }
}

@Composable
fun AnimatingBar(
    isPlaying: Boolean,
    delay: Int,
    duration: Int,
    maxHeight: Dp,
    width: Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Wave")

    // Nếu đang phát -> Animate từ 0.2f đến 1f
    // Nếu dừng -> Giữ nguyên ở mức thấp (0.2f)
    val heightScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, delayMillis = delay, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BarHeight"
        )
    } else {
        // Trả về giá trị tĩnh khi pause
        remember { mutableFloatStateOf(0.3f) }
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(maxHeight * heightScale) // Chiều cao thay đổi theo animation
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}