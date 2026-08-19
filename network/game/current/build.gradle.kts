plugins { id("kmp.dev.library") }
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.game.current"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "GameCurrentNetwork"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.network.game.v4)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
    }
}