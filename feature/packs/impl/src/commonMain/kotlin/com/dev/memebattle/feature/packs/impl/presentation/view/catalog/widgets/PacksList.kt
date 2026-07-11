package com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore

internal data class PackUiModel(val id: String, val name: String)

@Composable
internal fun PacksList(
    state: PacksCatalogStore.State,
    onPackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiPacks = when (state.activeType) {
        PacksCatalogStore.PackType.Memes -> state.memePacks.map { PackUiModel(it.id, it.name) }
        PacksCatalogStore.PackType.Situations -> state.situationPacks.map { PackUiModel(it.id, it.name) }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiPacks.isEmpty() && !state.isLoading) {
            item {
                EmptyPacksPlaceholder(
                    type = state.activeType,
                    modifier = Modifier.fillParentMaxSize(),
                )
            }
        } else {
            itemsIndexed(uiPacks, key = { _, pack -> pack.id }) { _, pack ->
                PackCard(
                    name = pack.name,
                    cardCount = 0, // Placeholder until domain model supports counting
                    onClick = { onPackClick(pack.id) },
                )
            }
        }
    }
}
