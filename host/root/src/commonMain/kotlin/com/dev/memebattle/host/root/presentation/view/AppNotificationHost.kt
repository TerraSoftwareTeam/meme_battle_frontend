package com.dev.memebattle.host.root.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dev.memebattle.core.navigation.output.NotificationType
import com.dev.memebattle.core.ui.notification.AppNotification
import com.dev.memebattle.core.ui.notification.NotificationController

@Composable
fun AppNotificationHost(
    controller: NotificationController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val current by controller.notifications.collectAsState()

    LaunchedEffect(current?.id) {
        val notification = current ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = notification.message,
            actionLabel = notification.actionLabel,
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            notification.onAction?.invoke()
        }
        controller.dismiss()
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 16.dp),
        ) { data ->
            NotificationSnackbar(
                data = data,
                notification = current,
            )
        }
    }
}

@Composable
private fun NotificationSnackbar(
    data: SnackbarData,
    notification: AppNotification?,
) {
    val containerColor = when (notification?.type) {
        NotificationType.Positive -> Color(0xFF1B5E20) // насыщенный зелёный
        NotificationType.Negative -> Color(0xFF93000A) // из темы ErrorContainer
        NotificationType.Neutral, null -> Color(0xFF4F378B) // PrimaryContainer из темы
    }

    Snackbar(
        snackbarData = data,
        containerColor = containerColor,
        contentColor = Color(0xFFE6E1E5),  // OnBackground
        actionColor = Color(0xFFD0BCFF),   // Primary — контрастная кнопка
        actionContentColor = Color(0xFFD0BCFF),
        dismissActionContentColor = Color(0xFFCAC4D0), // OnSurfaceVariant
    )
}
