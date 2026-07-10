package com.dev.memebattle.feature.packs.impl.presentation.component.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore
import com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

class PacksDetailsComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val packId: String,
    private val onClose: () -> Unit,
) : PacksDetailsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val store = PacksDetailsStoreFactory(storeFactory).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<PacksDetailsStore.State> = store.stateFlow(scope)

    override val effects: SharedFlow<PacksDetailsStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    init {
        store.accept(PacksDetailsStore.Intent.Load(packId))
    }

    override fun onIntent(intent: PacksDetailsStore.Intent) {
        when (intent) {
            is PacksDetailsStore.Intent.Close -> onClose()
            else -> store.accept(intent)
        }
    }
}
