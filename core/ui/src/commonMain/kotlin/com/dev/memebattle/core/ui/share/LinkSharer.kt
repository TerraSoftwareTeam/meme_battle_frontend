package com.dev.memebattle.core.ui.share

import androidx.compose.runtime.Composable

/**
 * Возвращает функцию для шарринга ссылки.
 * - На Android: вызывается системный Intent.ACTION_SEND (диалог "Поделиться"). Если софт/контекст не позволяет — копирует в буфер.
 * - На Web (WasmJs) / iOS: копирует ссылку в буфер обмена.
 */
@Composable
expect fun rememberLinkSharer(): (url: String, title: String) -> Unit
