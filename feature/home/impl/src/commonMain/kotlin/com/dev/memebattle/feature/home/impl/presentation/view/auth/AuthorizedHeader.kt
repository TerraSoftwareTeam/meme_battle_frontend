package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_authorized_logout
import com.dev.memebattle.feature.home.impl.domain.UserIdentity
import org.jetbrains.compose.resources.stringResource

/**
 * Profile header shown inside AuthDialog when the user is fully authorized.
 * Displays avatar initial, username, short ID, and a logout button.
 */
@Composable
internal fun AuthorizedHeader(
    identity: UserIdentity.Authorized,
    onLogOut: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF9B7FFF), Color(0xFF6C47FF)))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = identity.username.take(1).uppercase(),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = identity.username,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = "ID: ${identity.id.take(8)}…",
            fontSize = 12.sp,
            color = Color(0xFF887A9E),
        )

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = onLogOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
        ) {
            Text(
                text = stringResource(Res.string.auth_authorized_logout),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
