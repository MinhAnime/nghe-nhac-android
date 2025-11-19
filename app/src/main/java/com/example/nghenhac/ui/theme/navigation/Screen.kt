package com.example.nghenhac.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox // Icon thêm playlist
import androidx.compose.material.icons.filled.ExitToApp // Icon đăng xuất
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    object Search : Screen("search", "Tìm kiếm", Icons.Default.Search)

    object AddPlaylist : Screen("add_playlist", "Tạo Playlist", Icons.Default.AddBox)

    object Logout : Screen("logout", "Đăng xuất", Icons.Default.ExitToApp)
}