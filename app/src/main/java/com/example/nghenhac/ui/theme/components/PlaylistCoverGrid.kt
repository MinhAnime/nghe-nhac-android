package com.example.nghenhac.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nghenhac.R

@Composable
fun PlaylistCoverGrid(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        when (images.size) {
            0 -> PlaceholderImage()
            1 -> FullImage(images[0])
            2 -> TwoImages(images)
            3 -> ThreeImages(images)
            else -> FourImages(images.take(4))
        }
    }
}

@Composable
fun SingleImageItem(url: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun PlaceholderImage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- CÁC LAYOUT ---

@Composable
fun FullImage(url: String) {
    SingleImageItem(url)
}

@Composable
fun TwoImages(urls: List<String>) {
    Row(Modifier.fillMaxSize()) {
        SingleImageItem(urls[0], Modifier.weight(1f))
        SingleImageItem(urls[1], Modifier.weight(1f))
    }
}

@Composable
fun ThreeImages(urls: List<String>) {
    Row(Modifier.fillMaxSize()) {
        // Ảnh trái (lớn)
        SingleImageItem(urls[0], Modifier.weight(1f))
        // Cột phải (2 ảnh nhỏ)
        Column(Modifier.weight(1f)) {
            SingleImageItem(urls[1], Modifier.weight(1f))
            SingleImageItem(urls[2], Modifier.weight(1f))
        }
    }
}

@Composable
fun FourImages(urls: List<String>) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.weight(1f)) {
            SingleImageItem(urls[0], Modifier.weight(1f))
            SingleImageItem(urls[1], Modifier.weight(1f))
        }
        Row(Modifier.weight(1f)) {
            SingleImageItem(urls[2], Modifier.weight(1f))
            SingleImageItem(urls[3], Modifier.weight(1f))
        }
    }
}