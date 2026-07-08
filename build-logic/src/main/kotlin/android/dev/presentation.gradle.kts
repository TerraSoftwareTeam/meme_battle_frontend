package android.dev

import com.android.build.api.dsl.LibraryExtension
import helpers.catalog
import helpers.configureAndroidCompose
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

plugins {
    id("android.dev.compose")
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())
configureAndroidCompose(extensions.getByType<LibraryExtension>())

dependencies {
    add("implementation", catalog.findLibrary("androidx-lifecycle-viewmodelCompose").get())
    add("implementation", catalog.findLibrary("mvikotlin-core").get())
    add("implementation", catalog.findLibrary("mvikotlin-main").get())
    add("implementation", catalog.findLibrary("mvikotlin-coroutines").get())
    add("implementation", catalog.findLibrary("mvikotlin-logging").get())
}