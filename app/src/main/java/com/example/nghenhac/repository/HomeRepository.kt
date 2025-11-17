package com.example.nghenhac.repository

import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.network.ApiService

class HomeRepository(private  val apiService: ApiService) {
    suspend fun getMyPlaylists(): List<PlaylistSummaryDTO> {
        return apiService.getMyPlaylists()
    }
    suspend fun getAllSongs(): List<SongResponseDTO> {
        return apiService.getAllSongs()
    }

    suspend fun getPlaylistDetails(playlistId: Long): PlaylistDetailDTO {
        return apiService.getPlaylistDetails(playlistId)
    }
    suspend fun getSongStreamUrl(songId: Long): String {
        val response = apiService.getSongStreamUrl(songId)

        // Kiểm tra xem có phải là Redirect (302)
        if (response.isSuccessful.not() && response.code() in 300..399) {
            // Lấy header "Location"
            val locationHeader = response.headers()["Location"]
            if (locationHeader.isNullOrBlank()) {
                throw Exception("Không tìm thấy link nhạc (Location header bị rỗng)")
            }
            return locationHeader
        } else {
            // Nếu API trả về 200 OK (hoặc lỗi 404, 500) là BẤT THƯỜNG
            throw Exception("Lỗi khi lấy link nhạc, server không redirect. Code: ${response.code()}")
        }
    }


}