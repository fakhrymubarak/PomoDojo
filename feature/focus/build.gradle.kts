plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:framework"))
            implementation(project(":core:datastore"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:preferences"))
            implementation(project(":domain:history"))

            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
        }

        commonTest.dependencies {
            implementation(libs.orbit.test)
        }
    }
}
