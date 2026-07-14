package com.dev.memebattle.feature.packs.impl.presentation.component.create

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStore

interface PacksCreateComponent {
    val state: StateFlow<PacksCreateStore.State>
    val effects: SharedFlow<PacksCreateStore.Effect>
    fun onIntent(intent: PacksCreateStore.Intent)
    fun showNotification(message: String, isError: Boolean = false)
    fun onCreated(packId: String, kind: String)
}
