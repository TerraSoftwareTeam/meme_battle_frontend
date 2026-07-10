plugins {
    id("kmp.dev.library")
    id("tech.dev.koin")
}

kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.core.data.packs"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain:packs"))
            implementation(project(":network:game:current"))
            implementation(project(":core:network"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
