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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import com.dev.memebattle.core.ui.share.rememberLinkSharer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.play
import com.dev.memebattle.core.localization.store
import com.dev.memebattle.core.localization.home_join_dialog_title
import com.dev.memebattle.core.localization.home_join_dialog_description
import com.dev.memebattle.core.localization.home_join_dialog_nickname_label
import com.dev.memebattle.core.localization.home_join_dialog_nickname_hint
import com.dev.memebattle.core.localization.home_join_dialog_confirm
import com.dev.memebattle.core.localization.home_join_dialog_cancel
import com.dev.memebattle.core.localization.home_lobbies_available_title
import com.dev.memebattle.core.localization.home_lobbies_empty
import com.dev.memebattle.core.localization.home_lobbies_item_title
import com.dev.memebattle.core.localization.home_lobbies_item_details
import com.dev.memebattle.core.localization.home_lobbies_item_players
import com.dev.memebattle.core.localization.home_lobbies_create_fab
import com.dev.memebattle.core.localization.home_lobbies_btn_join
import com.dev.memebattle.core.localization.lobby_create_mode_situation_to_meme
import com.dev.memebattle.core.localization.lobby_create_mode_meme_to_situation
import com.dev.memebattle.core.localization.gameplay_players_btn_close
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.component.menu.HomeMenuComponent
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.view.auth.AuthDialog
import com.dev.memebattle.feature.home.impl.presentation.view.auth.UserChip
import com.dev.memebattle.feature.home.impl.presentation.view.create.CreateLobbyView
import androidx.compose.ui.zIndex

@Composable
fun HomeMenuView(
    component: HomeMenuComponent,
    detailsComponent: CreateLobbyComponent? = null
) {
    val state by component.state.collectAsState()
    val authState by component.authState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    // Close auth dialog automatically on success
    androidx.compose.runtime.LaunchedEffect(Unit) {
        component.authEffects.collect { effect ->
            when (effect) {
                is com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore.Effect.AuthSuccess ->
                    showAuthDialog = false
                is com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore.Effect.LoggedOut ->
                    showAuthDialog = false
            }
        }
    }

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
        // ── User chip (top-right) ───────────────────────────────────────
        androidx.compose.animation.AnimatedVisibility(
            visible = !state.isLobbyListVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 16.dp)
                .zIndex(10f)
        ) {
            UserChip(
                identity = authState.identity,
                onClick = { showAuthDialog = true },
                modifier = Modifier
            )
        }
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
                                    onCreateLobby = { component.onIntent(HomeMenuStore.Intent.OnCreateLobbyClicked) },
                                    onJoinLobby = { gameId -> component.onIntent(HomeMenuStore.Intent.OnJoinLobbyClicked(gameId)) },
                                    onUpdateJoinHandle = { component.onIntent(HomeMenuStore.Intent.UpdateJoinHandleInput(it)) },
                                    onConfirmJoin = { component.onIntent(HomeMenuStore.Intent.ConfirmJoin) },
                                    onCancelJoin = { component.onIntent(HomeMenuStore.Intent.CancelJoin) }
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

        // ── Auth Dialog ──────────────────────────────────────────────────
        if (showAuthDialog) {
            AuthDialog(
                authState = authState,
                onIntent = { component.onAuthIntent(it) },
                onDismiss = { showAuthDialog = false }
            )
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
    onCreateLobby: () -> Unit,
    onJoinLobby: (String) -> Unit = {},
    onUpdateJoinHandle: (String) -> Unit = {},
    onConfirmJoin: () -> Unit = {},
    onCancelJoin: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.gameplay_players_btn_close), tint = Color.White)
            }
            Text(
                text = stringResource(Res.string.home_lobbies_available_title),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lobby list
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF7C5DFA)
                    )
                }
                state.lobbies.isEmpty() -> {
                    Text(
                        text = stringResource(Res.string.home_lobbies_empty),
                        color = Color(0xFFB0A2C7),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Info block
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(Res.string.home_lobbies_item_title, lobby.id.take(8)),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val modeText = if (lobby.mode == "SITUATION_TO_MEME") {
                                                stringResource(Res.string.lobby_create_mode_situation_to_meme)
                                            } else {
                                                stringResource(Res.string.lobby_create_mode_meme_to_situation)
                                            }
                                            Text(
                                                text = stringResource(Res.string.home_lobbies_item_details, modeText, lobby.maxRounds, lobby.handSize),
                                                color = Color(0xFFB0A2C7),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = stringResource(Res.string.home_lobbies_item_players, lobby.playersCount),
                                                color = Color(0xFF7C5DFA),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Join button
                                        Button(
                                            onClick = { onJoinLobby(lobby.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF7C5DFA)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(Res.string.home_lobbies_btn_join),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FAB
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.home_lobbies_create_fab))
                }
            }
        }
        
        if (state.joinGameId != null) {
            Dialog(onDismissRequest = onCancelJoin) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E143B),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF7C5DFA).copy(alpha = 0.6f), Color(0xFF3B2F5E))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(Res.string.home_join_dialog_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )

                        Spacer(Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7C5DFA), Color(0xFF5B8DEF))
                                    )
                                )
                        )

                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.home_join_dialog_description),
                                color = Color(0xFFB0A2C7),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.joinHandleInput,
                            onValueChange = onUpdateJoinHandle,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(Res.string.home_join_dialog_nickname_label)) },
                            placeholder = { Text(stringResource(Res.string.home_join_dialog_nickname_hint), color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C5DFA),
                                unfocusedBorderColor = Color(0xFF3B2F5E),
                                focusedLabelColor = Color(0xFF7C5DFA),
                                unfocusedLabelColor = Color(0xFF887A9E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7C5DFA),
                            ),
                            shape = RoundedCornerShape(14.dp),
                        )

                        if (state.joinError != null) {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFB71C1C).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = state.joinError,
                                    color = Color(0xFFFF5252),
                                    fontSize = 13.sp,
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = onCancelJoin,
                                enabled = !state.isJoining,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFF3B2F5E)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFB0A2C7),
                                    disabledContentColor = Color(0xFF887A9E),
                                ),
                            ) {
                                Text(
                                    text = stringResource(Res.string.home_join_dialog_cancel),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            Button(
                                onClick = onConfirmJoin,
                                enabled = !state.isJoining,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7C5DFA),
                                    disabledContainerColor = Color(0xFF3B2F5E),
                                ),
                            ) {
                                if (state.isJoining) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = stringResource(Res.string.home_join_dialog_confirm),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
