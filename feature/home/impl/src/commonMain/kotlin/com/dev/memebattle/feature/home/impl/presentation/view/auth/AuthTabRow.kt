package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.auth_tab_guest
import com.dev.memebattle.core.localization.auth_tab_login
import com.dev.memebattle.core.localization.auth_tab_register
import org.jetbrains.compose.resources.stringResource

/** Auth flow tab variants. */
internal enum class AuthTab { Login, Register }

/**
 * Two-segment tab selector: Вход / Регистрация.
 */
@Composable
internal fun AuthTabRow(selected: AuthTab, onSelect: (AuthTab) -> Unit) {
    val tabs = listOf(
        AuthTab.Login    to stringResource(Res.string.auth_tab_login),
        AuthTab.Register to stringResource(Res.string.auth_tab_register),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F0820)),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        tabs.forEach { (tab, label) ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0xFF6C47FF) else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF887A9E),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
