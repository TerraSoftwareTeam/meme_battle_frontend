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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_login_hide_password
import com.dev.memebattle.core.localization.auth_login_show_password
import com.dev.memebattle.core.localization.auth_login_username_label
import com.dev.memebattle.core.localization.auth_register_no_password_hint
import com.dev.memebattle.core.localization.auth_register_password_hint
import com.dev.memebattle.core.localization.auth_register_password_label
import com.dev.memebattle.core.localization.auth_register_submit
import com.dev.memebattle.core.localization.auth_register_username_hint
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import org.jetbrains.compose.resources.stringResource

/**
 * Registration form: username + optional password + submit.
 * Explains that a password-less account is bound to this device only.
 */
@Composable
internal fun RegisterForm(
    state: AuthStore.State,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onIntent: (AuthStore.Intent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AuthTextField(
            value = state.loginUsername,
            onValueChange = { onIntent(AuthStore.Intent.UpdateLoginUsername(it)) },
            label = stringResource(Res.string.auth_login_username_label),
            placeholder = stringResource(Res.string.auth_register_username_hint),
        )
        AuthTextField(
            value = state.loginPassword,
            onValueChange = { onIntent(AuthStore.Intent.UpdateLoginPassword(it)) },
            label = stringResource(Res.string.auth_register_password_label),
            placeholder = stringResource(Res.string.auth_register_password_hint),
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = onTogglePassword) {
                    Text(
                        text = if (passwordVisible) stringResource(Res.string.auth_login_hide_password)
                               else stringResource(Res.string.auth_login_show_password),
                        color = Color(0xFF7C5DFA),
                    )
                }
            },
        )

        // Hint about password-less accounts (no emojis)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF6C47FF).copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.auth_register_no_password_hint),
                color = Color(0xFFB0A2C7),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }

        Spacer(Modifier.height(4.dp))

        AuthPrimaryButton(
            text = stringResource(Res.string.auth_register_submit),
            isLoading = state.isLoading,
            onClick = { onIntent(AuthStore.Intent.SubmitRegister) },
        )
    }
}
