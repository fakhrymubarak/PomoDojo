plugins {
    alias(libs.plugins.pomodojoLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:common"))
            implementation(project(":domain:focus"))
        }
    }
}
