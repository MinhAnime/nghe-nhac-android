package com.example.nghenhac.ui.theme.player

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun extractDominantColor(
    context: Context,
    imageUrl: String,
): Color? {
    return withContext(Dispatchers.IO) {
        try {
            // 1. Dùng Coil (trình tải ảnh) để lấy ảnh
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Cần thiết để Palette đọc Bitmap
                .scale(Scale.FILL)
                .build()

            // 2. Lấy kết quả là một Bitmap (ảnh)
            val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
            val bitmap = result?.toBitmap() ?: return@withContext null

            // 3. Dùng Palette (bản ktx) để phân tích màu
            // 'generate()' là một suspend fun (hỗ trợ coroutine)
            val palette = Palette.from(bitmap).generate()

            // 4. Lấy màu "Vibrant" (Sôi động) hoặc "Muted" (Trầm)
            // (Thứ tự ưu tiên: DarkVibrant > Vibrant > Muted)
            val swatch = palette.darkVibrantSwatch
                ?: palette.vibrantSwatch
                ?: palette.mutedSwatch

            // 5. Trả về màu (Compose Color)
            swatch?.rgb?.let { Color(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}