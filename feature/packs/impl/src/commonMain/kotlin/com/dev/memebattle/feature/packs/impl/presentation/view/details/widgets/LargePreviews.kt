package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.dev.memebattle.core.network.utils.normalizeMediaUrl
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckAccent
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckBorder
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckTextPri
import com.dev.memebattle.feature.packs.impl.presentation.view.details.SituationAccents
import com.dev.memebattle.core.ui.components.pack.CardBack

@Composable
internal fun LargeCardPreview(model: Any?, idx: Int) {
    val scale by animateFloatAsState(1f, spring(0.65f, 350f), label = "ps")
    Card(
        modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DeckBorder),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(12.dp),
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            if (model != null && (model !is String || model.isNotBlank())) {
                val formattedModel = if (model is String) normalizeMediaUrl(model) else model
                SubcomposeAsyncImage(
                    model = formattedModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = DeckAccent,
                                strokeWidth = 3.dp
                            )
                        }
                    },
                    error = {
                        CardBack(modifier = Modifier.fillMaxSize())
                    }
                )
            }
        }
    }
}

@Composable
internal fun LargeSituationPreview(text: String, idx: Int) {
    val accent = SituationAccents[idx % SituationAccents.size]
    val textLength = text.length
    val (fontSize, lineHeight) = when {
        textLength < 50  -> 18.sp to 26.sp
        textLength < 110 -> 15.sp to 22.sp
        textLength < 180 -> 13.sp to 18.sp
        else             -> 11.sp to 15.sp
    }
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
        elevation = CardDefaults.cardElevation(12.dp),
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Box(Modifier.size(120.dp).background(Brush.radialGradient(listOf(accent.copy(0.12f), Color.Transparent)), CircleShape))
            Text(text.ifBlank { "—" }, color = DeckTextPri, fontSize = fontSize, fontWeight = FontWeight.SemiBold, lineHeight = lineHeight, textAlign = TextAlign.Center)
        }
    }
}
