package com.dev.memebattle.feature.home.impl.presentation.view.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.play
import org.jetbrains.compose.resources.stringResource

@Composable
fun MorphingPlayButton(
    isExpanded: Boolean,
    isWide: Boolean,
    constraintsMaxWidth: Dp,
    constraintsMaxHeight: Dp,
    onPlayClick: () -> Unit,
    lobbyContent: @Composable () -> Unit
) {
    val collapsedWidth = if (isWide) 300.dp else constraintsMaxWidth * 0.75f
    val collapsedHeight = 64.dp
    val expandedWidth = if (isWide) 520.dp else constraintsMaxWidth * 0.92f
    val expandedHeight = if (isWide) 640.dp else constraintsMaxHeight * 0.78f

    val buttonWidth by animateDpAsState(
        targetValue = if (isExpanded) expandedWidth else collapsedWidth,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "buttonWidth"
    )
    val buttonHeight by animateDpAsState(
        targetValue = if (isExpanded) expandedHeight else collapsedHeight,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "buttonHeight"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isExpanded) Color(0xFF1E1035).copy(alpha = 0.98f)
        else Color(0xFF6C47FF),
        animationSpec = tween(400),
        label = "bgColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isExpanded) 1.5.dp else 0.dp,
        animationSpec = tween(300),
        label = "borderWidth"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (!isExpanded && isHovered) Color(0xFF5A3BD6)
                else backgroundColor
            )
            .then(
                if (borderWidth > 0.dp) Modifier.border(
                    BorderStroke(borderWidth, Color(0xFF7C5DFA).copy(alpha = 0.5f)),
                    RoundedCornerShape(24.dp)
                ) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isExpanded,
                onClick = onPlayClick
            )
            .padding(if (isExpanded) 16.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith 
                fadeOut(animationSpec = tween(150))
            },
            label = "buttonContent"
        ) { expanded ->
            if (expanded) {
                lobbyContent()
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).padding(end = 8.dp),
                        tint = Color.White
                    )
                    Text(
                        text = stringResource(Res.string.play),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
