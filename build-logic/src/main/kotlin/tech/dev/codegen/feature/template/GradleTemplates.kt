package tech.dev.codegen.feature.template

object GradleTemplates {
    fun apiBuild(pkg: String) = """
        plugins {
            id("kmp.dev.library")
            id("tech.dev.serialization")
        }
        
        kotlin {
            listOf(
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "Feature${pkg.split(".").dropLast(1).last().replaceFirstChar { it.uppercase() }}Api"
                    isStatic = true
                }
            }
        
            androidLibrary {
                namespace = "$pkg"
                compileSdk = libs.versions.compileSdk.get().toInt()
                minSdk = libs.versions.minSdk.get().toInt()
            }
            
            sourceSets {
                commonMain.dependencies {
                    implementation(project(":core:navigation"))
                }
            }
        }
    """.trimIndent()

    fun implBuild(pkg: String, featureKebab: String) = """
        plugins {
            id("kmp.dev.compose")
            id("tech.dev.mvikotlin")
            id("tech.dev.koin")
            id("tech.dev.serialization")
        }
        
        kotlin {
            listOf(
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "Feature${pkg.split(".").dropLast(1).last().replaceFirstChar { it.uppercase() }}Impl"
                    isStatic = true
                }
            }
        
            androidLibrary {
                namespace = "$pkg"
                compileSdk = libs.versions.compileSdk.get().toInt()
                minSdk = libs.versions.minSdk.get().toInt()
            }

            sourceSets {
                commonMain.dependencies {
                    implementation(project(":feature:$featureKebab:api"))
                    implementation(project(":core:navigation"))
                    
                    implementation(libs.koin.compose)
                    implementation(libs.koin.compose.viewmodel)
        
                    implementation(libs.mvikotlin.core)
                    implementation(libs.mvikotlin.main)
                    implementation(libs.mvikotlin.coroutines)
        
                    implementation(libs.decompose.core)
                    implementation(libs.decompose.compose)
                    implementation(libs.essenty.lifecycle.coroutines)
                }
            }
        }
    """.trimIndent()
}
