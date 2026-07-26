package com.dev.memebattle.feature.home.impl.presentation.view.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.play
import com.dev.memebattle.core.localization.store
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.component.menu.HomeMenuComponent
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.view.create.CreateLobbyView

@Composable
fun HomeMenuView(
    component: HomeMenuComponent,
    detailsComponent: CreateLobbyComponent? = null
) {
    val state by component.state.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1035),
            Color(0xFF0F081D),
            Color(0xFF08040F)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val constraintsMaxWidth = maxWidth
            val constraintsMaxHeight = maxHeight
            val isWide = constraintsMaxWidth >= 600.dp
            val horizontalPadding = if (isWide) 32.dp else 20.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Title (fades out when lobby opens) ────────────────────────
                androidx.compose.animation.AnimatedVisibility(
                    visible = !state.isLobbyListVisible,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + androidx.compose.animation.slideOutVertically { -it / 2 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MemeBattle",
                            fontSize = if (isWide) 42.sp else 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "The Ultimate Card Game",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB0A2C7),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
                        )
                    }
                }

                // ── Morphing Button / Panel ───────────────────────────────────
                MorphingPlayButton(
                    isExpanded = state.isLobbyListVisible,
                    isWide = isWide,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    onPlayClick = { component.onIntent(HomeMenuStore.Intent.OnPlayClicked) },
                    lobbyContent = {
                        if (detailsComponent != null) {
                            CreateLobbyView(
                                component = detailsComponent,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LobbiesWidget(
                                state = state,
                                onBack = { component.onIntent(HomeMenuStore.Intent.OnCloseLobbiesClicked) },
                                onCreateLobby = { component.onIntent(HomeMenuStore.Intent.OnCreateLobbyClicked) }
                            )
                        }
                    }
                )

                // ── Store button (fades out when lobby opens) ─────────────────
                Spacer(modifier = Modifier.height(20.dp))
                androidx.compose.animation.AnimatedVisibility(
                    visible = !state.isLobbyListVisible,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 }
                ) {
                    OutlinedButton(
                        onClick = { component.onIntent(HomeMenuStore.Intent.OnStoreClicked) },
                        modifier = Modifier
                            .width(if (isWide) 300.dp else constraintsMaxWidth * 0.75f)
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB0A2C7)
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = Color(0xFFB0A2C7).copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(Res.string.store),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MorphingPlayButton(
    isExpanded: Boolean,
    isWide: Boolean,
    constraintsMaxWidth: androidx.compose.ui.unit.Dp,
    constraintsMaxHeight: androidx.compose.ui.unit.Dp,
    onPlayClick: () -> Unit,
    lobbyContent: @Composable () -> Unit
) {
    // Adaptive sizes
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

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isExpanded) Color(0xFF1A1035).copy(alpha = 0.95f)
        else Color(0xFF6C47FF),
        animationSpec = tween(400),
        label = "bgColor"
    )

    // Border animation
    val borderWidth by animateDpAsState(
        targetValue = if (isExpanded) 2.dp else 0.dp,
        animationSpec = tween(300),
        label = "borderWidth"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .clip(RoundedCornerShape(if (isExpanded) 20.dp else 20.dp))
            .background(
                if (!isExpanded && isHovered) Color(0xFF5A3BD6)
                else backgroundColor
            )
            .then(
                if (borderWidth > 0.dp) Modifier.border(
                    BorderStroke(borderWidth, Color(0xFF6C47FF)),
                    RoundedCornerShape(20.dp)
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
        // Crossfade content
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith 
                fadeOut(animationSpec = tween(150))
            },
            label = "buttonContent"
        ) { expanded ->
            if (expanded) {
                // Lobby panel content (Lobbies list OR Create Lobby)
                lobbyContent()
            } else {
                // Play button content
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

@Composable
fun LobbiesWidget(
    state: HomeMenuStore.State,
    onBack: () -> Unit,
    onCreateLobby: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Available Lobbies",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lobby list with staggered animation
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.lobbies.isEmpty()) {
                Text(
                    text = "No active lobbies found.\nCreate one and invite friends!",
                    color = Color(0xFFB0A2C7),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.lobbies) { index, lobby ->
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(300, delayMillis = index * 60)
                            ) + slideInVertically(
                                animationSpec = tween(300, delayMillis = index * 60),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2452)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Lobby: ${lobby.id.take(8)}...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Mode: ${lobby.mode}",
                                            color = Color(0xFFB0A2C7),
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Players: ${lobby.playersCount}",
                                            color = Color(0xFFB0A2C7),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FAB with simple fade-in animation (no spring/bouncy effect)
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300, delayMillis = 200)),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = onCreateLobby,
                    modifier = Modifier.padding(8.dp),
                    containerColor = Color(0xFF7C5DFA),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Lobby")
                }
            }
        }
    }
}
