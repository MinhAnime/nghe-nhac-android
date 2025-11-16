package com.example.nghenhac.repository

import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.network.ApiService

class HomeRepository(private  val apiService: ApiService) {
    suspend fun getMyPlaylists(): List<PlaylistSummaryDTO> {
        return apiService.getMyPlaylists()
    }

    suspend fun getPlaylistDetails(playlistId: Long): PlaylistDetailDTO {
        return apiService.getPlaylistDetails(playlistId)
    }

}