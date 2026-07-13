package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckAccent
import kotlinx.coroutines.launch

@Composable
internal fun SlotMachineLever(
    onFlipUp: () -> Unit,
    onFlipDown: () -> Unit,
    canUp: Boolean,
    canDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val leverOffset = remember { Animatable(0f) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { 24.dp.toPx() }

    val leverColor by animateColorAsState(
        targetValue = when {
            leverOffset.value < -thresholdPx && canUp -> DeckAccent
            leverOffset.value > thresholdPx && canDown -> DeckAccent
            else -> Color(0xFF4A3880)
        },
        animationSpec = tween(150), label = "lc",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2E2452))
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer { translationY = leverOffset.value }
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(leverColor)
                .pointerInput(canUp, canDown) {
                    val maxTravelPx = 36.dp.toPx()
                    val triggerPx = 24.dp.toPx()
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                when {
                                    leverOffset.value < -triggerPx && canUp -> onFlipUp()
                                    leverOffset.value > triggerPx && canDown -> onFlipDown()
                                }
                                leverOffset.animateTo(0f, spring(dampingRatio = 0.4f, stiffness = 500f))
                            }
                        },
                        onVerticalDrag = { _, dy ->
                            coroutineScope.launch {
                                leverOffset.snapTo((leverOffset.value + dy).coerceIn(-maxTravelPx, maxTravelPx))
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(3) {
                    Box(Modifier.width(16.dp).height(2.dp).background(Color.White.copy(0.5f), RoundedCornerShape(1.dp)))
                }
            }
        }
    }
}
