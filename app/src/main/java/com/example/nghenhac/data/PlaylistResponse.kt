package com.example.nghenhac.data

data class PlaylistResponse(
    val id: Long, // UUID
    val name: String,
    val ownerUsername: String,
    val songs: List<SongResponseDTO>
)
