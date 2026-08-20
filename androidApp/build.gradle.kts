import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val appVersionName: String = (project.findProperty("app.version") as? String) ?: "1.0.0"
val appVersionCode: Int = try {
    val parts = appVersionName.split(".").map { it.takeWhile { char -> char.isDigit() }.toInt() }
    parts.getOrElse(0) { 1 } * 10000 + parts.getOrElse(1) { 0 } * 100 + parts.getOrElse(2) { 0 }
} catch (_: Exception) {
    1
}

android {
    namespace = "com.dev.memebattle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dev.memebattle"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        create("release") {
            val keyFile = rootProject.file("key.jks")
            if (keyFile.exists()) {
                storeFile = keyFile
                storePassword = keystoreProperties.getProperty("storePassword")
                    ?: (project.findProperty("KEYSTORE_PASSWORD") as? String)
                    ?: System.getenv("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                    ?: (project.findProperty("KEY_ALIAS") as? String)
                    ?: System.getenv("KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                    ?: (project.findProperty("KEY_PASSWORD") as? String)
                    ?: System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile?.exists() == true && !releaseSigning.storePassword.isNullOrEmpty()) {
                signingConfig = releaseSigning
            }
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.host.root)
    implementation(projects.core.navigation)
    implementation(projects.core.localization)
    implementation(projects.feature.home.api)
    implementation(projects.feature.packs.api)
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

