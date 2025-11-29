plugins {
    alias(libs.plugins.pomodojoLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
