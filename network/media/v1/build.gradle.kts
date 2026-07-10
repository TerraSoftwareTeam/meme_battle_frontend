plugins {
    id("kmp.dev.library")
    kotlin("plugin.serialization")
}
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.media.v1"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MediaNetwork"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.network)
            implementation(libs.ktor.client.core)
        }
    }
}