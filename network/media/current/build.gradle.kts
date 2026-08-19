plugins {
    id("kmp.dev.library")
    kotlin("plugin.serialization")
}
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.media.current"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MediaCurrentNetwork"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.network.media.v1)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
    }
}