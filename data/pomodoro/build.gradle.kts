plugins {
    alias(libs.plugins.pomodojoLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:focus"))
            implementation(project(":core:utils"))
            implementation(project(":core:datastore"))
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlin.stdlib)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
