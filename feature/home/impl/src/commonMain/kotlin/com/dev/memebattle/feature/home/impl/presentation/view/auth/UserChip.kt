package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import com.dev.memebattle.core.localization.auth_chip_guest
import com.dev.memebattle.core.localization.auth_chip_login
import com.dev.memebattle.feature.home.impl.domain.UserIdentity
import com.dev.memebattle.feature.home.impl.domain.displayName
import com.dev.memebattle.feature.home.impl.domain.isAuthorized
import com.dev.memebattle.feature.home.impl.domain.isGuest
import org.jetbrains.compose.resources.stringResource

/**
 * Compact chip shown in the top-right corner of HomeMenuView.
 * Reflects the current auth identity state at a glance.
 */
@Composable
fun UserChip(
    identity: UserIdentity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = when {
        identity.isAuthorized -> Color(0xFF7C5DFA)
        identity.isGuest     -> Color(0xFF3B2F5E)
        else                 -> Color(0xFF2A1F4A)
    }
    val borderColor = when {
        identity.isAuthorized -> Color(0xFF9B7FFF)
        identity.isGuest     -> Color(0xFF5A4680)
        else                 -> Color(0xFF4A3870)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(chipColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (identity.isAuthorized) Color(0xFF9B7FFF) else Color(0xFF5A4680)
                ),
            contentAlignment = Alignment.Center
        ) {
            val name = identity.displayName
            if (name != null) {
                Text(
                    text = name.take(1).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Display name label
        val label = when (identity) {
            is UserIdentity.Authorized -> identity.username
            is UserIdentity.Guest      -> identity.name?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.auth_chip_guest)
            UserIdentity.Unknown       -> stringResource(Res.string.auth_chip_login)
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}
