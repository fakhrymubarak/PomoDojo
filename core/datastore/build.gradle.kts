plugins {
    id("com.fakhry.pomodojo.kotlinMultiplatform")
    id("com.fakhry.pomodojo.jacocoMultiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.serialization.json)

            // DataStore library
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
