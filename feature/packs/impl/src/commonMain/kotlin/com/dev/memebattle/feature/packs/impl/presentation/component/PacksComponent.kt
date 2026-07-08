package com.dev.memebattle.feature.packs.impl.presentation.component

import com.dev.memebattle.core.navigation.entry.FeatureComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.packs.impl.presentation.store.PacksStore

interface PacksComponent : FeatureComponent {
    val state: StateFlow<PacksStore.State>
    val effects: SharedFlow<PacksStore.Effect>
    fun onIntent(intent: PacksStore.Intent)
}
