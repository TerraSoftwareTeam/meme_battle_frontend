package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_btn_add_from_store
import com.dev.memebattle.core.ui.components.pack.PackCardKind
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val BorderColor = Color(0xFF3B2F5E)
private val TextSecondary = Color(0xFFB0A2C7)
private val TextMuted = Color(0xFF887A9E)

@Composable
fun PackSectionHeader(
    title: String,
    onAddFromCatalog: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        TextButton(
            onClick = onAddFromCatalog,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.lobby_create_btn_add_from_store),
                color = AccentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PackSelectionRow(
    packs: List<Any>,
    selectedIds: Set<String>,
    officialIds: Set<String>,
    packKind: PackCardKind,
    cardCounts: Map<String, Int> = emptyMap(),
    onToggle: (String) -> Unit,
) {
    data class PackInfo(
        val id: String,
        val name: String,
        val description: String?,
        val createdAt: String,
        val safetyLevel: SafetyLevel,
        val languageCode: String,
        val cardCount: Int?,
    )

    val packInfoList = packs.mapNotNull { pack ->
        when (pack) {
            is MemePack -> PackInfo(
                id = pack.id, name = pack.name, description = pack.description,
                createdAt = pack.createdAt, safetyLevel = pack.safetyLevel, languageCode = pack.languageCode,
                cardCount = cardCounts[pack.id]
            )
            is SituationPack -> PackInfo(
                id = pack.id, name = pack.name, description = pack.description,
                createdAt = pack.createdAt, safetyLevel = pack.safetyLevel, languageCode = pack.languageCode,
                cardCount = cardCounts[pack.id]
            )
            else -> null
        }
    }

    if (packInfoList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "—", color = TextMuted, fontSize = 20.sp)
        }
        return
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(packInfoList, key = { it.id }) { pack ->
            val isSelected = selectedIds.contains(pack.id)
            val isOfficial = officialIds.contains(pack.id)
            Box(modifier = Modifier.width(150.dp)) {
                SelectablePackCard(
                    id = pack.id,
                    name = pack.name,
                    description = pack.description ?: "",
                    createdAt = pack.createdAt,
                    safetyLevel = pack.safetyLevel.toCardLevel(),
                    packType = packKind,
                    languageCode = pack.languageCode,
                    isSelected = isSelected,
                    isOfficial = isOfficial,
                    cardCount = pack.cardCount,
                    onClick = { onToggle(pack.id) }
                )
            }
        }
    }
}

@Composable
fun PackCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.70f)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BorderColor)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BorderColor)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(32.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(BorderColor))
                Box(modifier = Modifier.width(50.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(BorderColor))
            }
        }
    }
}

@Composable
fun SkeletonRow(count: Int) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(count) {
            Box(modifier = Modifier.width(150.dp)) {
                PackCardSkeleton()
            }
        }
    }
}
