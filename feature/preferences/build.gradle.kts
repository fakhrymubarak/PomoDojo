plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:focus"))
            implementation(project(":domain:preferences"))
        }

        commonTest.dependencies {
            implementation(libs.orbit.test)
        }
    }
}
