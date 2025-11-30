plugins {
    alias(libs.plugins.pomodojoLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:preferences"))
            implementation(project(":core:utils"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
