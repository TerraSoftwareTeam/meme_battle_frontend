package com.dev.memebattle.host.root.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.dev.memebattle.host.root.presentation.component.RootComponent

@Composable
fun RootScreen(
    component: RootComponent
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Children(
            stack = component.childStack,
            animation = stackAnimation(fade())
        ) { child ->
            child.instance.hostLayer.Render(
                entry = child.instance.entry,
                component = child.instance.component,
                onNavigate = component::onNavigate
            )
        }
    }
}
