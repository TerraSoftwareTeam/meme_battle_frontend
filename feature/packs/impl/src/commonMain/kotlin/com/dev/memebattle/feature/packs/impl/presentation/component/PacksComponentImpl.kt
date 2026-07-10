package com.dev.memebattle.feature.packs.impl.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.dev.memebattle.feature.packs.impl.presentation.store.PacksStore
import com.dev.memebattle.feature.packs.impl.presentation.store.PacksStoreFactory


class PacksComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
) : PacksComponent, ComponentContext by componentContext {
    
    private val scope = coroutineScope()
    private val store = PacksStoreFactory(storeFactory, packRepository).create()

    private val labelsFlow = store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val effects: SharedFlow<PacksStore.Effect> = labelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<PacksStore.State> = store.stateFlow(scope)

    override val output: kotlinx.coroutines.flow.Flow<com.dev.memebattle.core.navigation.output.NavigationOutput> = kotlinx.coroutines.flow.emptyFlow()

    override fun onIntent(intent: PacksStore.Intent) = store.accept(intent)
}
