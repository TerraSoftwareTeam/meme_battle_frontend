import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("android-gradle-plugin").get())
    implementation(libs.findLibrary("kotlin-gradle-plugin").get())
    implementation(libs.findLibrary("compose-multiplatform-gradle-plugin").get())
    implementation(libs.findLibrary("kotlin-compose-gradle-plugin").get())
    implementation(libs.findLibrary("kotlin-serialization-gradle-plugin").get())
    implementation(libs.findLibrary("ksp-gradle-plugin").get())
    implementation(libs.findLibrary("room-gradle-plugin").get())
    
    implementation("io.swagger.parser.v3:swagger-parser:2.1.20")
    implementation("com.squareup:kotlinpoet:1.16.0")
}
