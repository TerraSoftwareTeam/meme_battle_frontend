package com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Language",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(SurfaceColor, RoundedCornerShape(8.dp))
        ) {
            val languages = listOf("ru", "en")
            languages.forEach { lang ->
                val isSelected = selectedLanguage == lang
                val displayText = if (lang == "ru") "RU" else "EN"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentColor else Color.Transparent)
                        .clickable { onLanguageSelected(lang) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayText,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
