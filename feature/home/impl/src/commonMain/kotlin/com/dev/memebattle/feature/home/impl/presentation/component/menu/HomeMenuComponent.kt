package com.dev.memebattle.feature.home.impl.presentation.component.menu

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore

interface HomeMenuComponent {
    // --- Lobby state (existing) ---
    val state: StateFlow<HomeMenuStore.State>
    val effects: SharedFlow<HomeMenuStore.Effect>
    fun onIntent(intent: HomeMenuStore.Intent)

    // --- Auth state (new) ---
    val authState: StateFlow<AuthStore.State>
    val authEffects: SharedFlow<AuthStore.Effect>
    fun onAuthIntent(intent: AuthStore.Intent)
}
