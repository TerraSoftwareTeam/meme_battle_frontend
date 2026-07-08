plugins { id("kmp.dev.library") }
kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.network.game.current"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.network.game.v1)
            implementation(projects.core.network)
            implementation(libs.koin.core)
        }
    }
}