package com.dev.memebattle.core.ui.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationControllerImpl : NotificationController {

    private val _notifications = MutableStateFlow<AppNotification?>(null)
    override val notifications: StateFlow<AppNotification?> = _notifications.asStateFlow()

    override fun show(notification: AppNotification) {
        _notifications.value = notification
    }

    override fun dismiss() {
        _notifications.value = null
    }
}
