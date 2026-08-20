pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

rootProject.name = "MemeBattle"

include(":androidApp")
include(":webApp")
include(":shared")
include(":host:root")
include(":core:navigation")
include(":core:network")
include(":core:ui")
include(":core:utils")
include(":core:database")
include(":core:localization")
include(":core:domain:packs")
include(":core:data:packs")
include(":core:domain:game")
include(":core:data:game")
include(":network:user_auth:v1")
include(":network:user_auth:current")
include(":network:user:v1")
include(":network:user:current")
include(":network:media:v1")
include(":network:media:current")
include(":network:game:v1")
include(":network:game:current")



// region FEATURE Home
include(":feature:home:api")
include(":feature:home:impl")
// endregion FEATURE Home

// region FEATURE Packs
include(":feature:packs:api")
include(":feature:packs:impl")
// endregion FEATURE Packs



// region FEATURE Gameplay
include(":feature:gameplay:api")
include(":feature:gameplay:impl")
// endregion FEATURE Gameplay

include(":network:game:v2")

include(":network:game:v3")

include(":network:user_auth:v2")

include(":network:user:v2")

include(":network:user_auth:v3")



include(":network:user:v3")
