package tech.dev

import helpers.catalog
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val kmp = extensions.getByType<KotlinMultiplatformExtension>()

kmp.sourceSets.named("commonMain").configure {
    dependencies {
        implementation(catalog.findLibrary("koin-core").get())
        implementation(catalog.findLibrary("koin-compose").get())
    }
}