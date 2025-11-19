package com.example.nghenhac.data

import com.google.gson.annotations.SerializedName

data class PlaylistSummaryDTO(
    val id: Long,
    val name: String,
    val ownerUsername: String,
    @SerializedName("thumbnails")
    private val _thumbnails: List<String>? = null
){
    val thumbnails: List<String>
        get() = _thumbnails ?: emptyList()
}