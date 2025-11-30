rootProject.name = "PomoDojo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":domain:focus")
include(":domain:preferences")
include(":feature:dashboard")
include(":feature:focus")
include(":feature:preferences")
include(":core:datastore")
include(":core:database")
include(":core:framework")
include(":core:designsystem")
include(":core:utils")
include(":data:history")
include(":data:pomodoro")
include(":data:preferences")
include(":domain:history")
include(":domain:common")
