package tech.dev

import helpers.catalog
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

val kmp = extensions.getByType<KotlinMultiplatformExtension>()

kmp.sourceSets.named("commonMain").configure {
    dependencies {
        implementation(catalog.findLibrary("kotlinx-serialization-json").get())
    }
}