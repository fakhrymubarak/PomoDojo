plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:datastore"))
            implementation(project(":domain:pomodoro"))
            implementation(project(":domain:preferences"))
            implementation(libs.androidx.datastore.preferences)
        }

        commonTest.dependencies {
            implementation(libs.orbit.test)
        }
    }
}
