package com.dev.memebattle.host.root.di

import com.dev.memebattle.core.navigation.output.NavigationOutputHandler
import com.dev.memebattle.core.navigation.layer.HostLayer
import com.dev.memebattle.core.ui.notification.NotificationController
import com.dev.memebattle.core.ui.notification.NotificationControllerImpl
import com.dev.memebattle.host.root.presentation.handler.NotificationOutputHandler
import com.dev.memebattle.host.root.presentation.layer.GlobalHostLayer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val rootHostModule = module {
    singleOf(::GlobalHostLayer) bind HostLayer::class

    // Синглтон контроллера уведомлений — доступен из любого слоя через inject()
    singleOf(::NotificationControllerImpl) bind NotificationController::class

    // Обработчик NavigationOutput.ShowNotification в цепочке Chain-of-Responsibility
    singleOf(::NotificationOutputHandler) bind NavigationOutputHandler::class
}

