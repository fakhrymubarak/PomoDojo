plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:notification"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:preferences"))
            implementation(project(":domain:history"))

            implementation(libs.androidx.datastore.preferences)
            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
        }

        commonTest.dependencies {
            implementation(project(":domain:common"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.orbit.test)
        }
    }
}
