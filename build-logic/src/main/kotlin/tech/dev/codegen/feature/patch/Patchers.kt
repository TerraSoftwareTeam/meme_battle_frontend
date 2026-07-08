package tech.dev.codegen.feature.patch

import java.io.File

object Patchers {
    fun patchSettings(rootDir: File, featurePascal: String, modules: List<String>) {
        val settings = File(rootDir, "settings.gradle.kts")
        if (!settings.exists()) return
        val current = settings.readText()
        val missing = modules.map { "include(\"$it\")" }.filter { !current.contains(it) }
        if (missing.isEmpty()) return
        
        val block = buildString {
            append("\n// region FEATURE $featurePascal\n")
            missing.forEach { append(it).append("\n") }
            append("// endregion FEATURE $featurePascal\n")
        }
        
        settings.appendText(block)
        println("Updated settings.gradle.kts: added ${missing.size} modules.")
    }
}
