package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_chip_guest
import com.dev.memebattle.core.localization.auth_guest_username_hint
import com.dev.memebattle.core.localization.auth_guest_username_label
import com.dev.memebattle.core.localization.auth_guest_warning_hint
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import org.jetbrains.compose.resources.stringResource

/**
 * Guest authentication form.
 * - Single text field for guest name input.
 * - Submits via confirmGuestUsername (uses name if typed, anonymous if left blank).
 */
@Composable
internal fun GuestForm(
    state: AuthStore.State,
    onIntent: (AuthStore.Intent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AuthTextField(
            value = state.guestUsernameInput,
            onValueChange = { onIntent(AuthStore.Intent.UpdateGuestUsername(it)) },
            label = stringResource(Res.string.auth_guest_username_label),
            placeholder = stringResource(Res.string.auth_guest_username_hint),
        )

        // Explanation note
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

        AuthPrimaryButton(
            text = stringResource(Res.string.auth_chip_guest),
            isLoading = state.isLoading,
            onClick = { onIntent(AuthStore.Intent.ConfirmGuestUsername) },
        )
    }
}
