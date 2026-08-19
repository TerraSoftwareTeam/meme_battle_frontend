package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_guest_btn_anonymous
import com.dev.memebattle.core.localization.auth_guest_btn_with_name
import com.dev.memebattle.core.localization.auth_guest_current_name_label
import com.dev.memebattle.core.localization.auth_guest_username_hint
import com.dev.memebattle.core.localization.auth_guest_username_label
import com.dev.memebattle.core.localization.auth_guest_warning_hint
import com.dev.memebattle.feature.home.impl.domain.UserIdentity
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import org.jetbrains.compose.resources.stringResource

/**
 * Guest authentication form.
 * - Shows current guest name if one is already set.
 * - Allows entering a new name or continuing anonymously.
 * - Submits via authAsGuest(username?) on the backend.
 */
@Composable
internal fun GuestForm(
    state: AuthStore.State,
    onIntent: (AuthStore.Intent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Show current name if user is already a guest
        val currentName = (state.identity as? UserIdentity.Guest)?.name?.takeIf { it.isNotBlank() }
        if (currentName != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6C47FF).copy(alpha = 0.15f))
                    .padding(12.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.auth_guest_current_name_label),
                        fontSize = 11.sp,
                        color = Color(0xFF887A9E),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        AuthTextField(
            value = state.guestUsernameInput,
            onValueChange = { onIntent(AuthStore.Intent.UpdateGuestUsername(it)) },
            label = stringResource(Res.string.auth_guest_username_label),
            placeholder = stringResource(Res.string.auth_guest_username_hint),
        )

        // Explanation note (no emojis)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2E2452).copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.auth_guest_warning_hint),
                color = Color(0xFFB0A2C7),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }

        Spacer(Modifier.height(4.dp))

        // Two buttons: anonymous or with a chosen name
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { onIntent(AuthStore.Intent.ContinueAsGuest) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF3B2F5E)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB0A2C7)),
                enabled = !state.isLoading,
            ) {
                Text(
                    text = stringResource(Res.string.auth_guest_btn_anonymous),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
            Button(
                onClick = { onIntent(AuthStore.Intent.ConfirmGuestUsername) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47FF)),
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.auth_guest_btn_with_name),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}
