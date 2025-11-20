package com.example.nghenhac.data

import com.google.gson.annotations.SerializedName

data class PlaylistSummaryDTO(
    val id: Long,
    val name: String,
    val ownerUsername: String,
    val isPublic: Boolean = false,
    val thumbnailUrl: String? = null,
    @SerializedName("thumbnails")
    private val _thumbnails: List<String>? = null
){
    val thumbnails: List<String>
        get() = _thumbnails ?: emptyList()
}