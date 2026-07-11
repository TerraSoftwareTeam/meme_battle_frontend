package com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
internal fun FileImageCard(
    file: PlatformFile,
    onRemove: () -> Unit
) {
    var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(file) {
        bytes = file.readBytes()
    }

    Box(
        modifier = Modifier
            .width(100.dp)
            .aspectRatio(3f / 4f)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (bytes != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0820)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = bytes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AccentColor,
                    strokeWidth = 2.dp
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
