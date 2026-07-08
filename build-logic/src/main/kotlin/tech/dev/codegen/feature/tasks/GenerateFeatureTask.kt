package tech.dev.codegen.feature.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import tech.dev.codegen.feature.model.FeatureSpec
import tech.dev.codegen.feature.generator.FeatureGenerator
import java.io.File

import javax.inject.Inject
import org.gradle.api.file.ProjectLayout

abstract class GenerateFeatureTask : DefaultTask() {
    @get:Inject
    abstract val projectLayout: ProjectLayout

    private var featureName: String = ""
    private var layerOpt: String = "Global"
    private var withDomain: Boolean = false
    private var withData: Boolean = false

    @Option(option = "name", description = "Feature name in camelCase, e.g. profileSettings")
    fun setFeatureName(name: String) { this.featureName = name }

    @Option(option = "layer", description = "Layer name (e.g. Main, Auth, Global). Default is Global.")
    fun setLayer(layer: String) { this.layerOpt = layer }

    @Option(option = "with-domain", description = "Generate domain package")
    fun setWithDomain(value: Boolean) { this.withDomain = value }

    @Option(option = "with-data", description = "Generate data package (requires domain)")
    fun setWithData(value: Boolean) { this.withData = value }

    @TaskAction
    fun generate() {
        if (featureName.isBlank()) throw IllegalArgumentException("--name=<featureName> is required")
        if (withData && !withDomain) {
            println("⚠️  --with-data requires --with-domain. Enabling --with-domain automatically.")
            withDomain = true
        }

        val rootDir = projectLayout.projectDirectory.asFile

        val spec = FeatureSpec(featureName, layerOpt, withDomain, withData, rootDir)
        if (spec.featureDir.exists()) {
            println("Feature directory already exists: ${spec.featureDir}. Generating only missing modules.")
        }

        FeatureGenerator.generate(spec)
        println("✅ Feature skeleton '$featureName' generated successfully.")
    }
}
