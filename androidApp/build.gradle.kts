plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

android {
    namespace = "com.dev.memebattle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dev.memebattle"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.host.root)
    implementation(projects.core.localization)
    implementation(libs.androidx.activity.compose)
    implementation(libs.decompose.core)
    implementation(libs.decompose.android)
    implementation(libs.koin.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor)
}

val copyLocalizationResources by tasks.registering(Copy::class) {
    dependsOn(":core:localization:prepareComposeResourcesTaskForCommonMain")
    from(project.project(":core:localization").layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"))
    into(project.layout.projectDirectory.dir("src/main/assets/composeResources/com.dev.memebattle.core.localization"))
}

tasks.matching { (it.name.contains("mergeDebugAssets") || it.name.contains("mergeReleaseAssets") || it.name.contains("packageDebug") || it.name.contains("packageRelease")) && it.name != "copyLocalizationResources" }.configureEach {
    dependsOn(copyLocalizationResources)
}

tasks.clean {
    delete(project.layout.projectDirectory.dir("src/main/assets/composeResources"))
}

