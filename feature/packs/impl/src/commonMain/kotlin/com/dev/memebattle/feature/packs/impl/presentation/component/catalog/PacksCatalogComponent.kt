package com.dev.memebattle.feature.packs.impl.presentation.component.catalog

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore

interface PacksCatalogComponent {
    val state: StateFlow<PacksCatalogStore.State>
    val effects: SharedFlow<PacksCatalogStore.Effect>
    fun onIntent(intent: PacksCatalogStore.Intent)
}
