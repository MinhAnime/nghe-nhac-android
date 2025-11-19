package com.example.nghenhac.network

import com.example.nghenhac.data.AuthEvents
import com.example.nghenhac.data.TokenHolder
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenHolder.token
        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401 || response.code == 403) {

            TokenHolder.token = null

            runBlocking {
                AuthEvents.emitLogout()
            }
        }

        return response
    }
}