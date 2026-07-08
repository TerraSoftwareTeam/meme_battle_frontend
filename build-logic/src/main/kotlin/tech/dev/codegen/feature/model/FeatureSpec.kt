package tech.dev.codegen.feature.model

import java.io.File

data class FeatureSpec(
    val name: String,
    val layer: String,
    val withDomain: Boolean,
    val withData: Boolean,
    val rootDir: File
) {
    val pascal = name.replaceFirstChar { it.uppercase() }
    val kebab = name.replace(Regex("([a-z])([A-Z]+)")) { "${it.groupValues[1]}-${it.groupValues[2]}" }.lowercase()
    val basePkg = "com.dev.memebattle.feature.$name"
    val pkg = name.lowercase()
    val featureDir = File(rootDir, "feature/$kebab")
}
