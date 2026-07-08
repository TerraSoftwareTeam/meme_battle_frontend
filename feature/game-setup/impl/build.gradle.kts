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
            baseName = "FeatureGameSetupImpl"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.feature.gameSetup.impl"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:game-setup:api"))
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
