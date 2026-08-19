package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_login_hide_password
import com.dev.memebattle.core.localization.auth_login_password_hint
import com.dev.memebattle.core.localization.auth_login_password_label
import com.dev.memebattle.core.localization.auth_login_show_password
import com.dev.memebattle.core.localization.auth_login_submit
import com.dev.memebattle.core.localization.auth_login_username_hint
import com.dev.memebattle.core.localization.auth_login_username_label
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import org.jetbrains.compose.resources.stringResource

/**
 * Login form: username + password fields + submit button.
 */
@Composable
internal fun LoginForm(
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
            placeholder = stringResource(Res.string.auth_login_username_hint),
        )
        AuthTextField(
            value = state.loginPassword,
            onValueChange = { onIntent(AuthStore.Intent.UpdateLoginPassword(it)) },
            label = stringResource(Res.string.auth_login_password_label),
            placeholder = stringResource(Res.string.auth_login_password_hint),
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

        Spacer(Modifier.height(4.dp))

        AuthPrimaryButton(
            text = stringResource(Res.string.auth_login_submit),
            isLoading = state.isLoading,
            onClick = { onIntent(AuthStore.Intent.SubmitLogin) },
        )
    }
}
