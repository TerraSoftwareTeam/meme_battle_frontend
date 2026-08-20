package com.dev.memebattle.feature.home.impl.presentation.component

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.value.Value
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.feature.home.impl.presentation.component.menu.HomeMenuComponent

interface HomeComponent : FeatureComponent {
    @OptIn(ExperimentalDecomposeApi::class)
    val panels: Value<ChildPanels<
            HomeComponentImpl.MainConfig,
            HomeMenuComponent,
            HomeComponentImpl.DetailsConfig,
            Any,
            Nothing,
            Nothing>>

    fun setAdaptiveMode(mode: ChildPanelsMode)
}
