package android.dev

import com.android.build.api.dsl.ApplicationExtension
import helpers.catalog
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.application")
}

configureKotlinAndroid(extensions.getByType<ApplicationExtension>())

configure<ApplicationExtension> {
    defaultConfig {
        targetSdk = catalog.findVersion("targetSdk").get().requiredVersion.toInt()
    }
}
