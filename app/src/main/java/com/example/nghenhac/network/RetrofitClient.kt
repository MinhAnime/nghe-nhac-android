package com.example.nghenhac.network

import android.content.Context
import com.example.nghenhac.data.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.14:8080/"

    fun create(context: Context): ApiService {

        // Khởi tạo TokenManager
        val tokenManager = TokenManager(context.applicationContext)

        // Tạo Interceptor log
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Tạo Client, gắn AuthInterceptor và Logging
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager)) // Gắn bộ chặn token
            .addInterceptor(logging) // Gắn bộ log
            .followRedirects(false)
            .followSslRedirects(false)
            .build()

        // Tạo Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}