package kmp.dev

import helpers.catalog
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("kmp.dev.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val kmp = extensions.getByType<KotlinMultiplatformExtension>()

kmp.sourceSets.named("commonMain").configure {
    dependencies {
        implementation(catalog.findLibrary("compose-runtime").get())
        implementation(catalog.findLibrary("compose-foundation").get())
        implementation(catalog.findLibrary("compose-material3").get())
        implementation(catalog.findLibrary("compose-material-icons").get())
    }
}
