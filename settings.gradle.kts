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

// Sort by name to make it easier to find
include(":composeApp")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":app:di")
include(":core:utils")
include(":core:notification")
include(":data:history")
include(":data:preferences")
include(":domain:common")
include(":domain:pomodoro")
include(":domain:history")
include(":domain:preferences")
include(":feature:dashboard")
include(":feature:pomodoro")
include(":feature:preferences")
include(":core:constant")
include(":data:pomodoro")
