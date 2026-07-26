package com.dev.memebattle.feature.home.impl.presentation.component.menu

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore

interface HomeMenuComponent {
    val state: StateFlow<HomeMenuStore.State>
    val effects: SharedFlow<HomeMenuStore.Effect>
    fun onIntent(intent: HomeMenuStore.Intent)
}
