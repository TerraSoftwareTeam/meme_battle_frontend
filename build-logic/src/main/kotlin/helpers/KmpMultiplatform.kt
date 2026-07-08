package helpers

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.configureKmpMultiplatform(kmp: KotlinMultiplatformExtension) {
    kmp.apply {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        sourceSets.named("commonMain").configure {
            dependencies {
                implementation(catalog.findLibrary("kotlinx-coroutines-core").get())
            }
        }

        sourceSets.named("commonTest").configure {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
