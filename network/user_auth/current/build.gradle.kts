plugins {
    id("kmp.dev.library")
    kotlin("plugin.serialization")
}
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.user_auth.current"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "User_authCurrentNetwork"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.network.userAuth.v3)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
    }
}