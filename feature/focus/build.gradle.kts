plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:notification"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:preferences"))
            implementation(project(":domain:history"))

            implementation(libs.androidx.datastore.preferences)
            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
        }

        commonTest.dependencies {
            implementation(libs.orbit.test)
        }
    }
}
