package com.example.nghenhac.data

import com.google.gson.annotations.SerializedName

data class SearchResponseDTO(
    @SerializedName("songs")
    val songs: List<SongResponseDTO> = emptyList(),

    @SerializedName("playlists")
    val playlists: List<PlaylistSummaryDTO> = emptyList()
)