package com.example.nghenhac.network

import com.example.nghenhac.data.LoginRequest
import com.example.nghenhac.data.LoginResponse
import com.example.nghenhac.data.PlaylistDetailDTO
import com.example.nghenhac.data.PlaylistSummaryDTO
import com.example.nghenhac.data.RegisterRequest
import com.example.nghenhac.data.UserResponse
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
}