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
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_create_safety_0
import com.dev.memebattle.core.localization.packs_create_safety_16
import com.dev.memebattle.core.localization.packs_create_safety_18
import com.dev.memebattle.core.localization.packs_create_safety_level

@Composable
internal fun SafetyLevelSelector(
    selectedLevel: SafetyLevel,
    onLevelSelected: (SafetyLevel) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.packs_create_safety_level),
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
            val levels = SafetyLevel.entries
            levels.forEach { level ->
                val isSelected = selectedLevel == level
                val textRes = when (level) {
                    SafetyLevel.FAMILY_FRIENDLY -> Res.string.packs_create_safety_0
                    SafetyLevel.SPICY -> Res.string.packs_create_safety_16
                    SafetyLevel.EXPLICIT -> Res.string.packs_create_safety_18
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentColor else Color.Transparent)
                        .clickable { onLevelSelected(level) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(textRes),
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
