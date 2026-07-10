package com.dev.memebattle.core.ui.notification

import com.dev.memebattle.core.navigation.output.NotificationType

/**
 * Модель глобального уведомления.
 *
 * @param id уникальный идентификатор — используется для предотвращения повторного показа
 *           одного и того же уведомления при рекомпозиции.
 * @param message текст уведомления
 * @param type визуальный акцент (Positive / Neutral / Negative)
 * @param actionLabel подпись кнопки действия, если нужна
 * @param onAction callback кнопки действия
 */
data class AppNotification(
    val id: Long,
    val message: String,
    val type: NotificationType,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)
