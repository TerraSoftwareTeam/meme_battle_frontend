package com.dev.memebattle.feature.packs.impl.presentation.component.details

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore

interface PacksDetailsComponent {
    val state: StateFlow<PacksDetailsStore.State>
    val effects: SharedFlow<PacksDetailsStore.Effect>
    fun onIntent(intent: PacksDetailsStore.Intent)
}
