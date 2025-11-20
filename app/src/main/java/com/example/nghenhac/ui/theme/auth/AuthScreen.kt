package com.example.nghenhac.ui.theme.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class AuthTab {
    LOGIN, REGISTER
}

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {

    val viewModel: AuthViewModel = viewModel()


    val uiState by viewModel.uiState.collectAsState()


    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    val context = LocalContext.current

    // --- XỬ LÝ CÁC SỰ KIỆN TỪ VIEWMODEL ---

    // 1. Xử lý khi đăng nhập thành công
    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect {
            // Khi nhận được sự kiện, mới điều hướng
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    // 2. Xử lý khi đăng ký thành công
    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
            selectedTab = AuthTab.LOGIN
            viewModel.clearRegisterSuccess()
        }
    }

    // 3. Xử lý khi có lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = if (selectedTab == AuthTab.LOGIN) "Đăng nhập" else "Đăng ký",
                style = MaterialTheme.typography.headlineLarge, // M3 Typography
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- TAB (Login / Register) ---
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == AuthTab.LOGIN,
                    onClick = { selectedTab = AuthTab.LOGIN },
                    text = { Text("Đăng nhập") }
                )
                Tab(
                    selected = selectedTab == AuthTab.REGISTER,
                    onClick = { selectedTab = AuthTab.REGISTER },
                    text = { Text("Đăng ký") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- CÁC TRƯỜNG NHẬP LIỆU ---

            // Username
            OutlinedTextField(
                value = viewModel.username,
                onValueChange = { viewModel.username = it },
                label = { Text("Tên đăng nhập") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email (Chỉ hiển thị ở tab Register)
            if (selectedTab == AuthTab.REGISTER) {
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Password
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Mật khẩu") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- NÚT BẤM VÀ LOADING ---
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    // Button (Sử dụng phong cách "Expressive" của M3)
                    Button(
                        onClick = {
                            if (selectedTab == AuthTab.LOGIN) {
                                viewModel.onLoginClicked()
                            } else {
                                viewModel.onRegisterClicked()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = ShapeDefaults.Medium // Bo góc (M3)
                    ) {
                        Text(if (selectedTab == AuthTab.LOGIN) "ĐĂNG NHẬP" else "ĐĂNG KÝ")
                    }
                }
            }
        }
    }
}