plugins {
    id("kmp.dev.compose")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreNavigation"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.core.navigation"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.decompose.core)
            implementation(libs.decompose.compose)
            implementation(compose.components.resources)
        }
    }
}
