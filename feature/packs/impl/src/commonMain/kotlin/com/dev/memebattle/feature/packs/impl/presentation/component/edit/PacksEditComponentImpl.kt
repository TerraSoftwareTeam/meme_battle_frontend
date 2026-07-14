package com.dev.memebattle.feature.packs.impl.presentation.component.edit

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.memebattle.core.navigation.output.NotificationType
import com.dev.memebattle.feature.packs.impl.presentation.store.edit.PacksEditStore
import com.dev.memebattle.feature.packs.impl.presentation.store.edit.PacksEditStoreFactory
import com.dev.network.media.current.api.MediaApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

class PacksEditComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val mediaApiService: MediaApiService,
    private val packId: String,
    private val kind: String,
    private val onClose: () -> Unit,
    private val onSaved: (packId: String, kind: String) -> Unit,
    private val onShowNotification: (message: String, type: NotificationType) -> Unit,
) : PacksEditComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val store = PacksEditStoreFactory(storeFactory, packRepository, mediaApiService).create()

    init {
        store.accept(PacksEditStore.Intent.Load(packId, kind))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<PacksEditStore.State> = store.stateFlow(scope)

    override val effects: SharedFlow<PacksEditStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override fun onIntent(intent: PacksEditStore.Intent) {
        when (intent) {
            is PacksEditStore.Intent.Close -> onClose()
            is PacksEditStore.Intent.Save -> {
                // intercept Saved effect in the component layer
                store.accept(intent)
            }
            else -> store.accept(intent)
        }
    }

    override fun showNotification(message: String, isError: Boolean) {
        onShowNotification(
            message,
            if (isError) NotificationType.Negative else NotificationType.Positive
        )
    }

    override fun navigateToDetails(packId: String, kind: String) {
        onSaved(packId, kind)
    }
}
