package com.example.nghenhac.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    // Cache token trong RAM
    @Volatile
    private var cachedToken: String? = null

    // Lấy token đồng bộ cho Interceptor
    fun getToken(): String? = cachedToken

    // Chỉ gọi 1 lần khi App khởi động
    suspend fun loadToken() {
        cachedToken = context.dataStore.data
            .map { it[TOKEN_KEY] }
            .firstOrNull()
    }

    // Lưu token và update RAM
    suspend fun saveToken(token: String) {
        cachedToken = token
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    // Xóa token
    suspend fun deleteToken() {
        cachedToken = null
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}
