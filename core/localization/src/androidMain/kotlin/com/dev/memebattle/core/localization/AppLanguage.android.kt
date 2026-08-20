package com.dev.memebattle.core.localization

import android.content.Context
import java.util.Locale

private var androidContext: Context? = null
private const val PREFS_NAME = "app_language_prefs"
private const val KEY_LANG = "saved_language"

fun initAndroidLocalization(context: Context) {
    androidContext = context.applicationContext
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedCode = prefs.getString(KEY_LANG, null)
    val initialLang = if (savedCode != null) {
        AppLanguage.fromCode(savedCode)
    } else {
        AppLanguage.fromCode(Locale.getDefault().language)
    }
    setAppLanguage(initialLang)
}

actual fun initAppLanguage() {
    // Initialized via initAndroidLocalization(context)
}

actual fun setAppLanguage(language: AppLanguage) {
    currentAppLanguage = language
    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)

    androidContext?.let { ctx ->
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_LANG, language.code).apply()

            val resources = ctx.resources
            val config = resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
