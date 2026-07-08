package android.dev

import com.android.build.api.dsl.LibraryExtension
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.library")
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())
