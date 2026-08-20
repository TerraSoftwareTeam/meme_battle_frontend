package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.dev.memebattle.core.network.utils.normalizeMediaUrl
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckAccent
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckTextPri
import com.dev.memebattle.core.ui.components.pack.CardBack

@Composable
internal fun MemeCardFace(model: Any?, isSelected: Boolean) {
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
                            modifier = Modifier.size(20.dp),
                            color = DeckAccent,
                            strokeWidth = 2.dp
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

@Composable
internal fun SituationCardFace(text: String, accent: Color, isSelected: Boolean) {
    val textLength = text.length
    val (fontSize, lineHeight) = when {
        textLength < 40  -> 12.sp to 16.sp
        textLength < 80  -> 10.sp to 14.sp
        textLength < 140 -> 9.sp to 12.sp
        else             -> 8.sp to 10.sp
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0B1E)),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(60.dp).background(Brush.radialGradient(listOf(accent.copy(0.18f), Color.Transparent)), CircleShape))
        Text(text, color = DeckTextPri, fontSize = fontSize, lineHeight = lineHeight, textAlign = TextAlign.Center,
            maxLines = 8, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(8.dp))
    }
}
