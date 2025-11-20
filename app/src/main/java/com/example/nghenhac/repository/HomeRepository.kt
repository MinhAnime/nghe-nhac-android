package com.example.nghenhac.repository

import com.example.nghenhac.data.AddSongRequest
import com.example.nghenhac.data.CreatePlaylistRequest
import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.RenamePlaylistRequest
import com.example.nghenhac.data.SearchResponseDTO
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.data.TokenHolder
import com.example.nghenhac.network.ApiService
import kotlinx.coroutines.delay

class HomeRepository(private  val apiService: ApiService) {


    private suspend fun ensureToken() {
        if (TokenHolder.token.isNullOrBlank()) {
            delay(500)
            if (TokenHolder.token.isNullOrBlank()) {
                throw Exception("Token chưa sẵn sàng. Vui lòng đăng nhập lại.")
            }
        }
    }
    suspend fun getMyPlaylists(page: Int = 0): List<PlaylistSummaryDTO> {
        return apiService.getMyPlaylists(page = page)
    }
    suspend fun getAllSongs(page: Int = 0): List<SongResponseDTO> {
        return apiService.getAllSongs(page = page)
    }

    suspend fun getPlaylistDetails(playlistId: Long): PlaylistDetailDTO {
        return apiService.getPlaylistDetails(playlistId)
    }

    suspend fun createPlaylist(name: String): PlaylistSummaryDTO {
        val request = CreatePlaylistRequest(name)
        return apiService.createPlaylist(request)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        apiService.addSongToPlaylist(playlistId, AddSongRequest(songId))
    }
    suspend fun getSongsInPlaylist(playlistId: Long, page: Int): List<SongResponseDTO> {
        return apiService.getSongsInPlaylist(playlistId, page, size = 20)
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

    suspend fun deletePlaylist(playlistId: Long) {
        apiService.deletePlaylist(playlistId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        apiService.removeSongFromPlaylist(playlistId, songId)
    }
    suspend fun search(query: String): SearchResponseDTO {
        return apiService.search(query)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val request = RenamePlaylistRequest(newName)
        apiService.renamePlaylist(playlistId, request)
    }

    suspend fun togglePrivacy(playlistId: Long) {
        apiService.togglePrivacy(playlistId)
    }


}