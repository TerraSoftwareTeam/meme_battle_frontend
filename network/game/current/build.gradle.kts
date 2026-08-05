plugins {
    id("kmp.dev.library")
    kotlin("plugin.serialization")
}
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
            api(projects.network.game.v3)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
        val conventionalMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(conventionalMain)
        maybeCreate("iosMain").dependsOn(conventionalMain)
    }
}