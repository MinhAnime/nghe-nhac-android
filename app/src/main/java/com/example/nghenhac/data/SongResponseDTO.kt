package com.example.nghenhac.data

data class SongResponseDTO(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val artistName: String,
    val coverArtUrl: String?
)
