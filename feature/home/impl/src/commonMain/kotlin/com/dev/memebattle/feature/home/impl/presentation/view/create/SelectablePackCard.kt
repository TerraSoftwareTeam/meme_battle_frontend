package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_official_badge
import com.dev.memebattle.core.ui.components.pack.PackCard
import com.dev.memebattle.core.ui.components.pack.PackCardKind
import com.dev.memebattle.core.ui.components.pack.PackCardSafetyLevel
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SelectedBorder = Color(0xFF7C5DFA)
private val OfficialBadge = Color(0xFFFFD700)

@Composable
fun SelectablePackCard(
    id: String,
    name: String,
    description: String,
    createdAt: String,
    safetyLevel: PackCardSafetyLevel,
    packType: PackCardKind,
    languageCode: String,
    isSelected: Boolean,
    isOfficial: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(3.dp, SelectedBorder, RoundedCornerShape(16.dp))
                else Modifier
            )
    ) {
        PackCard(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            safetyLevel = safetyLevel,
            packType = packType,
            languageCode = languageCode,
            onClick = onClick
        )
        if (isOfficial) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
                shape = RoundedCornerShape(6.dp),
                color = OfficialBadge.copy(alpha = 0.92f)
            ) {
                Text(
                    text = stringResource(Res.string.lobby_create_official_badge),
                    color = Color(0xFF1A1000),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AccentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

fun SafetyLevel.toCardLevel(): PackCardSafetyLevel = when (this) {
    SafetyLevel.FAMILY_FRIENDLY -> PackCardSafetyLevel.FAMILY_FRIENDLY
    SafetyLevel.SPICY -> PackCardSafetyLevel.SPICY
    SafetyLevel.EXPLICIT -> PackCardSafetyLevel.EXPLICIT
}
