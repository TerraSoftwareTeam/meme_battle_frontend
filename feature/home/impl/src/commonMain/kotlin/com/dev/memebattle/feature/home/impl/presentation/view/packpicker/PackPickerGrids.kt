package com.dev.memebattle.feature.home.impl.presentation.view.packpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_no_memes
import com.dev.memebattle.core.localization.packs_no_situations
import com.dev.memebattle.core.ui.components.pack.PackCard
import com.dev.memebattle.core.ui.components.pack.PackCardKind
import com.dev.memebattle.core.ui.components.pack.PackCardSafetyLevel
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val TextSecondary = Color(0xFFB0A2C7)
private val SelectedBorder = Color(0xFF7C5DFA)

@Composable
fun MemePackGrid(
    packs: List<MemePack>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (packs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.packs_no_memes),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val minCardWidth = 140.dp
        val gap = 12.dp
        val columns = maxOf(2, ((availableWidth - 24.dp + gap) / (minCardWidth + gap)).toInt())

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            items(packs, key = { it.id }) { pack ->
                PickerSelectablePackCard(
                    id = pack.id,
                    name = pack.name,
                    description = pack.description,
                    createdAt = pack.createdAt,
                    safetyLevel = pack.safetyLevel.toCardLevel(),
                    packType = PackCardKind.MEME,
                    languageCode = pack.languageCode,
                    isSelected = pack.id in selectedIds,
                    onClick = { onToggle(pack.id) }
                )
            }
        }
    }
}

@Composable
fun SituationPackGrid(
    packs: List<SituationPack>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (packs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.packs_no_situations),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val minCardWidth = 140.dp
        val gap = 12.dp
        val columns = maxOf(2, ((availableWidth - 24.dp + gap) / (minCardWidth + gap)).toInt())

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            items(packs, key = { it.id }) { pack ->
                PickerSelectablePackCard(
                    id = pack.id,
                    name = pack.name,
                    description = pack.description,
                    createdAt = pack.createdAt,
                    safetyLevel = pack.safetyLevel.toCardLevel(),
                    packType = PackCardKind.SITUATION,
                    languageCode = pack.languageCode,
                    isSelected = pack.id in selectedIds,
                    onClick = { onToggle(pack.id) }
                )
            }
        }
    }
}

@Composable
private fun PickerSelectablePackCard(
    id: String,
    name: String,
    description: String?,
    createdAt: String,
    safetyLevel: PackCardSafetyLevel,
    packType: PackCardKind,
    languageCode: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, SelectedBorder, RoundedCornerShape(16.dp))
                } else Modifier
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
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun SafetyLevel.toCardLevel(): PackCardSafetyLevel = when (this) {
    SafetyLevel.FAMILY_FRIENDLY -> PackCardSafetyLevel.FAMILY_FRIENDLY
    SafetyLevel.SPICY -> PackCardSafetyLevel.SPICY
    SafetyLevel.EXPLICIT -> PackCardSafetyLevel.EXPLICIT
}
