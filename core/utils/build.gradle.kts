plugins {
    id("kmp.dev.library")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreUtils"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.core.utils"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
