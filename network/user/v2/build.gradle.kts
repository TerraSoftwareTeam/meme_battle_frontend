plugins {
    id("kmp.dev.library")
    kotlin("plugin.serialization")
}
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.user.v2"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "UserNetwork"
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