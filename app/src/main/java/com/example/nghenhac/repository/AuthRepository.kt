package com.example.nghenhac.repository

import com.example.nghenhac.data.LoginRequest
import com.example.nghenhac.data.LoginResponse
import com.example.nghenhac.data.RegisterRequest
import com.example.nghenhac.data.TokenHolder
import com.example.nghenhac.data.TokenManager
import com.example.nghenhac.data.UserResponse

import com.example.nghenhac.network.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = apiService.login(request)
        tokenManager.saveToken(response.token)
        TokenHolder.token = response.token
        return response
    }

    suspend fun register(request: RegisterRequest): UserResponse {
        return apiService.register(request)
    }

    suspend fun logout() {
        tokenManager.deleteToken()
        TokenHolder.token = null
    }

}