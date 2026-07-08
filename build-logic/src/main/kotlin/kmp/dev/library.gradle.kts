package kmp.dev

import helpers.configureKmpMultiplatform
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

configureKmpMultiplatform(extensions.getByType<KotlinMultiplatformExtension>())
