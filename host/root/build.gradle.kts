plugins {
    id("kmp.dev.presentation")
}

kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.host.root"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.ui)
            implementation(projects.core.localization)
            implementation(projects.feature.home.api)
            implementation(projects.feature.gameplay.api)
            
            implementation(libs.decompose.core)
            implementation(libs.decompose.compose)
            implementation(libs.essenty.lifecycle.coroutines)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(compose.components.resources)
        }
    }
}


