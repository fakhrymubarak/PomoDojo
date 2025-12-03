plugins {
    alias(libs.plugins.pomodojoLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:focus"))
            implementation(project(":core:utils"))
            implementation(compose.runtime)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
