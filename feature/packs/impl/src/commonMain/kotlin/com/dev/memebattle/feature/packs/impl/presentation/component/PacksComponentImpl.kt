package com.dev.memebattle.feature.packs.impl.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.activateExtra
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.dismissDetails
import com.arkivanov.decompose.router.panels.dismissExtra
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.memebattle.core.navigation.output.NavigationOutput
import com.dev.memebattle.feature.packs.impl.presentation.component.catalog.PacksCatalogComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.catalog.PacksCatalogComponentImpl
import com.dev.memebattle.feature.packs.impl.presentation.component.create.PacksCreateComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.create.PacksCreateComponentImpl
import com.dev.memebattle.feature.packs.impl.presentation.component.details.PacksDetailsComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.details.PacksDetailsComponentImpl
import com.dev.memebattle.feature.packs.impl.presentation.component.edit.PacksEditComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.edit.PacksEditComponentImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable

import com.dev.network.media.current.api.MediaApiService

class PacksComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val mediaApiService: MediaApiService,
) : PacksComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val outputChannel = Channel<NavigationOutput>(Channel.BUFFERED)

    // ── Конфигурации панелей ─────────────────────────────────────────────────

    @Serializable
    sealed interface MainConfig {
        @Serializable
        data object Catalog : MainConfig
    }

    @Serializable
    sealed interface DetailsConfig {
        @Serializable
        data class Details(val packId: String, val kind: String = "meme") : DetailsConfig
    }

    @Serializable
    sealed interface ExtraConfig {
        @Serializable
        data object Create : ExtraConfig
        @Serializable
        data class Edit(val packId: String, val kind: String) : ExtraConfig
    }

    // ── PanelsNavigation ─────────────────────────────────────────────────────

    @OptIn(ExperimentalDecomposeApi::class)
    private val panelsNavigation = PanelsNavigation<MainConfig, DetailsConfig, ExtraConfig>()

    @OptIn(ExperimentalDecomposeApi::class)
    @Suppress("UNCHECKED_CAST")
    override val panels: Value<ChildPanels<MainConfig, PacksCatalogComponent, DetailsConfig, PacksDetailsComponent, ExtraConfig, PacksExtraComponent>> =
        childPanels(
            source = panelsNavigation,
            // Triple<KSerializer, KSerializer, KSerializer> для 3-панельного варианта
            serializers = Triple(
                MainConfig.serializer(),
                DetailsConfig.serializer(),
                ExtraConfig.serializer(),
            ),
            initialPanels = { Panels(main = MainConfig.Catalog) },
            handleBackButton = true,
            mainFactory = { config, ctx ->
                @Suppress("UNCHECKED_CAST")
                when (config) {
                    is MainConfig.Catalog -> createCatalog(ctx)
                }
            },
            detailsFactory = { config, ctx ->
                when (config) {
                    is DetailsConfig.Details -> createDetails(ctx, config.packId, config.kind)
                }
            },
            extraFactory = { config, ctx ->
                when (config) {
                    is ExtraConfig.Create -> PacksExtraComponent.Create(createCreate(ctx))
                    is ExtraConfig.Edit -> PacksExtraComponent.Edit(createEdit(ctx, config.packId, config.kind))
                }
            },
        )

    // ── Фабрики дочерних компонентов ─────────────────────────────────────────

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createCatalog(ctx: ComponentContext): PacksCatalogComponent =
        PacksCatalogComponentImpl(
            componentContext = ctx,
            storeFactory = storeFactory,
            packRepository = packRepository,
            onNavigateToDetails = { packId, kind ->
                panelsNavigation.activateDetails(DetailsConfig.Details(packId, kind))
            },
            onNavigateToCreate = {
                panelsNavigation.activateExtra(ExtraConfig.Create)
            },
            onNavigateToEdit = { packId, kind ->
                panelsNavigation.activateExtra(ExtraConfig.Edit(packId, kind))
            },
            onNavigateBack = {
                outputChannel.trySend(NavigationOutput.Back)
            },
        )

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createDetails(ctx: ComponentContext, packId: String, kindStr: String): PacksDetailsComponent {
        val kind = if (kindStr == "situation") {
            com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore.PackKind.Situation
        } else {
            com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore.PackKind.Meme
        }
        return PacksDetailsComponentImpl(
            componentContext = ctx,
            storeFactory = storeFactory,
            packRepository = packRepository,
            packId = packId,
            kind = kind,
            onClose = { panelsNavigation.dismissDetails() },
        )
    }

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createCreate(ctx: ComponentContext): PacksCreateComponent =
        PacksCreateComponentImpl(
            componentContext = ctx,
            storeFactory = storeFactory,
            packRepository = packRepository,
            mediaApiService = mediaApiService,
            onClose = { panelsNavigation.dismissExtra() },
            onShowNotification = { message, type ->
                outputChannel.trySend(NavigationOutput.ShowNotification(message = message, type = type))
            },
            onCreatedCallback = { packId, kind ->
                panelsNavigation.dismissExtra()
                panelsNavigation.activateDetails(DetailsConfig.Details(packId, kind))
            }
        )

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createEdit(ctx: ComponentContext, packId: String, kind: String): PacksEditComponent =
        PacksEditComponentImpl(
            componentContext = ctx,
            storeFactory = storeFactory,
            packRepository = packRepository,
            mediaApiService = mediaApiService,
            packId = packId,
            kind = kind,
            onClose = { panelsNavigation.dismissExtra() },
            onSaved = { savedPackId, savedKind ->
                panelsNavigation.dismissExtra()
                panelsNavigation.activateDetails(DetailsConfig.Details(savedPackId, savedKind))
                outputChannel.trySend(NavigationOutput.ShowNotification(message = "Пэк успешно обновлён!", type = com.dev.memebattle.core.navigation.output.NotificationType.Positive))
            },
            onShowNotification = { message, type ->
                outputChannel.trySend(NavigationOutput.ShowNotification(message = message, type = type))
            }
        )

    @OptIn(ExperimentalDecomposeApi::class)
    override fun setAdaptiveMode(mode: ChildPanelsMode) {
        panelsNavigation.setMode(mode)
    }

    override val output: Flow<NavigationOutput> = outputChannel.receiveAsFlow()
}
