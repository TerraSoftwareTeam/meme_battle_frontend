plugins {
    id("kmp.dev.presentation")
}

kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.host.root"
        compileSdk = 34
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.ui)
            implementation(projects.feature.home.api)
            
            implementation(libs.decompose.core)
            implementation(libs.decompose.compose)
            implementation(libs.essenty.lifecycle.coroutines)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(compose.components.resources)
        }
    }
}


