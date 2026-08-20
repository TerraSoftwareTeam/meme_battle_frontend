package com.dev.memebattle.feature.home.impl.presentation.view.menu.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.AppLanguage
import com.dev.memebattle.core.localization.currentAppLanguage
import com.dev.memebattle.core.localization.setAppLanguage

@Composable
fun LanguageSwitchChip(
    modifier: Modifier = Modifier,
    language: AppLanguage = currentAppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit = { setAppLanguage(it) }
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF2A1F44),
        border = BorderStroke(1.dp, Color(0xFF3B2F5E))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLanguage.entries.forEach { item ->
                val isSelected = item == language
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF7C5DFA) else Color.Transparent
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFFB0A2C7)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    modifier = Modifier.clickable { onLanguageChanged(item) }
                ) {
                    Text(
                        text = item.label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
