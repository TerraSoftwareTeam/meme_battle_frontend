package com.dev.memebattle.core.localization

private fun getLocalStorageLanguage(): String? = js("window.localStorage.getItem('app_language')")
private fun setLocalStorageLanguage(code: String): Unit = js("window.localStorage.setItem('app_language', code)")

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun setNavigatorLanguage(langCode: String): Unit = js("""
    (function(lang) {
        try {
            Object.defineProperty(navigator, 'language', {
                get: function() { return lang; },
                configurable: true
            });
            Object.defineProperty(navigator, 'languages', {
                get: function() { return [lang]; },
                configurable: true
            });
            document.documentElement.lang = lang;
        } catch (e) {
            console.error(e);
        }
    })(langCode)
""")

actual fun initAppLanguage() {
    val savedCode = getLocalStorageLanguage()
    val initialLang = if (savedCode != null) {
        AppLanguage.fromCode(savedCode)
    } else {
        AppLanguage.RUSSIAN
    }
    setAppLanguage(initialLang)
}

actual fun setAppLanguage(language: AppLanguage) {
    currentAppLanguage = language
    setLocalStorageLanguage(language.code)
    setNavigatorLanguage(language.code)
}
