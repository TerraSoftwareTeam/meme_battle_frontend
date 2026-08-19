package com.dev.memebattle.core.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.browser.window
import kotlinx.coroutines.delay

private fun copyToClipboardJs(text: String) {
    try {
        window.navigator.clipboard.writeText(text)
    } catch (_: Throwable) {}
}

@Composable
actual fun rememberLinkSharer(): (url: String, title: String) -> Unit {
    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2500)
            showToast = false
        }
        ShareToastPopup(
            message = "Ссылка скопирована в буфер обмена!",
            visible = showToast,
        )
    }

    val clipboardManager = LocalClipboardManager.current
    return remember(clipboardManager) {
        { url, _ ->
            try {
                clipboardManager.setText(AnnotatedString(url))
            } catch (_: Throwable) {
                copyToClipboardJs(url)
            }
            showToast = true
        }
    }
}
