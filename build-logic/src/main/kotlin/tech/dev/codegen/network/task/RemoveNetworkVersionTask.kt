package tech.dev.codegen.network.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class RemoveNetworkVersionTask : DefaultTask() {

    @get:Input
    @set:Option(option = "service", description = "Target service name (e.g. auth)")
    var serviceName: String = ""

    @get:Input
    @get:Optional
    @set:Option(option = "version", description = "Version to remove (e.g. 3). If empty, removes the latest version.")
    var version: String = ""

    @TaskAction
    fun run() {
        if (serviceName.isBlank()) throw IllegalArgumentException("--service is required")

        val networkDir = File(project.rootDir, "network")
        if (!networkDir.exists()) return

        val serviceDir = File(networkDir, serviceName)
        if (!serviceDir.exists()) return

        val targetVersion = if (version.isNotBlank()) {
            version.toInt()
        } else {
            val currentVersions = serviceDir.listFiles { f -> f.isDirectory && f.name.startsWith("v") }
                ?.mapNotNull { it.name.substringAfter("v").toIntOrNull() } ?: emptyList()
            currentVersions.maxOrNull() ?: throw IllegalArgumentException("No versions found for service $serviceName")
        }

        val moduleName = "v$targetVersion"
        val moduleDir = File(serviceDir, moduleName)
        val modulePath = ":network:$serviceName:$moduleName"

        if (!moduleDir.exists()) {
            throw IllegalArgumentException("Module $modulePath does not exist!")
        }

        println("🗑️ Removing network module $modulePath...")
        moduleDir.deleteRecursively()

        println("📝 Removing from settings.gradle.kts...")
        val settings = File(project.rootDir, "settings.gradle.kts")
        if (settings.exists()) {
            val content = settings.readText()
            val directive = "include(\"$modulePath\")"
            if (content.contains(directive)) {
                settings.writeText(content.replace("\n$directive", ""))
            }
        }

        val currentDir = File(serviceDir, "current")
        val currentBuildFile = File(currentDir, "build.gradle.kts")
        if (currentBuildFile.exists()) {
            val currentContent = currentBuildFile.readText()
            if (currentContent.contains("project(\":network:$serviceName:v$targetVersion\")") || 
                currentContent.contains("projects.network.$serviceName.v$targetVersion")) {
                println("🔄 Alias depends on the removed version. Updating alias...")
                val remainingVersions = serviceDir.listFiles { f -> f.isDirectory && f.name.startsWith("v") }
                    ?.mapNotNull { it.name.substringAfter("v").toIntOrNull() }
                    ?.filter { it != targetVersion } ?: emptyList()
                
                val newTarget = remainingVersions.maxOrNull()
                if (newTarget != null) {
                    currentBuildFile.writeText(
                        currentContent
                            .replace("project(\":network:$serviceName:v$targetVersion\")", "project(\":network:$serviceName:v$newTarget\")")
                            .replace("projects.network.$serviceName.v$targetVersion", "projects.network.$serviceName.v$newTarget")
                            .replace("com.dev.network.$serviceName.v$targetVersion", "com.dev.network.$serviceName.v$newTarget")
                    )
                    println("✅ Switched alias to v$newTarget")
                } else {
                    currentBuildFile.writeText(
                        """
                        plugins { id("kmp.dev.library") }
                        kotlin {
                            androidLibrary {
                                namespace = "com.dev.memebattle.network.${serviceName}.current"
                                compileSdk = libs.versions.compileSdk.get().toInt()
                                minSdk = libs.versions.minSdk.get().toInt()
                            }
                        }
                        """.trimIndent()
                    )
                    println("⚠️ No versions left! Alias :network:$serviceName:current is now empty.")
                }
            }
        }

        println("✅ Module $moduleName successfully removed.")
    }
}
