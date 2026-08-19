package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_dialog_close
import com.dev.memebattle.core.localization.auth_dialog_title
import com.dev.memebattle.feature.home.impl.domain.UserIdentity
import com.dev.memebattle.feature.home.impl.domain.isGuest
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import org.jetbrains.compose.resources.stringResource

/**
 * Full-screen auth dialog with three tabs: Login / Register / Guest.
 * When the user is already [UserIdentity.Authorized], shows [AuthorizedHeader] instead of the tabs.
 */
@Composable
fun AuthDialog(
    authState: AuthStore.State,
    onIntent: (AuthStore.Intent) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialTab = if (authState.identity.isGuest) AuthTab.Guest else AuthTab.Login
    var selectedTab by remember { mutableStateOf(initialTab) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.92f)
                    .wrapContentHeight()
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF1A1035),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF7C5DFA).copy(alpha = 0.7f), Color(0xFF3B2F5E))
                    ),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // ── Header bar ──────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_dialog_title),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(Res.string.auth_dialog_close),
                                tint = Color(0xFF887A9E),
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Accent underline
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C5DFA), Color(0xFF5B8DEF))
                                )
                            )
                            .align(Alignment.Start),
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Content: authorized state OR tab flow ───────────
                    when (val id = authState.identity) {
                        is UserIdentity.Authorized -> {
                            AuthorizedHeader(
                                identity = id,
                                onLogOut = { onIntent(AuthStore.Intent.LogOut) },
                            )
                        }
                        else -> {
                            AuthTabRow(selected = selectedTab, onSelect = { selectedTab = it })

                            Spacer(Modifier.height(20.dp))

                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                                },
                                label = "authTabContent",
                            ) { tab ->
                                when (tab) {
                                    AuthTab.Login -> LoginForm(
                                        state = authState,
                                        passwordVisible = passwordVisible,
                                        onTogglePassword = { passwordVisible = !passwordVisible },
                                        onIntent = onIntent,
                                    )
                                    AuthTab.Register -> RegisterForm(
                                        state = authState,
                                        passwordVisible = passwordVisible,
                                        onTogglePassword = { passwordVisible = !passwordVisible },
                                        onIntent = onIntent,
                                    )
                                    AuthTab.Guest -> GuestForm(
                                        state = authState,
                                        onIntent = onIntent,
                                    )
                                }
                            }
                        }
                    }

                    // ── Error banner with generous margins and padding ───
                    AnimatedVisibility(
                        visible = authState.error != null,
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit  = fadeOut() + slideOutVertically { -it / 2 },
                    ) {
                        authState.error?.let { error ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFB71C1C).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                ) {
                                    Text(
                                        text = error,
                                        color = Color(0xFFFF5252),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
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
