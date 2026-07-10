package com.dev.memebattle.host.root.presentation.handler

import com.dev.memebattle.core.navigation.output.NavigationContext
import com.dev.memebattle.core.navigation.output.NavigationOutput
import com.dev.memebattle.core.navigation.output.NavigationOutputHandler
import com.dev.memebattle.core.ui.notification.AppNotification
import com.dev.memebattle.core.ui.notification.NotificationController

class NotificationOutputHandler(
    private val controller: NotificationController,
) : NavigationOutputHandler {

    private var idCounter = 0L

    override fun canHandle(output: NavigationOutput, ctx: NavigationContext): Boolean =
        output is NavigationOutput.ShowNotification

    override fun handle(output: NavigationOutput, ctx: NavigationContext) {
        val n = output as NavigationOutput.ShowNotification
        controller.show(
            AppNotification(
                id = ++idCounter,
                message = n.message,
                type = n.type,
                actionLabel = n.actionLabel,
                onAction = n.onAction,
            )
        )
    }
}

