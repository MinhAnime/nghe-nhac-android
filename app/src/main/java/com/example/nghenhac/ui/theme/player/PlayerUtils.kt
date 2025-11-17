package com.example.nghenhac.ui.theme.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.nghenhac.data.SongResponseDTO
import java.util.concurrent.TimeUnit

fun formatTime(milliseconds: Long): String {
    if (milliseconds < 0) return "00:00"

    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            TimeUnit.MINUTES.toSeconds(minutes)

    return String.format("%02d:%02d", minutes, seconds)
}

fun convertSongsToMediaItems(songs: List<SongResponseDTO>): List<MediaItem> {
    return songs.map { song ->
        // 1. Tạo Metadata (Tên, Nghệ sĩ, Ảnh)
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artistName)

        song.coverArtUrl?.let { url ->
            if (url.isNotBlank()) {
                metadataBuilder.setArtworkUri(Uri.parse(url))
            }
        }

        MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }
}