package com.example.nghenhac.data

data class PlaylistDetailDTO(
    val id: Long,
    val name: String,
    val ownerUsername: String,
    val songs: List<SongResponseDTO>
)
