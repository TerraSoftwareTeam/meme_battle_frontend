package com.dev.memebattle.feature.home.impl.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.dev.memebattle.feature.home.impl.presentation.component.HomeComponent
import com.dev.memebattle.feature.home.impl.presentation.component.HomeComponentImpl
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.component.packpicker.PackPickerComponent
import com.dev.memebattle.feature.home.impl.presentation.view.create.CreateLobbyView
import com.dev.memebattle.feature.home.impl.presentation.view.menu.HomeMenuView
import com.dev.memebattle.feature.home.impl.presentation.view.packpicker.PackPickerView

@OptIn(com.arkivanov.decompose.ExperimentalDecomposeApi::class)
@Composable
fun HomeView(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val panels by component.panels.subscribeAsState()

    Box(modifier = modifier.fillMaxSize()) {
        HomeMenuView(
            component = panels.main.instance,
            detailsComponent = when (val details = panels.details?.instance) {
                is CreateLobbyComponent -> details
                else -> null
            },
            packPickerComponent = when (val details = panels.details?.instance) {
                is PackPickerComponent -> details
                else -> null
            }
        )
    }
}
