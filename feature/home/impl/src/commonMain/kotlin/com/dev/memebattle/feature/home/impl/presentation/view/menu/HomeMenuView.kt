package com.dev.memebattle.feature.home.impl.presentation.view.menu

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.store
import com.dev.memebattle.feature.home.impl.presentation.view.menu.widgets.LanguageSwitchChip
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.component.menu.HomeMenuComponent
import com.dev.memebattle.feature.home.impl.presentation.component.packpicker.PackPickerComponent
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.view.auth.AuthDialog
import com.dev.memebattle.feature.home.impl.presentation.view.auth.UserChip
import com.dev.memebattle.feature.home.impl.presentation.view.create.CreateLobbyView
import com.dev.memebattle.feature.home.impl.presentation.view.packpicker.PackPickerView
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeMenuView(
    component: HomeMenuComponent,
    detailsComponent: CreateLobbyComponent? = null,
    packPickerComponent: PackPickerComponent? = null
) {
    val state by component.state.collectAsState()
    val authState by component.authState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        component.authEffects.collect { effect ->
            when (effect) {
                is AuthStore.Effect.AuthSuccess -> showAuthDialog = false
                is AuthStore.Effect.LoggedOut -> showAuthDialog = false
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
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                .zIndex(10f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageSwitchChip()

            androidx.compose.animation.AnimatedVisibility(
                visible = !state.isLobbyListVisible,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200))
            ) {
                UserChip(
                    identity = authState.identity,
                    onClick = { showAuthDialog = true },
                    modifier = Modifier
                )
            }
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

                MorphingPlayButton(
                    isExpanded = state.isLobbyListVisible || detailsComponent != null || packPickerComponent != null,
                    isWide = isWide,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    onPlayClick = { component.onIntent(HomeMenuStore.Intent.OnPlayClicked) },
                    lobbyContent = {
                        when {
                            packPickerComponent != null -> {
                                PackPickerView(
                                    component = packPickerComponent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            detailsComponent != null -> {
                                CreateLobbyView(
                                    component = detailsComponent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
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
                    }
                )

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

        if (showAuthDialog) {
            AuthDialog(
                authState = authState,
                onIntent = { component.onAuthIntent(it) },
                onDismiss = { showAuthDialog = false }
            )
        }
    }
}
