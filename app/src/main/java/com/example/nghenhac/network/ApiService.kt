package com.example.nghenhac.network

import com.example.nghenhac.data.AddSongRequest
import com.example.nghenhac.data.CreatePlaylistRequest
import com.example.nghenhac.data.LoginRequest
import com.example.nghenhac.data.LoginResponse
import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.RegisterRequest
import com.example.nghenhac.data.SongResponseDTO
import com.example.nghenhac.data.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/api/v1/playlists/my-playlists")
    suspend fun getMyPlaylists(): List<PlaylistSummaryDTO>

    @GET("/api/v1/playlists/{playlistId}")
    suspend fun getPlaylistDetails(@Path("playlistId") playlistId: Long): PlaylistDetailDTO

    @GET("/api/v1/songs/stream/{songId}")
    suspend fun getSongStreamUrl(@Path("songId") songId: Long): Response<Unit>

    @GET("/api/v1/songs")
    suspend fun getAllSongs(): List<SongResponseDTO>

    @POST("/api/v1/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): PlaylistSummaryDTO

    @POST("/api/v1/playlists/{playlistId}/songs")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Body request: AddSongRequest
    ): PlaylistDetailDTO
}