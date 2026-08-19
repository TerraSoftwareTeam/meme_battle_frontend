import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    id("kmp.dev.library")
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlinSerialization)
}

val envFile = rootProject.file(".env")
val envProperties = Properties()
if (envFile.exists()) {
    envProperties.load(envFile.inputStream())
}

buildkonfig {
    packageName = "com.dev.memebattle.core.network"
    exposeObjectWithName = "BuildKonfig"
    defaultConfigs {
        buildConfigField(STRING, "API_BASE_URL", envProperties.getProperty("API_BASE_URL") ?: "https://api.meme.skyfly.hackclub.app")
        buildConfigField(STRING, "WS_BASE_URL", envProperties.getProperty("WS_BASE_URL") ?: "wss://realtime.meme.skyfly.hackclub.app")
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreNetwork"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.dev.memebattle.core.network"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.json)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.auth)
            api(libs.ktor.client.websockets)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
        androidMain.dependencies {
            implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
            implementation(libs.koin.android)
            implementation(libs.ktor.client.cio)
        }
        val iosMain = maybeCreate("iosMain").apply {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val wasmJsMain = maybeCreate("wasmJsMain").apply {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}
