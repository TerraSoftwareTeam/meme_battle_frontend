package com.dev.memebattle.core.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val label: String) {
    RUSSIAN("ru", "RU"),
    ENGLISH("en", "EN");

    companion object {
        fun fromCode(code: String?): AppLanguage = when (code?.lowercase()) {
            "en" -> ENGLISH
            else -> RUSSIAN
        }
    }
}

var currentAppLanguage by mutableStateOf(AppLanguage.RUSSIAN)
    internal set

expect fun initAppLanguage()
expect fun setAppLanguage(language: AppLanguage)
