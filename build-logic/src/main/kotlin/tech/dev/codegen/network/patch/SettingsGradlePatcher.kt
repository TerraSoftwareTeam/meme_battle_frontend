package tech.dev.codegen.network.patch

import java.io.File

object SettingsGradlePatcher {
    fun include(modulePath: String, rootDir: File) {
        val settings = File(rootDir, "settings.gradle.kts")
        if (!settings.exists()) return
        
        val content = settings.readText()
        val directive = "include(\"$modulePath\")"
        
        if (!content.contains(directive)) {
            settings.appendText("\n$directive\n")
            println("✅ Added $modulePath to settings.gradle.kts")
        }
    }
}
