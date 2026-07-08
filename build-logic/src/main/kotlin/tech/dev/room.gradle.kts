package tech.dev

import helpers.catalog
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("com.google.devtools.ksp")
    id("androidx.room")
}

val kmp = extensions.getByType<KotlinMultiplatformExtension>()

kmp.sourceSets.named("commonMain").configure {
    dependencies {
        implementation(catalog.findLibrary("room-runtime").get())
    }
}

kmp.sourceSets.named("androidMain").configure {
    dependencies {
        implementation(catalog.findLibrary("androidx-sqlite-framework").get())
    }
}