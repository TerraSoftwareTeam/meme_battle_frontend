plugins {
    id("kmp.dev.library")
    id("tech.dev.koin")
}

kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.core.data.game"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain:game"))
            implementation(project(":network:game:current"))
            implementation(project(":core:network"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
