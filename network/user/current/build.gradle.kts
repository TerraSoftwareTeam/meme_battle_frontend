plugins {
    id("kmp.dev.library")
    kotlin("plugin.serialization")
}
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.user.current"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "UserCurrentNetwork"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.network.user.v2)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
    }
}