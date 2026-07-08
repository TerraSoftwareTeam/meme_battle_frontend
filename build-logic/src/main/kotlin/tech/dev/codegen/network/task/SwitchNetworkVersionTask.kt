package tech.dev.codegen.network.task
import org.gradle.api.tasks.Input
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import tech.dev.codegen.network.patch.SettingsGradlePatcher

abstract class SwitchNetworkVersionTask : DefaultTask() {

    @get:Input
    @set:Option(option = "service", description = "Target service name (e.g. auth)")
    var serviceName: String = ""

    @get:Input
    @set:Option(option = "version", description = "Version number to switch to (e.g. 3)")
    var version: String = ""

    @TaskAction
    fun run() {
        if (serviceName.isBlank()) throw IllegalArgumentException("--service is required")
        if (version.isBlank()) throw IllegalArgumentException("--version is required")

        val networkDir = File(project.rootDir, "network")
        val serviceDir = File(networkDir, serviceName)
        val moduleName = "v$version"
        val moduleDir = File(serviceDir, moduleName)

        if (!moduleDir.exists()) {
            throw IllegalArgumentException("Version v$version for service $serviceName does not exist!")
        }

        println("🔄 Switching $serviceName to version v$version...")

        val currentDir = File(serviceDir, "current")
        if (!currentDir.exists()) currentDir.mkdirs()

        val accessorName = serviceName.split("_").mapIndexed { i, s -> if (i > 0) s.capitalize() else s }.joinToString("")
        File(currentDir, "build.gradle.kts").writeText(
            """
            plugins { id("kmp.dev.library") }
            kotlin {
                androidLibrary {
                    namespace = "com.dev.memebattle.network.${serviceName}.current"
                    compileSdk = libs.versions.compileSdk.get().toInt()
                    minSdk = libs.versions.minSdk.get().toInt()
                }
                sourceSets {
                    commonMain.dependencies {
                        api(projects.network.${accessorName}.v$version)
                        implementation(projects.core.network)
                        implementation(libs.koin.core)
                    }
                }
            }
            """.trimIndent()
        )

        SettingsGradlePatcher.include(":network:$serviceName:current", project.rootDir)
        println("✅ Switched alias :network:$serviceName:current to point to v$version.")
        println("Make sure :network:$serviceName:current is included in settings.gradle.kts!")
    }
}
