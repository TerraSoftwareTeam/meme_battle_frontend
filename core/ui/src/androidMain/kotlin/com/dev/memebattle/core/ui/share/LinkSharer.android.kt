package com.dev.memebattle.core.ui.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
actual fun rememberLinkSharer(): (url: String, title: String) -> Unit {
    val context = LocalContext.current
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

    return remember(context) {
        { url, title ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                if (title.isNotEmpty()) {
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TITLE, title)
                }
            }
            val chooser = Intent.createChooser(sendIntent, title.ifEmpty { "Поделиться" })
            try {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (_: Exception) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText(title.ifEmpty { "Ссылка" }, url))
                Toast.makeText(context, "Ссылка скопирована в буфер обмена!", Toast.LENGTH_SHORT).show()
                showToast = true
            }
        }
    }
}
