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
            baseName = "FeaturePacksImpl"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.feature.packs.impl"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:packs:api"))
            implementation(project(":core:navigation"))
            implementation(project(":core:domain:packs"))
            api(project(":core:localization"))
            implementation(project(":network:media:v1"))
            implementation(compose.components.resources)
            
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.mvikotlin.core)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.coroutines)

            implementation(libs.decompose.core)
            implementation(libs.decompose.compose)
            implementation(libs.essenty.lifecycle.coroutines)
            
            implementation(libs.filekit.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
    }
}
