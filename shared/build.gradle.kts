plugins {
    id("kmp.dev.library")
}

kotlin {
    androidLibrary {
        namespace = "com.dev.memebattle.shared"
        compileSdk = 36
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.mvikotlin.core)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.logging)
            
            api(projects.core.network)
            api(projects.network.userAuth.current)
            api(projects.network.user.current)
            api(projects.network.media.current)
            api(projects.network.game.current)
            api(projects.core.data.packs)
            api(projects.core.data.game)
            api(projects.feature.home.impl)
            api(projects.feature.packs.impl)
            api(projects.feature.gameplay.impl)
            
            api(projects.host.root)
        }
    }
}
