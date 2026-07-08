plugins { id("kmp.dev.library") }
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.media.current"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.network.media.v1)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
    }
}