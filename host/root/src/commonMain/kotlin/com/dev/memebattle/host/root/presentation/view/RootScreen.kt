package com.dev.memebattle.host.root.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.dev.memebattle.core.ui.notification.NotificationController
import com.dev.memebattle.host.root.presentation.component.RootComponent
import org.koin.compose.koinInject

@Composable
fun RootScreen(
    component: RootComponent,
) {
    val notificationController: NotificationController = koinInject()

    AppNotificationHost(controller = notificationController) {
        Box(modifier = Modifier.fillMaxSize()) {
            Children(
                stack = component.childStack,
                animation = stackAnimation(slide())
            ) { child ->
                child.instance.hostLayer.Render(
                    entry = child.instance.entry,
                    component = child.instance.component,
                    onNavigate = component::onNavigate
                )
            }
        }
    }
}

