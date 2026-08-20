package com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore

internal data class PackUiModel(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val safetyLevel: SafetyLevel,
    val languageCode: String
)

@Composable
internal fun PacksList(
    state: PacksCatalogStore.State,
    onPackClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiPacks = when (state.activeType) {
        PacksCatalogStore.PackType.Memes -> {
            val packs = when (state.activeFilter) {
                PacksCatalogStore.PackFilter.All -> state.memePacks
                PacksCatalogStore.PackFilter.Personal -> state.myMemePacks
            }
            packs.map { PackUiModel(it.id, it.name, it.description, it.createdAt, it.safetyLevel, it.languageCode) }
        }
        PacksCatalogStore.PackType.Situations -> {
            val packs = when (state.activeFilter) {
                PacksCatalogStore.PackFilter.All -> state.situationPacks
                PacksCatalogStore.PackFilter.Personal -> state.mySituationPacks
            }
            packs.map { PackUiModel(it.id, it.name, it.description, it.createdAt, it.safetyLevel, it.languageCode) }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiPacks.isEmpty() && !state.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyPacksPlaceholder(
                    type = state.activeType,
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        } else {
            itemsIndexed(uiPacks, key = { _, pack -> pack.id }) { _, pack ->
                com.dev.memebattle.core.ui.components.pack.PackCard(
                    id = pack.id,
                    name = pack.name,
                    description = pack.description,
                    createdAt = pack.createdAt,
                    packType = if (state.activeType == PacksCatalogStore.PackType.Memes) com.dev.memebattle.core.ui.components.pack.PackCardKind.MEME else com.dev.memebattle.core.ui.components.pack.PackCardKind.SITUATION,
                    safetyLevel = when (pack.safetyLevel) {
                        SafetyLevel.FAMILY_FRIENDLY -> com.dev.memebattle.core.ui.components.pack.PackCardSafetyLevel.FAMILY_FRIENDLY
                        SafetyLevel.SPICY -> com.dev.memebattle.core.ui.components.pack.PackCardSafetyLevel.SPICY
                        SafetyLevel.EXPLICIT -> com.dev.memebattle.core.ui.components.pack.PackCardSafetyLevel.EXPLICIT
                    },
                    languageCode = pack.languageCode,
                    onClick = { onPackClick(pack.id) },
                    onEditClick = if (state.activeFilter == PacksCatalogStore.PackFilter.Personal) {
                        { onEditClick(pack.id) }
                    } else null,
                )
            }
        }
    }
}
