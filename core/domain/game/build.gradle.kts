plugins {
    id("kmp.dev.library")
}

kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.core.domain.game"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    sourceSets {
        commonMain.dependencies {
            api(project(":network:game:current"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
