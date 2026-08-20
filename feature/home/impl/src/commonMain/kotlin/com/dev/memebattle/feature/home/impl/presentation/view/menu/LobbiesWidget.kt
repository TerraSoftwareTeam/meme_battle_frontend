package com.dev.memebattle.feature.home.impl.presentation.view.menu

import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.DialogProperties
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_players_btn_close
import com.dev.memebattle.core.localization.home_join_dialog_cancel
import com.dev.memebattle.core.localization.home_join_dialog_confirm
import com.dev.memebattle.core.localization.home_join_dialog_description
import com.dev.memebattle.core.localization.home_join_dialog_nickname_hint
import com.dev.memebattle.core.localization.home_join_dialog_nickname_label
import com.dev.memebattle.core.localization.home_join_dialog_title
import com.dev.memebattle.core.localization.home_lobbies_available_title
import com.dev.memebattle.core.localization.home_lobbies_btn_join
import com.dev.memebattle.core.localization.home_lobbies_create_fab
import com.dev.memebattle.core.localization.home_lobbies_empty
import com.dev.memebattle.core.localization.home_lobbies_item_details
import com.dev.memebattle.core.localization.home_lobbies_item_players
import com.dev.memebattle.core.localization.home_lobbies_item_title
import com.dev.memebattle.core.localization.lobby_create_mode_meme_to_situation
import com.dev.memebattle.core.localization.lobby_create_mode_situation_to_meme
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import org.jetbrains.compose.resources.stringResource

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
                                        Column(modifier = Modifier.weight(1f)) {
                                            val titleText = if (!lobby.name.isNullOrBlank()) lobby.name!! else stringResource(Res.string.home_lobbies_item_title, lobby.id.take(8))
                                            Text(
                                                text = titleText,
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
            Dialog(
                onDismissRequest = onCancelJoin,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(onClick = onCancelJoin),
                    contentAlignment = Alignment.Center,
                ) {
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
                            .padding(16.dp)
                            .widthIn(max = 440.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable(enabled = false, onClick = {}),
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
}
