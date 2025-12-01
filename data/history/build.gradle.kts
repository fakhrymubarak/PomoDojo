plugins {
    alias(libs.plugins.pomodojoLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:focus"))
            implementation(project(":domain:history"))
            implementation(project(":domain:common"))
            implementation(project(":core:database"))
            implementation(project(":core:utils"))

            implementation(libs.androidx.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
