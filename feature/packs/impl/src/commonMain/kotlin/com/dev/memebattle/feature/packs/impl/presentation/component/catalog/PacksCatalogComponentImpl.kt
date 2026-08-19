package com.dev.memebattle.feature.packs.impl.presentation.component.catalog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

class PacksCatalogComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val onNavigateToDetails: (packId: String, kind: String) -> Unit,
    private val onNavigateToCreate: () -> Unit,
    private val onNavigateToEdit: (packId: String, kind: String) -> Unit,
    private val onNavigateBack: () -> Unit,
    /** Если задан (из диплинка) — авто-откроет экран деталей при старте */
    private val initialOpenPackId: String? = null,
    /** Тип пака для авто-открытия: "meme" или "situation" */
    private val initialOpenPackKind: String? = null,
) : PacksCatalogComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val store = PacksCatalogStoreFactory(storeFactory, packRepository).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<PacksCatalogStore.State> = store.stateFlow(scope)

    override val effects: SharedFlow<PacksCatalogStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    init {
        store.accept(PacksCatalogStore.Intent.Init)
        // Deep link: если пришли по ссылке /pack/{id} — сразу открываем детали
        if (initialOpenPackId != null) {
            val kind = if (initialOpenPackKind == "situation") "situation" else "meme"
            onNavigateToDetails(initialOpenPackId, kind)
        }
    }

    override fun onIntent(intent: PacksCatalogStore.Intent) {
        when (intent) {
            // Навигационные эффекты перехватываем и проксируем вверх через колбэки
            is PacksCatalogStore.Intent.OpenDetails -> {
                val kind = if (state.value.activeType == PacksCatalogStore.PackType.Situations) "situation" else "meme"
                onNavigateToDetails(intent.packId, kind)
            }
            is PacksCatalogStore.Intent.OpenEdit -> {
                val kind = if (state.value.activeType == PacksCatalogStore.PackType.Situations) "situation" else "meme"
                onNavigateToEdit(intent.packId, kind)
            }
            is PacksCatalogStore.Intent.OpenCreate -> onNavigateToCreate()
            is PacksCatalogStore.Intent.GoBack -> onNavigateBack()
            else -> store.accept(intent)
        }
    }
}
