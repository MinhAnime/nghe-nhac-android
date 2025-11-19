package com.example.nghenhac.ui.theme.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Data class để định nghĩa một mục trong menu
data class MenuItemData(
    val text: String,
    val icon: @Composable (() -> Unit)? = null, // Icon là tùy chọn
    val onClick: () -> Unit
)

@Composable
fun MoreOptionsButton(
    menuItems: List<MenuItemData>, // Danh sách các lựa chọn
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        // 1. Nút 3 chấm
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Tùy chọn thêm",
                tint = contentColor
            )
        }

        // 2. Menu xổ xuống
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.text) },
                    leadingIcon = item.icon,
                    onClick = {
                        expanded = false // Đóng menu
                        item.onClick()   // Thực hiện hành động
                    }
                )
            }
        }
    }
}