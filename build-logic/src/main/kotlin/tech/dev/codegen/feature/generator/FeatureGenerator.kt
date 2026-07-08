package tech.dev.codegen.feature.generator

import tech.dev.codegen.feature.model.FeatureSpec
import tech.dev.codegen.feature.template.FeatureTemplates
import tech.dev.codegen.feature.template.GradleTemplates
import tech.dev.codegen.feature.patch.Patchers
import java.io.File

object FeatureGenerator {

    fun generate(spec: FeatureSpec) {
        val modules = mutableListOf<String>()

        if (!File(spec.featureDir, "api").exists()) {
            createApi(spec)
            modules += ":feature:${spec.kebab}:api"
        }

        if (!File(spec.featureDir, "impl").exists()) {
            createImpl(spec)
            modules += ":feature:${spec.kebab}:impl"
        }

        Patchers.patchSettings(spec.rootDir, spec.pascal, modules)
    }

    private fun createApi(spec: FeatureSpec) {
        val dir = File(spec.featureDir, "api")
        writeBuild(dir, GradleTemplates.apiBuild("${spec.basePkg}.api"))
        val src = srcDir(dir, "${spec.basePkg}.api")
        
        val routeDir = File(src, "route").also { it.mkdirs() }
        write(File(routeDir, "${spec.pascal}Route.kt"), FeatureTemplates.apiRoute(spec.basePkg, spec.pascal))
        
        val entryDir = File(src, "entry").also { it.mkdirs() }
        write(File(entryDir, "${spec.pascal}FeatureEntry.kt"), FeatureTemplates.apiFeatureEntry(spec.basePkg, spec.pascal))
    }

    private fun createImpl(spec: FeatureSpec) {
        val dir = File(spec.featureDir, "impl")
        writeBuild(dir, GradleTemplates.implBuild("${spec.basePkg}.impl", spec.kebab))
        val src = srcDir(dir, "${spec.basePkg}.impl")
        
        val featureDir = File(src, "feature").also { it.mkdirs() }
        write(File(featureDir, "${spec.pascal}FeatureEntryImpl.kt"), FeatureTemplates.implFeatureEntry(spec.basePkg, spec.pascal))
        
        val diDir = File(src, "di").also { it.mkdirs() }
        write(File(diDir, "${spec.pascal}Di.kt"), FeatureTemplates.diModule(spec.basePkg, spec.pascal, spec.withDomain, spec.withData))
        
        val compDir = File(src, "presentation/component").also { it.mkdirs() }
        write(File(compDir, "${spec.pascal}Component.kt"), FeatureTemplates.component(spec.basePkg, spec.pascal))
        write(File(compDir, "${spec.pascal}ComponentImpl.kt"), FeatureTemplates.componentImpl(spec.basePkg, spec.pascal, spec.withDomain))
        
        val storeDir = File(src, "presentation/store").also { it.mkdirs() }
        write(File(storeDir, "${spec.pascal}Store.kt"), FeatureTemplates.store(spec.basePkg, spec.pascal))
        write(File(storeDir, "${spec.pascal}StoreFactory.kt"), FeatureTemplates.storeFactory(spec.basePkg, spec.pascal, spec.withDomain))
        
        val viewDir = File(src, "presentation/view").also { it.mkdirs() }
        write(File(viewDir, "${spec.pascal}View.kt"), FeatureTemplates.view(spec.basePkg, spec.pascal))
        
        if (spec.withDomain) {
            val domainDir = File(src, "domain").also { it.mkdirs() }
            write(File(domainDir, "${spec.pascal}Model.kt"), "package ${spec.basePkg}.impl.domain\n\ndata class ${spec.pascal}Model(val id: String = \"\")\n")
            write(File(domainDir, "${spec.pascal}Repository.kt"), "package ${spec.basePkg}.impl.domain\n\ninterface ${spec.pascal}Repository { suspend fun getAll(): List<${spec.pascal}Model> }\n")
            write(File(domainDir, "${spec.pascal}Interactor.kt"), "package ${spec.basePkg}.impl.domain\n\nclass ${spec.pascal}Interactor(private val repository: ${spec.pascal}Repository) {\n    suspend fun getAll(): List<${spec.pascal}Model> = repository.getAll()\n}\n")
        }
        
        if (spec.withData) {
            val dataDir = File(src, "data").also { it.mkdirs() }
            write(File(dataDir, "${spec.pascal}RepositoryImpl.kt"), "package ${spec.basePkg}.impl.data\n\nimport ${spec.basePkg}.impl.domain.${spec.pascal}Repository\nimport ${spec.basePkg}.impl.domain.${spec.pascal}Model\n\nclass ${spec.pascal}RepositoryImpl : ${spec.pascal}Repository {\n    override suspend fun getAll(): List<${spec.pascal}Model> = emptyList()\n}\n")
        }
    }

    private fun writeBuild(dir: File, content: String) {
        dir.mkdirs()
        File(dir, "build.gradle.kts").writeText(content + "\n")
        File(dir, ".gitignore").writeText("/build\n")
    }

    private fun srcDir(moduleDir: File, pkg: String): File =
        File(moduleDir, "src/commonMain/kotlin/${pkg.replace(".", "/")}").also { it.mkdirs() }

    private fun write(file: File, content: String) = file.writeText(content + "\n")
}
