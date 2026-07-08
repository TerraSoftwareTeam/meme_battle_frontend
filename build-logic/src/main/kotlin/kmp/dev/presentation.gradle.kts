package kmp.dev

import helpers.catalog
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("kmp.dev.compose")
    id("tech.dev.koin")
    id("tech.dev.mvikotlin")
}

val kmp = extensions.getByType<KotlinMultiplatformExtension>()

kmp.sourceSets.named("commonMain").configure {
    dependencies {
        implementation(catalog.findLibrary("androidx-lifecycle-viewmodelCompose").get())
    }
}