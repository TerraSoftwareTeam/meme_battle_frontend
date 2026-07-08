package android.dev

import com.android.build.api.dsl.LibraryExtension
import helpers.catalog
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

plugins {
    id("android.dev.library")
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())

configure<LibraryExtension> {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    add("androidTestImplementation", catalog.findLibrary("androidx-testExt-junit").get())
    add("androidTestImplementation", catalog.findLibrary("androidx-espresso-core").get())
}