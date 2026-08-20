package com.dev.memebattle.core.localization

import platform.Foundation.NSUserDefaults

private const val KEY_LANG = "app_language"

actual fun initAppLanguage() {
    val savedCode = NSUserDefaults.standardUserDefaults.stringForKey(KEY_LANG)
    val initialLang = if (savedCode != null) {
        AppLanguage.fromCode(savedCode)
    } else {
        AppLanguage.RUSSIAN
    }
    setAppLanguage(initialLang)
}

actual fun setAppLanguage(language: AppLanguage) {
    currentAppLanguage = language
    NSUserDefaults.standardUserDefaults.setObject(language.code, forKey = KEY_LANG)
}
