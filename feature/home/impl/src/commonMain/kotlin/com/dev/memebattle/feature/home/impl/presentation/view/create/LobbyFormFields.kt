package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_name_hint
import com.dev.memebattle.core.localization.lobby_create_name_label
import com.dev.memebattle.core.localization.lobby_create_nickname_hint
import com.dev.memebattle.core.localization.lobby_create_nickname_label
import com.dev.memebattle.core.localization.lobby_create_submit
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val BorderColor = Color(0xFF3B2F5E)
private val TextMuted = Color(0xFF887A9E)

@Composable
fun LobbyFormFields(
    lobbyName: String,
    handle: String,
    error: String?,
    isLoading: Boolean,
    isCreateEnabled: Boolean,
    onLobbyNameChanged: (String) -> Unit,
    onHandleChanged: (String) -> Unit,
    onCreateLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = lobbyName,
            onValueChange = onLobbyNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.lobby_create_name_label)) },
            placeholder = { Text(stringResource(Res.string.lobby_create_name_hint), color = Color.White.copy(alpha = 0.3f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = BorderColor,
                focusedLabelColor = AccentColor,
                unfocusedLabelColor = TextMuted,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentColor,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = handle,
            onValueChange = { if (it.length <= 20) onHandleChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.lobby_create_nickname_label)) },
            placeholder = { Text(stringResource(Res.string.lobby_create_nickname_hint), color = Color.White.copy(alpha = 0.3f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = BorderColor,
                focusedLabelColor = AccentColor,
                unfocusedLabelColor = TextMuted,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentColor,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFB71C1C).copy(alpha = 0.2f),
            ) {
                Text(
                    text = error,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onCreateLobby,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = isCreateEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                disabledContainerColor = SurfaceColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = stringResource(Res.string.lobby_create_submit),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCreateEnabled) Color.White else TextMuted
                )
            }
        }
    }
}
