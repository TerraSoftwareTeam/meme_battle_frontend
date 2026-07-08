package com.dev.memebattle.feature.home.impl.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.dev.memebattle.feature.home.impl.presentation.store.HomeStore
import com.dev.memebattle.feature.home.impl.presentation.store.HomeStoreFactory


class HomeComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory
) : HomeComponent, ComponentContext by componentContext {
    
    private val scope = coroutineScope()
    private val store = HomeStoreFactory(storeFactory).create()

    private val labelsFlow = store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val effects: SharedFlow<HomeStore.Effect> = labelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HomeStore.State> = store.stateFlow(scope)

    override val output: kotlinx.coroutines.flow.Flow<com.dev.memebattle.core.navigation.output.NavigationOutput> = kotlinx.coroutines.flow.emptyFlow()

    override fun onIntent(intent: HomeStore.Intent) = store.accept(intent)
}
