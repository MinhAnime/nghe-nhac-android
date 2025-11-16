package com.example.nghenhac.ui.theme.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nghenhac.data.LoginRequest
import com.example.nghenhac.data.RegisterRequest
import com.example.nghenhac.data.TokenManager
import com.example.nghenhac.network.RetrofitClient
import com.example.nghenhac.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registerSuccess: Boolean = false
)


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginEvent = MutableSharedFlow<Unit>()
    val loginEvent = _loginEvent.asSharedFlow()

    init {
        // Khởi tạo mọi thứ
        val apiService = RetrofitClient.create(application.applicationContext)
        val tokenManager = TokenManager(application.applicationContext)
        authRepository = AuthRepository(apiService, tokenManager)
    }

    fun onLoginClicked() {
        // Bắt đầu tải
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null // Reset lỗi cũ
        )

        viewModelScope.launch {
            try {
                val request = LoginRequest(username = username, pass = password)

                authRepository.login(request)

                _uiState.value = AuthUiState(isLoading = false) // Tắt loading
                _loginEvent.emit(Unit)

            } catch (e: Exception) {
                // Thất bại
                _uiState.value = AuthUiState(error = e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun onRegisterClicked() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null // Reset lỗi cũ
        )

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    pass = password
                )
                authRepository.register(request)

                // Đăng ký thành công, báo cho UI
                _uiState.value = AuthUiState(registerSuccess = true)

            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message ?: "Lỗi đăng ký")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    fun clearRegisterSuccess() {
        _uiState.value = _uiState.value.copy(registerSuccess = false)
    }

}