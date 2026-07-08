plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dev.memebattle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dev.memebattle"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(projects.shared)
}
