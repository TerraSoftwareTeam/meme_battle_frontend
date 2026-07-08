plugins {
    id("kmp.dev.compose")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreLocalization"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.core.localization"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
