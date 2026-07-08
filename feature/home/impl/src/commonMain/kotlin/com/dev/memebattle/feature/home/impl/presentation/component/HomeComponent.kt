package com.dev.memebattle.feature.home.impl.presentation.component

import com.dev.memebattle.core.navigation.entry.FeatureComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.home.impl.presentation.store.HomeStore

interface HomeComponent : FeatureComponent {
    val state: StateFlow<HomeStore.State>
    val effects: SharedFlow<HomeStore.Effect>
    fun onIntent(intent: HomeStore.Intent)
}
