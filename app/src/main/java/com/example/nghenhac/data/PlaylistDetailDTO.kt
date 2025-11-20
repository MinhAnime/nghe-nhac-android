package com.example.nghenhac.data

data class PlaylistDetailDTO(
    val id: Long,
    val name: String,
    val ownerUsername: String,
    val isPublic: Boolean = false,
    val songs: List<SongResponseDTO>
)
