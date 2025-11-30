plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:framework"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:history"))
            implementation(project(":domain:common"))
            implementation(project(":domain:preferences"))

            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
        }
        commonTest.dependencies {
            implementation(libs.orbit.test)
        }
    }
}
