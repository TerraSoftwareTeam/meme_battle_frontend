package com.dev.memebattle.feature.packs.impl.presentation.component.create

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStore
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStoreFactory
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.network.media.current.api.MediaApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

import com.dev.memebattle.core.navigation.output.NotificationType

class PacksCreateComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val mediaApiService: MediaApiService,
    private val onClose: () -> Unit,
    private val onShowNotification: (message: String, type: NotificationType) -> Unit,
    private val onCreatedCallback: (packId: String, kind: String) -> Unit,
) : PacksCreateComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val store = PacksCreateStoreFactory(storeFactory, packRepository, mediaApiService).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<PacksCreateStore.State> = store.stateFlow(scope)

    override val effects: SharedFlow<PacksCreateStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override fun onIntent(intent: PacksCreateStore.Intent) {
        when (intent) {
            is PacksCreateStore.Intent.Close -> onClose()
            else -> store.accept(intent)
        }
    }

    override fun showNotification(message: String, isError: Boolean) {
        onShowNotification(
            message,
            if (isError) NotificationType.Negative else NotificationType.Neutral
        )
    }

    override fun onCreated(packId: String, kind: String) {
        onCreatedCallback(packId, kind)
    }
}
