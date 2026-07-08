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
            baseName = "FeatureGameSetupApi"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.feature.gameSetup.api"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:navigation"))
        }
    }
}
