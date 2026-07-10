package com.dev.memebattle.core.ui.notification

import kotlinx.coroutines.flow.StateFlow


interface NotificationController {
    val notifications: StateFlow<AppNotification?>

    fun show(notification: AppNotification)

    fun dismiss()
}
