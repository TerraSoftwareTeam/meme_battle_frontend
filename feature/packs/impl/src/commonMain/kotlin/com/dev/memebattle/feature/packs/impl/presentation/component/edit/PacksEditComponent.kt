package com.dev.memebattle.feature.packs.impl.presentation.component.edit

import com.dev.memebattle.feature.packs.impl.presentation.store.edit.PacksEditStore
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PacksEditComponent {
    val state: StateFlow<PacksEditStore.State>
    val effects: SharedFlow<PacksEditStore.Effect>

    fun onIntent(intent: PacksEditStore.Intent)
    fun showNotification(message: String, isError: Boolean = false)
    fun navigateToDetails(packId: String, kind: String)
}
